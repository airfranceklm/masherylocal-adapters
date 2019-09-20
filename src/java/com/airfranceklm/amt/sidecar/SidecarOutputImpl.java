package com.airfranceklm.amt.sidecar;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

/**
 * Output from the lambda sidecar.
 */
public class SidecarOutputImpl implements SidecarOutput {

    private List<String> dropHeaders;
    private Map<String,String> addHeaders;

    /**
     * Date until the output of the function call will not change.
     */
    private Date unchangedUntil;
    private SidecarOutputRouting changeRoute;
    private Map<String, Object> relayParameters;
    private String relayMessage;

    private String payload;
    private JsonNode json;
    private Integer code;
    private String message;

    public SidecarOutputImpl() {
    }

    public SidecarOutputImpl(Integer code) {
        this.code = code;
    }

    @Override
    public List<String> getDropHeaders() {
        return dropHeaders;
    }

    public void setDropHeaders(List<String> dropHeaders) {
        this.dropHeaders = dropHeaders;
    }

    @Override
    public Map<String, String> getAddHeaders() {
        return addHeaders;
    }

    public void setAddHeaders(Map<String, String> addHeaders) {
        this.addHeaders = addHeaders;
    }

    @Override
    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public JsonNode getJson() {
        return json;
    }

    public void setJson(JsonNode json) {
        this.json = json;
    }

    void addHeader(String h, String v) {
        if (addHeaders == null) {
            addHeaders = new HashMap<>();
        }
        addHeaders.put(h, v);
    }

    @Override
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public Date getUnchangedUntil() {
        return unchangedUntil;
    }

    public void setUnchangedUntil(Date unchangedUntil) {
        this.unchangedUntil = unchangedUntil;
    }

    @Override
    public SidecarOutputRouting getChangeRoute() {
        return changeRoute;
    }

    public void setChangeRoute(SidecarOutputRouting changeRoute) {
        this.changeRoute = changeRoute;
    }

    @Override
    public boolean addsContentType() {
        return addsHeader("content-type");
    };

    boolean addsHeader(String str) {
        if (addHeaders != null) {
            for (String s : addHeaders.keySet()) {
                if (str.equalsIgnoreCase(s)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public Map<String, Object> getRelayParameters() {
        return relayParameters;
    }

    @Override
    public HashMap<String, Object> createSerializeableRelayParameters() {
        if (relayParameters == null) {
            return null;
        }

        HashMap<String, Object> retVal = new HashMap<>(relayParameters.size());
        retVal.putAll(relayParameters);

        return retVal;
    }

    public void setRelayParameters(Map<String, Object> relayParameters) {
        this.relayParameters = relayParameters;
    }

    @Override
    public String getRelayMessage() {
        return relayMessage;
    }

    public void setRelayMessage(String relayMessage) {
        this.relayMessage = relayMessage;
    }

    @Override
    public boolean relaysMessageToPostprocessor() {
        return relayMessage != null || (relayParameters != null && relayParameters.size()>0);
    }
}
