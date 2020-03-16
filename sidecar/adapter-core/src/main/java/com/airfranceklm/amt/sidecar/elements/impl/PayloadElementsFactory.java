package com.airfranceklm.amt.sidecar.elements.impl;

import com.airfranceklm.amt.sidecar.JsonHelper;
import com.airfranceklm.amt.sidecar.elements.*;
import com.airfranceklm.amt.sidecar.model.SidecarInputHTTPMessage;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amtml.payload.PayloadOperations;
import com.fasterxml.jackson.databind.JsonNode;
import com.mashery.http.client.HTTPClientResponse;
import com.mashery.http.io.ContentSource;
import com.mashery.http.server.HTTPServerRequest;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import com.mashery.trafficmanager.model.core.ApplicationRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.airfranceklm.amt.sidecar.SidecarProcessor.addContentBody;
import static com.airfranceklm.amt.sidecar.SidecarInvocationData.*;
import static com.airfranceklm.amt.sidecar.elements.DataElementSalience.*;
import static com.airfranceklm.amt.sidecar.elements.NumericElements.RequestPayloadSize;
import static com.airfranceklm.amt.sidecar.elements.NumericElements.ResponsePayloadSize;
import static com.airfranceklm.amt.sidecar.elements.PayloadElements.*;
import static com.airfranceklm.amt.sidecar.model.SidecarInput.Accessor.*;

@Slf4j
public class PayloadElementsFactory {

    private static final String REQUEST_PAYLOAD_INTERMEDIARY_KEY = "RequestAsJSON";
    private static final String RESPONSE_PAYLOAD_INTERMEDIARY_KEY = "ResponseAsJSON";

    private static ChainableExtractor<ProcessorEvent, JsonNode> convertRequestBodyToJSON = (ProcessorEvent ppe, SidecarInvocationData sid) -> {
        JsonNode retVal = sid.getIntermediary(REQUEST_PAYLOAD_INTERMEDIARY_KEY);
        if (retVal == null) {
            final ApplicationRequest req = ppe.getCallContext().getRequest();

            try {
                byte[] jsonData = PayloadOperations.bodyContentIfJson(req.getHTTPRequest().getHeaders()
                        , req.getBody());
                if (jsonData != null) {
                    retVal = JsonHelper.readTransportOptimizedJSON(jsonData);
                    sid.useIntermediary(REQUEST_PAYLOAD_INTERMEDIARY_KEY, retVal);
                }
            } catch (IOException ex) {
                log.warn(String.format("Failed to extract JSON data: %s", ex.getMessage()), ex);
            }
        }

        return retVal;
    };

    private static ChainableExtractor<PostProcessEvent, JsonNode> convertResponseBodyToJSON = (PostProcessEvent ppe, SidecarInvocationData sid) -> {
        JsonNode retVal = sid.getIntermediary(RESPONSE_PAYLOAD_INTERMEDIARY_KEY);
        if (retVal == null) {
            final HTTPClientResponse req = ppe.getClientResponse();

            try {
                if (PayloadOperations.bearsJson(req.getHeaders(), req.getBody())) {
                    byte[] data = extractPostProcessorContent(ppe, sid);
                    if (data != null) {
                        retVal = JsonHelper.readTransportOptimizedJSON(data);
                        sid.useIntermediary(RESPONSE_PAYLOAD_INTERMEDIARY_KEY, retVal);
                    }
                }
            } catch (IOException ex) {
                log.warn(String.format("Failed to extract JSON data: %s", ex.getMessage()), ex);
            }
        }

        return retVal;
    };

    private static DataElement<ProcessorEvent, Object> createRequestFragmentExtractor(String param) {
        ChainedExtractionDataElement<ProcessorEvent, Object> retVal
                = new ChainedExtractionDataElement<>(RequestPayloadFragment, param, CONTENT_OPERATION);

        ChainableExtractor<ProcessorEvent, Object> extractor =
                convertRequestBodyToJSON.andThen((node, sid) -> {
                    if (node != null) {
                        final JsonNode at = node.at(param);
                        if (!at.isMissingNode()) {
                            return at;
                        }
                    }

                    // In all other cases, null will be returned
                    return null;
                });

        retVal.extractUsing(extractor);
        retVal.convertUsing((obj, sid) -> allocOrGetPayloadFragments(allocOrGetRequest(sid.getInput())).put(param, obj));

        return retVal;
    }

    private static DataElement<PostProcessEvent, Object> createResponseFragmentExtractor(String param) {
        ChainedExtractionDataElement<PostProcessEvent, Object> retVal
                = new ChainedExtractionDataElement<>(ResponsePayloadFragment, param, CONTENT_OPERATION);

        ChainableExtractor<PostProcessEvent, Object> extractor =
                convertResponseBodyToJSON.andThen((node, sid) -> {
                    if (node != null) {
                        final JsonNode at = node.at(param);
                        if (!at.isMissingNode()) {
                            return at;
                        }
                    }

                    // In all other cases, null will be returned
                    return null;
                });

        retVal.extractUsing(extractor);
        retVal.convertUsing((obj, sid) -> allocOrGetPayloadFragments(allocOrGetResponse(sid.getInput())).put(param, obj));

        return retVal;
    }

    public static DataElement<ProcessorEvent, Long> createRequestBodySizeExtractor(String param) {
        SimpleDataElement<ProcessorEvent, Long> retVal = new SimpleDataElement<>(RequestPayloadSize, OBVIOUS_ERRORS);

        retVal.extractWith((pe) -> {
            ContentSource cs = pe.getCallContext().getRequest().getHTTPRequest().getBody();
            if (cs != null) {
                return cs.getContentLength();
            } else {
                return 0L;
            }
        });

        // The body size by its own is not communicated.

        return retVal;
    }

    public static DataElement<PostProcessEvent, Long> createResponseBodySizeExtractor(String param) {
        SimpleDataElement<PostProcessEvent, Long> retVal = new SimpleDataElement<>(ResponsePayloadSize, OBVIOUS_ERRORS);

        retVal.extractWith((pe) -> {
            ContentSource cs = pe.getClientResponse().getBody();
            if (cs != null) {
                return cs.getContentLength();
            } else {
                return 0L;
            }
        });

        // The body size by its own is not communicated.

        return retVal;
    }

    private static DataElement<ProcessorEvent, ProcessorEvent> createRequestBodyExtractor(String param) {
        SimpleDataElement<ProcessorEvent, ProcessorEvent> retVal = new SimpleDataElement<>(RequestPayload, CONTENT_OPERATION);

        retVal.extractWith((pe) -> pe);

        retVal.convertUsing((pe, sid) -> {
            final HTTPServerRequest httpReq = pe.getCallContext().getRequest().getHTTPRequest();
            final ContentSource body = httpReq.getBody();

            if (body != null) {
                SidecarInputHTTPMessage msg = allocOrGetRequest(sid.getInput());
                byte[] data = extractPreProcessorContent(pe, sid);

                addContentBody(httpReq.getHeaders(), data, msg);
            }
        });

        return retVal;
    }

    private static byte[] extractPreProcessorContent(ProcessorEvent ppe, SidecarInvocationData sid) {
        return allocOrGetRawPayloadIntermediary(ppe.getCallContext().getRequest().getBody(), RAW_REQUEST_PAYLOAD_KEY, sid);
    }

    private static byte[] extractPostProcessorContent(PostProcessEvent ppe, SidecarInvocationData sid) {
        return allocOrGetRawPayloadIntermediary(ppe.getClientResponse().getBody(), RAW_RESPONSE_PAYLOAD_KEY, sid);
    }

    private static byte[] allocOrGetRawPayloadIntermediary(ContentSource body, String key, SidecarInvocationData sid) {

        if (sid.hasIntermediary(key)) {
            return sid.getIntermediary(key);
        }

        byte[] retVal = null;

        if (body != null) {
            try {
                retVal = PayloadOperations.getContentsOf(body);
            } catch (IOException ex) {
                log.error(String.format("Content extraction has failed: %s", ex.getMessage()), ex);
            }

            sid.useIntermediary(key, retVal);
        }

        return retVal;
    }

    ;

    private static DataElement<PostProcessEvent, PostProcessEvent> createResponseBodyExtractor(String param) {
        SimpleDataElement<PostProcessEvent, PostProcessEvent> retVal = new SimpleDataElement<>(ResponsePayload, CONTENT_OPERATION);

        retVal.extractWith((pe) -> pe);

        retVal.convertUsing((pe, sid) -> {
            final HTTPClientResponse httpResp = pe.getClientResponse();
            final ContentSource body = httpResp.getBody();

            if (body != null) {
                SidecarInputHTTPMessage msg = allocOrGetResponse(sid.getInput(), pe.getClientResponse().getStatusCode());

                byte[] content = extractPostProcessorContent(pe, sid);
                addContentBody(httpResp.getHeaders(), content, msg);
            }
        });

        return retVal;
    }

    public static void fill(ElementsFactoryBuilder b) {
        b.addCommonElement(RequestPayloadSize, PayloadElementsFactory::createRequestBodySizeExtractor);
        b.addCommonElement(RequestPayload, PayloadElementsFactory::createRequestBodyExtractor);
        b.addCommonElement(RequestPayloadFragment, PayloadElementsFactory::createRequestFragmentExtractor);

        b.addPostProcessorElement(ResponsePayloadSize, PayloadElementsFactory::createResponseBodySizeExtractor);
        b.addPostProcessorElement(ResponsePayload, PayloadElementsFactory::createResponseBodyExtractor);
        b.addPostProcessorElement(ResponsePayloadFragment, PayloadElementsFactory::createResponseFragmentExtractor);
    }
}
