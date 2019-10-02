package com.airfranceklm.amt.testsupport;

import java.util.Map;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.*;
import static com.airfranceklm.amt.testsupport.RequestMockSupport.*;

class HTTPServerResponseData extends RequestCaseDatum {
    private Number code;
    private String payload;
    private String statusMessage;
    private Map<String,String> headers;

    HTTPServerResponseData() {
        super();
    }

    HTTPServerResponseData(Map<String, Object> yaml) {
        super(yaml);
    }

    void buildFromYAML(Map<String,Object> yaml) {
        super.buildFromYAML(yaml);

        forDefinedInteger(yaml, "code", this::setCode);

        forDefinedString(yaml, "payload", this::setPayload);
        forDefinedString(yaml, "status message", this::setStatusMessage);
        forDefinedStringMap(yaml, "headers", this::setHeaders);
    }

    void copyFrom(HTTPServerResponseData other) {
        this.code = other.code;
        this.payload = other.payload;
        this.statusMessage = other.statusMessage;
    }

    public Number getCode() {
        return code;
    }

    public String getPayload() {
        return payload;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setCode(Number code) {
        this.code = code;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
