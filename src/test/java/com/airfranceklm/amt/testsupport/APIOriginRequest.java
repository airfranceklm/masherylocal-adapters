package com.airfranceklm.amt.testsupport;

import java.util.List;
import java.util.Map;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.*;

public class APIOriginRequest extends RequestCaseDatum {
    String uri;
    List<String> masheryDroppedHeaders;
    Map<String, String> masheryAddedHeaders;

    List<String> expectedSidecarDroppedHeaders;
    Map<String, String> expectedSidecarAddedHeaders;

    String expectSetVerb;
    String expectSetUri;
    boolean expectOverrideBody = false;
    String expectOverridingBodyValue;

    APIOriginRequest() {
    }

    APIOriginRequest(Map<String,Object> yaml) {
        this();
        buildFromYAML(yaml);
    }

    void copyFrom(APIOriginRequest other) {
        this.uri = other.uri;
        this.masheryDroppedHeaders = other.masheryDroppedHeaders;
        this.masheryAddedHeaders = other.masheryAddedHeaders;
        this.expectedSidecarDroppedHeaders = other.expectedSidecarDroppedHeaders;
        this.expectedSidecarAddedHeaders = other.expectedSidecarAddedHeaders;
        this.expectSetVerb = other.expectSetVerb;
        this.expectSetUri = other.expectSetUri;
        this.expectOverrideBody = other.expectOverrideBody;
        this.expectOverridingBodyValue = other.expectOverridingBodyValue;
    }

    void buildFromYAML(Map<String,Object> yaml) {
        super.buildFromYAML(yaml);

        forDefinedString(yaml, "provider uri", this::setUri);
        forDefinedStringList(yaml, "dropped headers", this::setMasheryDroppedHeaders);
        forDefinedStringMap(yaml, "added headers", this::setMasheryAddedHeaders);
    }

    void buildSidecarEffectExpectation(Map<String,Object> yaml) {
        forDefinedStringList(yaml, "dropped headers", this::setExpectedSidecarDroppedHeaders);

        forDefinedStringMap(yaml, "added headers", this::setExpectedSidecarAddedHeaders);

        forDefinedString(yaml, "set http verb", this::setExpectSetVerb);
        forDefinedString(yaml, "set uri", this::setUri);

        forDefinedString(yaml, "override body", (v) -> {
            setExpectOverrideBody(true);
            setExpectOverridingBodyValue(v);
        });
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<String> getMasheryDroppedHeaders() {
        return masheryDroppedHeaders;
    }

    public void setMasheryDroppedHeaders(List<String> masheryDroppedHeaders) {
        this.masheryDroppedHeaders = masheryDroppedHeaders;
    }

    public Map<String, String> getMasheryAddedHeaders() {
        return masheryAddedHeaders;
    }

    public void setMasheryAddedHeaders(Map<String, String> masheryAddedHeaders) {
        this.masheryAddedHeaders = masheryAddedHeaders;
    }

    public List<String> getExpectedSidecarDroppedHeaders() {
        return expectedSidecarDroppedHeaders;
    }

    public void setExpectedSidecarDroppedHeaders(List<String> expectedSidecarDroppedHeaders) {
        this.expectedSidecarDroppedHeaders = expectedSidecarDroppedHeaders;
    }

    public Map<String, String> getExpectedSidecarAddedHeaders() {
        return expectedSidecarAddedHeaders;
    }

    public void setExpectedSidecarAddedHeaders(Map<String, String> expectedSidecarAddedHeaders) {
        this.expectedSidecarAddedHeaders = expectedSidecarAddedHeaders;
    }

    public String getExpectSetVerb() {
        return expectSetVerb;
    }

    public void setExpectSetVerb(String expectSetMethod) {
        this.expectSetVerb = expectSetMethod;
    }

    public String getExpectSetUri() {
        return expectSetUri;
    }

    public void setExpectSetUri(String expectSetUri) {
        this.expectSetUri = expectSetUri;
    }

    public boolean isExpectOverrideBody() {
        return expectOverrideBody;
    }

    public void setExpectOverrideBody(boolean expectOverrideBody) {
        this.expectOverrideBody = expectOverrideBody;
    }

    public String getExpectOverridingBodyValue() {
        return expectOverridingBodyValue;
    }

    public void setExpectOverridingBodyValue(String expectOverridingBodyValue) {
        this.expectOverridingBodyValue = expectOverridingBodyValue;
    }


}
