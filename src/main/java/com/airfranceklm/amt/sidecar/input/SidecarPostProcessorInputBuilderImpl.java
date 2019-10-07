package com.airfranceklm.amt.sidecar.input;

import com.airfranceklm.amt.sidecar.SidecarInput;
import com.airfranceklm.amt.sidecar.SidecarInputHTTPMessage;
import com.airfranceklm.amt.sidecar.SidecarInputHTTPResponseMessage;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.mashery.http.HTTPHeaders;
import com.mashery.http.client.HTTPClientResponse;
import com.mashery.http.server.HTTPServerRequest;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.model.core.ApplicationRequest;

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

    void expandPostProcessorToResponsePayload(PostProcessEvent ppe,
                                                      SidecarInput input) throws IOException {
        final HTTPClientResponse clResp = ppe.getClientResponse();

        if (clResp.getBody() != null) {
            HTTPHeaders h = clResp.getHeaders();
            addContentBody(h, clResp.getBody(), input.getOrCreateResponse());
        }
    }

    void expandPostProcessorToRequestPayload(PostProcessEvent ppe,
                                                     SidecarInput input) throws IOException {
        final ApplicationRequest clReq = ppe.getCallContext().getRequest();
        if (clReq.getBody() != null) {
            addContentBody(clReq.getHTTPRequest().getHeaders(), clReq.getBody(), input.getOrCreateRequest());
        }
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

    void expandListedRequestHeaders(PostProcessEvent ppe, SidecarInput input) {
        SidecarInputHTTPMessage msg = input.getOrCreateRequest();
        HTTPHeaders reqHeaders = ppe.getCallContext().getRequest().getHTTPRequest().getHeaders();

        getSidecarConfig().forEachHeaderConfig((c) -> {
            String hValue = reqHeaders.get(c.getToken());
            if (hValue != null) {
                msg.addHeader(c.getToken(), hValue);
            }
        });
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

    void expandMasheryMessageId(PostProcessEvent ppe, SidecarInput input) {
        String messageId = ppe.getCallContext().getResponse().getHTTPResponse().getHeaders().get(MASH_MSG_ID_HEADER);

        if (messageId != null) {
            input.setMasheryMessageId(messageId);
        }
    }

    void expandListedResponseHeaders(PostProcessEvent ppe, SidecarInput input) {
        SidecarInputHTTPResponseMessage msg = input.getOrCreateResponse();
        HTTPHeaders reqHeaders = ppe.getClientResponse().getHeaders();

        getSidecarConfig().forEachIncludedResponseHeader((h) -> {
            String hValue = reqHeaders.get(h);
            if (hValue != null) {
                msg.addHeader(h, hValue);
            }
        });
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
