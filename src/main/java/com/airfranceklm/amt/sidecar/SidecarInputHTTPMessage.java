package com.airfranceklm.amt.sidecar;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.airfranceklm.amt.sidecar.SidecarInput.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SidecarInputHTTPMessage {
    private Map<String, String> headers;
    private long payloadLength;
    private String payload;

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

    long getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(long payloadLength) {
        this.payloadLength = payloadLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SidecarInputHTTPMessage that = (SidecarInputHTTPMessage) o;
        return payloadLength == that.payloadLength &&
                Objects.equals(headers, that.headers) &&
                Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers, payloadLength, payload);
    }

    @Override
    public String toString() {
        return "SidecarInputHTTPMessage{" +
                "headers=" + SidecarInput.mapToString(headers) +
                ", payloadLength=" + payloadLength +
                ", payload='" + payload + '\'' +
                '}';
    }
}
