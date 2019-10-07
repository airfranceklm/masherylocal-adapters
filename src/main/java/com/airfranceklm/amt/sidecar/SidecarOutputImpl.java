package com.airfranceklm.amt.sidecar;

import com.fasterxml.jackson.core.TreeNode;

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
    private Map<String, ?> relayParams;
    private Map<String, ?> jsonPayload;

    private String payload;
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

    public Map<String, ?> getJSONPayload() {
        return jsonPayload;
    }

    public void setJSONPayload(Map<String, ?> jsonPayload) {
        this.jsonPayload = jsonPayload;
    }

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
    public Map<String, ?> getRelayParams() {
        return relayParams;
    }

    @Override
    public HashMap<String, Object> createSerializeableRelayParameters() {
        if (relayParams == null) {
            return null;
        }

        HashMap<String, Object> retVal = new HashMap<>(relayParams.size());
        retVal.putAll(relayParams);

        return retVal;
    }

    public void setRelayParams(Map<String, ?> relayParams) {
        this.relayParams = relayParams;
    }

    @Override
    public boolean relaysMessageToPostprocessor() {
        return relayParams != null && relayParams.size()>0;
    }
}
