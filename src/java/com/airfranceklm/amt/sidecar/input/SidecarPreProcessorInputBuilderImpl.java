package com.airfranceklm.amt.sidecar.input;

import com.airfranceklm.amt.sidecar.SidecarInput;
import com.airfranceklm.amt.sidecar.SidecarInputHTTPMessage;
import com.airfranceklm.amt.sidecar.SidecarInputRouting;
import com.airfranceklm.amt.sidecar.config.ConfigRequirement;
import com.airfranceklm.amt.sidecar.config.ConfigSetting;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.mashery.http.MutableHTTPHeaders;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.airfranceklm.amt.sidecar.AFKLMSidecarProcessor.addContentBody;

public class SidecarPreProcessorInputBuilderImpl extends SidecarInputBuilderImpl<PreProcessEvent> {

    private Set<String> requiredHeaders;

    public SidecarPreProcessorInputBuilderImpl(SidecarConfiguration cfg) {
        super(cfg);
    }

    void assertRequiredHeaders() {
        assertHeadersFromSet(getSidecarConfig().getRequestHeaders());
    }

    private void assertHeadersFromSet(Set<ConfigSetting> set) {
        requiredHeaders = new HashSet<>();
        set.forEach((cs) -> {
            if (cs.getRequired() == ConfigRequirement.Required) {
                requiredHeaders.add(cs.getToken());
            }
        });
        addConditionAssertion(this::inspectRequiredHeaders);
    }

    void assertRequiredPreflightHeaders() {
        assertHeadersFromSet(getSidecarConfig().getPreflightHeaders());
    }

    void assertRequestBodySize() {
        addConditionAssertion(this::inspectMaxRequestBodySize);
    }

    // --------------------------------------------------------
    // Pre-condition assertions

    private InspectionResult inspectMaxRequestBodySize(PreProcessEvent ppe) {
        return doAssertBodySize(SidecarRuntimeCompiler.getPreProcessorBodySize(ppe));
    }

    private InspectionResult inspectRequiredHeaders(PreProcessEvent ppe) {
        MutableHTTPHeaders headers = ppe.getClientRequest().getHeaders();

        for (String s : requiredHeaders) {
            String v = headers.get(s);
            if (v == null || v.trim().length() == 0) {
                return InspectionResult.Fail;
            }
        }

        return InspectionResult.Pass;
    }

    /// -------------------------------------------------------------------
    // Input scope expansion methods.


    void expandHeaders(PreProcessEvent ppe, SidecarInput input) {
        MutableHTTPHeaders headers = ppe.getClientRequest().getHeaders();

        getSidecarConfig().forEachHeaderConfig((c) -> {
            String v = headers.get(c.getToken());
            if (v != null) {
                input.getOrCreateRequest().addHeader(c.getToken(), v);
            }
        });
    }

    void expandHeadersSkipping(PreProcessEvent ppe, SidecarInput input) {
        MutableHTTPHeaders headers = ppe.getClientRequest().getHeaders();

        for (String h : headers) {
            final String headerName = h.toLowerCase();

            if (!getSidecarConfig().skipsRequestHeader(headerName)) {
                input.getOrCreateRequest().addHeader(headerName, headers.get(h));
            }
        }
    }

    void expandAllHeaders(PreProcessEvent ppe, SidecarInput input) {
        MutableHTTPHeaders headers = ppe.getClientRequest().getHeaders();
        SidecarInputHTTPMessage req = input.getOrCreateRequest();
        for (String h : headers) {
            req.addHeader(h.toLowerCase(), headers.get(h));
        }
    }

    void expandPreflightHeaders(PreProcessEvent ppe, SidecarInput input) {
        MutableHTTPHeaders headers = ppe.getClientRequest().getHeaders();
        SidecarInputHTTPMessage req = input.getOrCreateRequest();

        getSidecarConfig().forEachPreflightHeader((h) -> {
            String v = headers.get(h.getToken());
            if (v != null) {
                req.addHeader(h.getToken(), v);
            }
        });
    }

    void expandPreprocessToRouting(PreProcessEvent ppe, SidecarInput input) {
        SidecarInputRouting r = new SidecarInputRouting();
        r.setHttpVerb(ppe.getClientRequest().getMethod());
        r.setUri(ppe.getClientRequest().getURI());

        input.setRouting(r);
    }

    void expandPreprocessToPayload(PreProcessEvent ppe, SidecarInput input) throws IOException {
        addContentBody(ppe.getServerRequest().getBody(), input.getRequest());
    }


}
