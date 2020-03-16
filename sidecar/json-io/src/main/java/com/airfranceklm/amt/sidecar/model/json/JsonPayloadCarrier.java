package com.airfranceklm.amt.sidecar.model.json;

import com.airfranceklm.amt.sidecar.model.PayloadCarrier;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static com.airfranceklm.amt.sidecar.CommonExpressions.allocOrGet;
import static com.airfranceklm.amt.sidecar.model.json.JsonAbstractSidecarOutput.jsonToMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class JsonPayloadCarrier implements PayloadCarrier {
    @Getter @Setter
    protected TreeMap<String, String> passHeaders;

    @Getter @Setter @JsonIgnore
    protected Map<String,?> jsonPayload;

    @Getter @Setter
    protected String payload;

    @Getter
    protected JsonNode json;

    @Getter @Setter
    protected Boolean base64Encoded;

    public JsonPayloadCarrier(TreeMap<String, String> passHeaders, Map<String, ?> jsonPayload, String payload, JsonNode json, Boolean base64Encoded) {
        this.passHeaders = passHeaders;
        this.jsonPayload = jsonPayload;
        this.payload = payload;
        this.json = json;
        this.base64Encoded = base64Encoded;
    }

    protected static TreeMap<String, String> asTreeMap(Map<String,String> m) {
        if (m == null) {
            return null;
        } else if (m instanceof TreeMap) {
            return (TreeMap<String,String>)m;
        } else {
            TreeMap<String, String> retVal = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            retVal.putAll(m);
            return retVal;
        }
    }

    public void setJson(JsonNode json) {

        this.json = json;
        if (json != null) {
            this.jsonPayload = jsonToMap(json);
        } else {
            this.jsonPayload = null;
        }
    }

    @JsonIgnore
    public static Map<String, String> allocOrGetPassHeaders(JsonPayloadCarrier c) {
        return allocOrGet(c::getPassHeaders, c::setPassHeaders, () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
    }


    protected boolean containsOnlyNulls() {
        return base64Encoded == null &&
                payload == null &&
                json == null &&
                jsonPayload == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonPayloadCarrier that = (JsonPayloadCarrier) o;
        return Objects.equals(jsonPayload, that.jsonPayload) &&
                Objects.equals(payload, that.payload) &&
                Objects.equals(json, that.json) &&
                Objects.equals(base64Encoded, that.base64Encoded) &&
                Objects.equals(passHeaders, that.passHeaders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonPayload, payload, json, base64Encoded);
    }


    /**
     * Used for backward-compatibility with Phase-1 Mashery protocol. Going forward, use
     * {@link #setPassHeaders(TreeMap)} instead
     * @param headers headers to pass
     * @deprecated
     */
    @Deprecated
    public void setHeaders(TreeMap<String,String> headers) {
        setPassHeaders(headers);
    }

    /**
     * Used for backward-compatibility with Phase-1 Mashery protocol. Going forward, use
     * {@link #setPassHeaders(TreeMap)} instead
     * @param headers headers to pass
     * @deprecated
     */
    @Deprecated
    public void setAddHeaders(TreeMap<String,String> headers) {
        setPassHeaders(headers);
    }
}
