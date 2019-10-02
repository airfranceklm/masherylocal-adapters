package com.airfranceklm.amt.testsupport;

import java.util.HashMap;
import java.util.Map;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.forDefinedString;
import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.forDefinedStringMap;

public class APIClientRequest extends RequestCaseDatum {

    String version = "HTTP/1.1"; // TODO parse this.
    boolean overSSL = true; // TODO parse this.
    String remoteAddr;
    String httpVerb;
    String uri;
    Map<String,String> headers;
    long payloadLength;
    String payload;

    /**
     * Default constructor.
     */
    APIClientRequest() {
    }

    APIClientRequest(Map<String, Object> yaml) {
        this();
        buildFromYAML(yaml);
    }

    void copyFrom(APIClientRequest another) {
        if (remoteAddr == null) {
            remoteAddr = another.remoteAddr;
        }

        if (httpVerb == null) {
            httpVerb = another.httpVerb;
        }

        if (uri == null) {
            uri = another.uri;
        }

        if (another.headers != null) {
            if (headers == null) {
                headers = new HashMap<>();
            }

            headers.putAll(another.headers);
        }

        if (payload == null && another.payload != null) {
            payloadLength = another.payloadLength;
            payload = another.payload;
        }
    }

    void buildFromYAML(Map<String, Object> clRequestYaml) {
        super.buildFromYAML(clRequestYaml);

        forDefinedString(clRequestYaml, "http verb", this::setHttpVerb);
        forDefinedString(clRequestYaml, "uri", this::setUri);
        forDefinedString(clRequestYaml, "remote address", this::setRemoteAddr);
        forDefinedStringMap(clRequestYaml, "headers", this::setHeaders);

        forDefinedString(clRequestYaml, "payload", this::setPayload);

        if (payload != null) {
            payloadLength = payload.length();
        }
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setOverSSL(boolean overSSL) {
        this.overSSL = overSSL;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setPayload(String payload) {
        this.payload = payload;
        if (this.payload != null) {
            this.payloadLength = this.payload.length();
        } else {
            this.payloadLength = 0;
        }
    }

    public String getVersion() {
        return version;
    }

    public boolean isOverSSL() {
        return overSSL;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public String getUri() {
        return uri;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public long getPayloadLength() {
        return payloadLength;
    }

    public String getPayload() {
        return payload;
    }
}
