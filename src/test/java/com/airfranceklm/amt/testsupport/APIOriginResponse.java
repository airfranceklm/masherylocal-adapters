package com.airfranceklm.amt.testsupport;

import java.util.Map;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.*;

public class APIOriginResponse extends RequestCaseDatum {
    Map<String,String> headers;
    String body;
    int code;
    long bodyLength;

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
        forDefinedString(yaml, "payload", this::setBody);

        if (body != null) {
            this.bodyLength = body.length();
        } else {
            bodyLength = 0;
        }
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public int getCode() {
        return code;
    }

    public void setBodyLength(long bodyLength) {
        this.bodyLength = bodyLength;
    }
}
