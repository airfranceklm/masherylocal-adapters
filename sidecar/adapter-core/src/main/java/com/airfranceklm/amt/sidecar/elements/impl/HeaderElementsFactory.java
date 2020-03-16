package com.airfranceklm.amt.sidecar.elements.impl;

import com.airfranceklm.amt.sidecar.elements.*;
import com.mashery.http.HTTPHeaders;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.airfranceklm.amt.sidecar.elements.DataElementSalience.FAIL_FAST;
import static com.airfranceklm.amt.sidecar.elements.ObjectElements.AllRequestHeaders;
import static com.airfranceklm.amt.sidecar.elements.ObjectElements.AllResponseHeaders;
import static com.airfranceklm.amt.sidecar.elements.ParameterizedStringElement.*;
import static com.airfranceklm.amt.sidecar.filters.StringFilterFactory.createCaseInsensitiveMatcher;
import static com.airfranceklm.amt.sidecar.model.SidecarInput.Accessor.allocOrGetRequest;
import static com.airfranceklm.amt.sidecar.model.SidecarInput.Accessor.allocOrGetResponse;

public class HeaderElementsFactory {

    private static DataElement<ProcessorEvent,String> createRequestHeader(String param) {
        SimpleDataElement<ProcessorEvent, String> retVal = new SimpleDataElement<>(RequestHeader, FAIL_FAST);

        retVal.extractWith((pe) -> {
            String extracted = pe.getCallContext().getRequest().getHTTPRequest().getHeaders().get(param);
            if (extracted == null && pe instanceof PreProcessEvent) {
                extracted = ((PreProcessEvent) pe).getClientRequest().getHeaders().get(param);
            }

            return extracted;
        });

        retVal.convertUsing((v, sid) -> {
            if (v != null) {
                allocOrGetRequest(sid.getInput()).addHeader(param.toLowerCase(), v);
            }
        });

        retVal.reportFilterMatchUsing((label, v, sid) -> {
            CommonElementsFactory.reportParameterizedFilterMatch(label, v, sid, RequestHeader.getElementName(), param);
        });

        return retVal;
    }

    private static DataElement<PostProcessEvent,String> createResponseHeaders(String param) {
        SimpleDataElement<PostProcessEvent, String> retVal = new SimpleDataElement<>(ResponseHeader, FAIL_FAST);

        retVal.extractWith((pe) -> {
            String extracted = pe.getClientResponse().getHeaders().get(param);

            if (extracted == null) {
                extracted = pe.getCallContext().getResponse().getHTTPResponse().getHeaders().get(param);
            }

            return extracted;
        });

        retVal.convertUsing((v, sid) -> {
            if (v != null) {
                allocOrGetResponse(sid.getInput()).addHeader(param.toLowerCase(), v);
            }
        });

        return retVal;
    }


    private static DataElement<ProcessorEvent,?> createAllRequestHeaders(String param) {
        SimpleDataElement<ProcessorEvent, Map<String, String>> retVal = new SimpleDataElement<>(AllRequestHeaders, FAIL_FAST);

        retVal.extractWith((pe) -> {
            Map<String, String> extr = toMap(pe.getCallContext().getRequest().getHTTPRequest().getHeaders());

            if (pe instanceof PreProcessEvent) {
                HTTPHeaders clH = ((PreProcessEvent) pe).getClientRequest().getHeaders();
                for (String s : clH) {
                    final String normalizedHeader = s.toLowerCase();

                    if (!extr.containsKey(normalizedHeader)) {
                        extr.put(normalizedHeader, clH.get(s));
                    }
                }
            }

            return extr;
        });
        retVal.convertUsing((v, sid) -> allocOrGetRequest(sid.getInput()).setHeaders(v));

        return retVal;
    }

    private static DataElement<PostProcessEvent,?> createAllResponseHeaders(String param) {
        SimpleDataElement<PostProcessEvent, HTTPHeaders> retVal = new SimpleDataElement<>(AllResponseHeaders, FAIL_FAST);

        retVal.extractWith((pe) -> pe.getClientResponse().getHeaders());
        retVal.convertUsing((v, sid) -> allocOrGetResponse(sid.getInput()).setHeaders(toMap(v)));

        return retVal;
    }

    private static DataElement<ProcessorEvent,?> createRequestHeadersSkipping(String param) {
        SimpleDataElement<ProcessorEvent, Map<String, String>> retVal = new SimpleDataElement<>(RequestHeadersSkipping, FAIL_FAST);

        Function<String, Boolean> skipFunction = createCaseInsensitiveMatcher(param);

        retVal.extractWith((pe) -> {
            HTTPHeaders source = null;
            if (pe instanceof PreProcessEvent) {
                source = ((PreProcessEvent) pe).getClientRequest().getHeaders();
            } else {
                source = pe.getCallContext().getRequest().getHTTPRequest().getHeaders();
            }

            return extractHeadersSkipping(source, skipFunction);
        });

        retVal.convertUsing((v, sid) -> allocOrGetRequest(sid.getInput()).setHeaders(v));

        return retVal;
    }

    private static DataElement<PostProcessEvent,?> createResponseHeadersSkipping(String param) {
        SimpleDataElement<PostProcessEvent, Map<String, String>> retVal = new SimpleDataElement<>(ResponseHeadersSkipping, FAIL_FAST);

        Function<String, Boolean> skipFunction = createCaseInsensitiveMatcher(param);

        retVal.extractWith((pe) -> extractHeadersSkipping(pe.getServerResponse().getHeaders(), skipFunction));
        retVal.convertUsing((v, sid) -> allocOrGetResponse(sid.getInput()).setHeaders(v));

        return retVal;
    }

    private static Map<String, String> extractHeadersSkipping(HTTPHeaders headers, Function<String, Boolean> skipFunction) {
        Map<String, String> extracted = new HashMap<>();
        for (String s : headers) {
            if (!skipFunction.apply(s)) {
                extracted.put(s.toLowerCase(), headers.get(s));
            }
        }
        return extracted;
    }

    private static Map<String, String> toMap(HTTPHeaders headers) {
        if (headers != null) {
            Map<String, String> retVal = new HashMap<>();
            for (String h : headers) {
                retVal.put(h.toLowerCase(), headers.get(h));
            }

            return retVal;
        } else {
            return null;
        }
    }

    public static void fill(ElementsFactoryBuilder b) {
        b.addCommonElement(RequestHeader, HeaderElementsFactory::createRequestHeader);
        b.addPostProcessorElement(ResponseHeader, HeaderElementsFactory::createResponseHeaders);

        b.addCommonElement(AllRequestHeaders, HeaderElementsFactory::createAllRequestHeaders);
        b.addPostProcessorElement(AllResponseHeaders, HeaderElementsFactory::createAllResponseHeaders);

        b.addCommonElement(RequestHeadersSkipping, HeaderElementsFactory::createRequestHeadersSkipping);
        b.addPostProcessorElement(ResponseHeadersSkipping, HeaderElementsFactory::createResponseHeadersSkipping);
    }
}
