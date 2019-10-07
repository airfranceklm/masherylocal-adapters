package com.airfranceklm.amt.sidecar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import java.security.MessageDigest;
import java.util.Objects;

import static com.airfranceklm.amt.sidecar.SidecarInput.stdOf;
import static com.airfranceklm.amt.sidecar.SidecarInput.utf8Of;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SidecarInputHTTPResponseMessage extends SidecarInputHTTPMessage {

    private int responseCode;
    private JsonNode json;

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

    public JsonNode getJson() {
        return json;
    }

    public void setJson(JsonNode json) {
        this.json = json;
    }

    void updateChecksum(MessageDigest md) {
        super.updateChecksum(md);
        md.update(stdOf(String.valueOf(responseCode)));
        if (json != null) {
            md.update(utf8Of(json.toString())); // TODO: this may not be the best; needs revisiting.
        }
    }

    @Override
    public String toString() {
        return "SidecarOutputHTTPMessage{" +
                "code=" + responseCode +
                ", headers=" + SidecarInput.mapToString(getHeaders()) +
                ", " + reportJSONForToString() +
                ", payloadLength=" + getPayloadLength() +
                ", payload='" + getPayload() + '\'' +
                ", base64Encoded='" + isPayloadBase64Encoded() + '\'' +
                '}';
    }

    private String reportJSONForToString() {
        if (json == null) return "no json";
        else return "DEFINED json";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SidecarInputHTTPResponseMessage that = (SidecarInputHTTPResponseMessage) o;
        return responseCode == that.responseCode &&
                Objects.equals(json, that.json);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), responseCode, json);
    }
}
