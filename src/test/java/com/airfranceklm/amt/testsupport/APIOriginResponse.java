package com.airfranceklm.amt.testsupport;

import java.util.Map;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.*;

public class APIOriginResponse extends RequestCaseDatum {
    Map<String,String> headers;
    String payload;
    int code;
    long payloadLength;

    Class payloadOwner;
    String payloadResource;

    APIOriginResponse() {
        super();
    }

    APIOriginResponse(Map<String,Object> yaml) {
        buildFromYAML(yaml);
    }

    @Override
    void buildFromYAML(Map<String, Object> yaml) {
        super.buildFromYAML(yaml);

        forDefinedStringMap(yaml, "headers", this::setHeaders);
        forDefinedInteger(yaml, "code", this::setCode);
        forDefinedString(yaml, "payload", this::setPayload);

        if (payload != null) {
            this.payloadLength = payload.length();
        } else {
            payloadLength = 0;
        }
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getPayload() {
        return payload;
    }

    public int getCode() {
        return code;
    }

    public void setPayloadLength(long payloadLength) {
        this.payloadLength = payloadLength;
    }

    public void setPayload(Class clazz, String resource) {
        this.payloadOwner = clazz;
        this.payloadResource = resource;
        this.payload = null;
    }
}
