package com.airfranceklm.amt.testsupport.dsl;

import com.airfranceklm.amt.testsupport.APIClientRequest;

import java.util.HashMap;

/**
 * DSL to configure the API Client request.
 */
public class APIClientRequestDSL {
    private APIClientRequest request;

    APIClientRequestDSL(APIClientRequest request) {
        this.request = request;
    }

    public APIClientRequestDSL from(String remoteAddr) {
        this.request.setRemoteAddr(remoteAddr);
        return this;
    }

    public APIClientRequestDSL withVerb(String verb) {
        this.request.setHttpVerb(verb);
        return this;
    }

    public APIClientRequestDSL withUri(String uri) {
        this.request.setUri(uri);
        return this;
    }

    public APIClientRequestDSL withHeader(String h, String v) {
        if (this.request.getHeaders() == null) {
            this.request.setHeaders(new HashMap<>());
        }

        this.request.getHeaders().put(h, v);
        return this;
    }

    public APIClientRequestDSL withPayload(String h) {
        this.request.setPayload(h);
        return this;
    }

    public APIClientRequestDSL withoutPayload() {
        this.request.setPayload(null);
        return this;
    }
}
