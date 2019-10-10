package com.airfranceklm.amt.sidecar.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import java.security.MessageDigest;
import java.util.Objects;

import static com.airfranceklm.amt.sidecar.model.SidecarInput.stdOf;
import static com.airfranceklm.amt.sidecar.model.SidecarInput.utf8Of;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SidecarInputHTTPResponseMessage extends SidecarInputHTTPMessage {

    private int responseCode;

    public SidecarInputHTTPResponseMessage() {
        this(200);
    }

    SidecarInputHTTPResponseMessage(int code) {
        this.responseCode = code;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }


    void updateChecksum(MessageDigest md) {
        super.updateChecksum(md);
        md.update(stdOf(String.valueOf(responseCode)));
    }

    @Override
    public String toString() {
        return "SidecarOutputHTTPMessage{" +
                "code=" + responseCode +
                ", headers=" + SidecarInput.mapToString(getHeaders()) +
                ", payloadLength=" + getPayloadLength() +
                ", payload='" + getPayload() + '\'' +
                ", base64Encoded='" + isPayloadBase64Encoded() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SidecarInputHTTPResponseMessage that = (SidecarInputHTTPResponseMessage) o;
        return super.equals(o)
            && responseCode == that.responseCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), responseCode);
    }
}
