package com.airfranceklm.amt.sidecar.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.airfranceklm.amt.sidecar.model.SidecarInput.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SidecarInputHTTPMessage {
    private Map<String, String> headers;
    private Map<String, Object> payloadExcerpts;
    private long payloadLength;
    private String payload;
    private boolean payloadBase64Encoded;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    boolean containsOnlyNulls() {
        return (headers == null || headers.size() == 0)
                && payload == null;
    }

    void updateChecksum(MessageDigest md) {
        updateChecksumOfMap(md, "reqHeaders", headers);
        if (payload != null) {
            updateRedirect(md);
            md.update(utf8Of(payload));
        }
    }

    public void addHeader(String h, String val) {
        if (this.headers == null) {
            this.headers = new HashMap<>();
        }
        this.headers.put(h, val);
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public long getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(long payloadLength) {
        this.payloadLength = payloadLength;
    }

    public boolean isPayloadBase64Encoded() {
        return payloadBase64Encoded;
    }

    public void setPayloadBase64Encoded(boolean payloadBase64Encoded) {
        this.payloadBase64Encoded = payloadBase64Encoded;
    }

    public Map<String, Object> getPayloadExcerpts() {
        return payloadExcerpts;
    }

    public void setPayloadExcerpts(Map<String, Object> payloadExcerpts) {
        this.payloadExcerpts = payloadExcerpts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SidecarInputHTTPMessage that = (SidecarInputHTTPMessage) o;
        return payloadLength == that.payloadLength &&
                Objects.equals(headers, that.headers) &&
                Objects.equals(payload, that.payload) &&
                Objects.equals(payloadExcerpts, that.payloadExcerpts) &&
                Objects.equals(payloadBase64Encoded, that.payloadBase64Encoded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers, payloadLength, payload, payloadBase64Encoded);
    }



    @Override
    public String toString() {
        return "SidecarInputHTTPMessage{" +
                "headers=" + mapToString(headers) +
                ", payloadLength=" + payloadLength +
                ", payload='" + payload + '\'' +
                ", payloadExcerpts='" + mapToString(payloadExcerpts) + '\'' +
                ", base64Encoded='" + payloadBase64Encoded + '\'' +
                '}';
    }
}
