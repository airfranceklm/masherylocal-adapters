package com.airfranceklm.amt.testsupport.dsl;

import com.airfranceklm.amt.testsupport.APIOriginResponse;

import java.util.HashMap;

public class APIOriginResponseDSL {
    private APIOriginResponse data;

    public APIOriginResponseDSL(APIOriginResponse data) {
        this.data = data;
    }

    public APIOriginResponseDSL withHeader(String h, String v) {
        if (data.getHeaders() == null) {
            data.setHeaders(new HashMap<>());
        }
        data.getHeaders().put(h, v);
        return this;
    }

    public APIOriginResponseDSL withCode(int code) {
        data.setCode(code);
        return this;
    }

    public APIOriginResponseDSL withPayload(String payload) {
        data.setPayload(payload);
        data.setPayloadLength(payload == null ? 0 : payload.length());
        return this;
    }

    public APIOriginResponseDSL withNoPayload() {
        return withPayload(null);
    }

    public void withPayloadFromResource(Class owner, String resourceName) {
        data.setPayload(null);
        data.setPayload(owner, resourceName);
    }
}
