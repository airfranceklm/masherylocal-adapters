package com.airfranceklm.amt.testsupport.dsl;

import com.airfranceklm.amt.testsupport.APIOriginRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class APIOriginRequestDSL {
    private APIOriginRequest data;

    public APIOriginRequestDSL(APIOriginRequest data) {
        this.data = data;
    }

    public APIOriginRequestDSL withOriginalURI(String uri) {
        data.setUri(uri);
        return this;
    }

    public APIOriginRequestDSL droppingRequestHeaders(String... headers) {
        if (data.getMasheryDroppedHeaders() == null) {
            data.setMasheryDroppedHeaders(new ArrayList<>());
        }
        Collections.addAll(data.getMasheryDroppedHeaders(), headers);

        return this;
    }

    public APIOriginRequestDSL addingHeader(String h, String v) {
        if (data.getMasheryAddedHeaders() == null) {
            data.setMasheryAddedHeaders(new HashMap<>());
        }
        data.getMasheryAddedHeaders().put(h, v);
        return this;
    }

    public APIOriginRequestDSL expectDropHeaders(String... headers) {
        if (data.getExpectedSidecarDroppedHeaders() == null) {
            data.setExpectedSidecarDroppedHeaders(new ArrayList<>());
        }
        Collections.addAll(data.getExpectedSidecarDroppedHeaders(), headers);

        return this;
    }

    public APIOriginRequestDSL expectAddHeader(String h, String v) {
        if (data.getExpectedSidecarAddedHeaders() == null) {
            data.setExpectedSidecarAddedHeaders(new HashMap<>());
        }
        data.getExpectedSidecarAddedHeaders().put(h, v);

        return this;
    }

    public APIOriginRequestDSL expectSetVerb(String verb) {
        data.setExpectSetVerb(verb);
        return this;
    }

    public APIOriginRequestDSL expectSetURI(String uri) {
        data.setExpectSetUri(uri);
        return this;
    }

    public APIOriginRequestDSL expectOverridePayload(String payload) {
        data.setExpectOverridingBodyValue(payload);
        data.setExpectOverrideBody(payload != null);
        return this;
    }
}
