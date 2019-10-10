package com.airfranceklm.amt.sidecar.impl.model;

import com.airfranceklm.amt.sidecar.model.PayloadCarrier;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

import static com.airfranceklm.amt.sidecar.impl.model.AbstractSidecarOutputImpl.jsonToMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayloadCarrierImpl implements PayloadCarrier {
    private Map<String,?> jsonPayload;
    private String payload;
    private JsonNode json;
    private Boolean base64Encoded;

    @Override
    public Boolean getBase64Encoded() {
        return base64Encoded;
    }

    public void setBase64Encoded(Boolean base64Encoded) {
        this.base64Encoded = base64Encoded;
    }

    @Override
    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public JsonNode getJson() {
        return json;
    }

    public void setJson(JsonNode json) {

        this.json = json;
        if (json != null) {
            this.jsonPayload = jsonToMap(json);
        } else {
            this.jsonPayload = null;
        }
    }

    @Override
    @JsonIgnore
    public Map<String, ?> getJSONPayload() {
        return jsonPayload;
    }

    public void setJSONPayload(Map<String, ?> jsonPayload) {
        this.jsonPayload = jsonPayload;
    }

    protected boolean containsOnlyNulls() {
        return base64Encoded == null &&
                payload == null &&
                json == null &&
                jsonPayload == null;
    }
}
