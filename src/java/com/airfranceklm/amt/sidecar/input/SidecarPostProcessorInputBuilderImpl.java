package com.airfranceklm.amt.sidecar.input;

import com.airfranceklm.amt.sidecar.SidecarInput;
import com.airfranceklm.amt.sidecar.SidecarInputHTTPMessage;
import com.airfranceklm.amt.sidecar.SidecarInputHTTPResponseMessage;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.mashery.http.HTTPHeaders;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;

import java.io.IOException;

import static com.airfranceklm.amt.sidecar.AFKLMSidecarProcessor.addContentBody;

public class SidecarPostProcessorInputBuilderImpl extends SidecarInputBuilderImpl<PostProcessEvent> {
    public SidecarPostProcessorInputBuilderImpl(SidecarConfiguration cfg) {
        super(cfg);
    }

    void assertRequestBodySize() {
        addConditionAssertion(this::inspectMaxResponseBodySize);
    }


    private InspectionResult inspectMaxResponseBodySize(PostProcessEvent ppe) {
        return doAssertBodySize(SidecarRuntimeCompiler.getPostProcessorBodySize(ppe));
    }

    void expandPostProcessToResponseHeaders(PostProcessEvent ppe,
                                                    SidecarConfiguration cfg,
                                                    SidecarInput input) {

        HTTPHeaders apiRespHeaders = ppe.getClientResponse().getHeaders();
        if (cfg.includeSpecificResponseHeaders()) {
            cfg.forEachIncludedResponseHeader(h -> {
                String hv = apiRespHeaders.get(h);
                if (hv != null) {
                    input.getRequest().addHeader(h.toLowerCase(), hv);
                }
            });
        } else {
            for (String header : apiRespHeaders) {
                final String h = header.toLowerCase();
                if (!cfg.skipsResponseHeader(h)) {
                    input.getResponse().addHeader(h, apiRespHeaders.get(header));
                }
            }
        }
    }

    void expandPostProcessorToRequestHeaders(PostProcessEvent ppe,
                                                     SidecarConfiguration cfg,
                                                     SidecarInput input) {
        HTTPHeaders reqHeaders = ppe.getCallContext().getRequest().getHTTPRequest().getHeaders();
        if (cfg.includesRequestHeaders()) {
            cfg.forEachHeaderConfig(reqHeader -> {
                String reqHeaderValue = reqHeaders.get(reqHeader.getToken());

                if (reqHeaderValue != null) {
                    input.getOrCreateRequest().addHeader(reqHeader.getToken(), reqHeaderValue);
                }
            });

        } else {
            for (String h : reqHeaders) {
                String normalizedHeaderName = h.toLowerCase();

                if (!cfg.skipsResponseHeader(normalizedHeaderName)) {
                    input.getOrCreateRequest().addHeader(normalizedHeaderName, reqHeaders.get(h));
                }
            }
        }
    }

    void expandPostProcessorToResponsePayload(PostProcessEvent ppe,
                                                      SidecarInput input) throws IOException {
        addContentBody(ppe.getClientResponse().getBody(), input.getResponse());
    }


    void expandPostProcessorToRequestPayload(PostProcessEvent ppe,
                                                     SidecarInput input) throws IOException {
        addContentBody(ppe.getCallContext().getRequest().getHTTPRequest().getBody(), input.getOrCreateRequest());
    }

    void expandRequestHeadersSkipping(PostProcessEvent ppe, SidecarInput input) {
        SidecarInputHTTPMessage msg = input.getOrCreateRequest();
        HTTPHeaders reqHeaders = ppe.getCallContext().getRequest().getHTTPRequest().getHeaders();
        for (String h: reqHeaders) {
            String normalizedHeader = h.toLowerCase();
            if (!getSidecarConfig().skipsRequestHeader(normalizedHeader)) {
                msg.addHeader(normalizedHeader, reqHeaders.get(h));
            }
        }
    }

    void expandAllRequestHeaders(PostProcessEvent ppe, SidecarInput input) {
        SidecarInputHTTPMessage msg = input.getOrCreateRequest();
        HTTPHeaders reqHeaders = ppe.getCallContext().getRequest().getHTTPRequest().getHeaders();
        for (String h: reqHeaders) {
            msg.addHeader(h.toLowerCase(), reqHeaders.get(h));
        }
    }

    void expandResponseHeadersSkipping(PostProcessEvent ppe, SidecarInput input) {
        SidecarInputHTTPResponseMessage msg = input.getOrCreateResponse();
        HTTPHeaders reqHeaders = ppe.getClientResponse().getHeaders();
        for (String h: reqHeaders) {
            String normalizedHeader = h.toLowerCase();
            if (!getSidecarConfig().skipsResponseHeader(normalizedHeader)) {
                msg.addHeader(normalizedHeader, reqHeaders.get(h));
            }
        }
    }

    void expandAllResponseHeaders(PostProcessEvent ppe, SidecarInput input) {
        SidecarInputHTTPResponseMessage msg = input.getOrCreateResponse();
        HTTPHeaders reqHeaders = ppe.getClientResponse().getHeaders();
        for (String h: reqHeaders) {
            msg.addHeader(h.toLowerCase(), reqHeaders.get(h));
        }
    }

    void expandResponseCode(PostProcessEvent ppe, SidecarInput input) {
        input.getOrCreateResponse().setResponseCode(ppe.getClientResponse().getStatusCode());
    }
}
