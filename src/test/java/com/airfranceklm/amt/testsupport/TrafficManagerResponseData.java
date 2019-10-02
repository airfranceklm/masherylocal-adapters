package com.airfranceklm.amt.testsupport;

import java.util.Map;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.*;

public class TrafficManagerResponseData extends RequestCaseDatum {
    Boolean expectSetComplete;
    String expectSetCompleteWithMessage;
    String expectSetFailedWithMessage;
    Number expectStatusCode;
    String expectStatusMessage;
    String expectResponseBody;
    Map<String,String> responseHeaders;

    TrafficManagerResponseData() {
        super();
    }

    TrafficManagerResponseData(Map<String, Object> yaml) {
        super(yaml);
    }

    boolean modifiesHTTPServerResponse() {
        return expectStatusCode != null || expectStatusMessage != null || expectResponseBody != null;
    }

    void copyFrom(TrafficManagerResponseData other) {
        this.expectSetComplete = other.expectSetComplete;
        this.expectSetCompleteWithMessage = other.expectSetCompleteWithMessage;
        this.expectSetFailedWithMessage = other.expectSetFailedWithMessage;
        this.expectStatusCode = other.expectStatusCode;
        this.expectStatusMessage = other.expectStatusMessage;
        this.expectResponseBody = other.expectResponseBody;
        this.responseHeaders = other.responseHeaders;
    }

    @Override
    void buildFromYAML(Map<String, Object> yaml) {
        super.buildFromYAML(yaml);

        forDefinedBoolean(yaml, "set complete", this::setExpectSetComplete);
        forDefinedString(yaml, "set complete with", this::setExpectSetCompleteWithMessage);
        forDefinedString(yaml, "set failed", this::setExpectSetFailedWithMessage);

        forDefinedInteger(yaml, "status code", this::setExpectStatusCode);
        forDefinedString(yaml, "status message", this::setExpectStatusMessage);

        forDefinedString(yaml, "body", this::setExpectResponseBody);
        forDefinedStringMap(yaml, "headers", this::setResponseHeaders);
    }

    public void setExpectSetComplete(Boolean expectSetComplete) {
        this.expectSetComplete = expectSetComplete;
    }

    public void setExpectSetCompleteWithMessage(String expectSetCompleteWithMessage) {
        this.expectSetCompleteWithMessage = expectSetCompleteWithMessage;
    }

    public void setExpectSetFailedWithMessage(String expectSetFailedWithMessage) {
        this.expectSetFailedWithMessage = expectSetFailedWithMessage;
    }

    public void setExpectStatusCode(Number expectStatusCode) {
        this.expectStatusCode = expectStatusCode;
    }

    public void setExpectStatusMessage(String expectStatusMessage) {
        this.expectStatusMessage = expectStatusMessage;
    }

    public void setExpectResponseBody(String expectResponseBody) {
        this.expectResponseBody = expectResponseBody;
    }

    public void setResponseHeaders(Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }
}
