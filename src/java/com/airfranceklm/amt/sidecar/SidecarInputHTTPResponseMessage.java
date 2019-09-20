package com.airfranceklm.amt.sidecar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SidecarInputHTTPResponseMessage extends SidecarInputHTTPMessage {

    private int responseCode;
    private JsonNode json;

    SidecarInputHTTPResponseMessage() {
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
