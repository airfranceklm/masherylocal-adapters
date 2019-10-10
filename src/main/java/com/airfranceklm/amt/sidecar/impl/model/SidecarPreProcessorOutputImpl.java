package com.airfranceklm.amt.sidecar.impl.model;

import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SidecarPreProcessorOutputImpl extends AbstractSidecarOutputImpl implements SidecarPreProcessorOutput {

    private RequestModificationCommandImpl modify;
    private Map<String,?> relayParams;
    private JsonNode relay;

    @Override
    public RequestModificationCommandImpl getModify() {
        return modify;
    }

    public void setModify(RequestModificationCommandImpl modify) {
        this.modify = modify;
    }

    public JsonNode getRelay() {
        return relay;
    }

    public void setRelay(JsonNode relay) {
        this.relay = relay;

        if (relay != null) {
            this.relayParams = jsonToMap(relay);
        } else {
            this.relayParams = null;
        }
    }

    @Override
    @JsonIgnore
    public Map<String, ?> getRelayParams() {
        return relayParams;
    }

    public void setRelayParams(Map<String, ?> relayParams) {
        this.relayParams = relayParams;
    }

    @Override
    public Serializable createSerializableRelayParameters() {
        return jsonToString(relay);
    }

    @Override
    public boolean relaysMessageToPostprocessor() {
        return relay != null;
    }

    @Override
    public boolean modifies() {
        return modify != null;
    }

    @Override
    public SidecarPreProcessorOutput toSerializableForm() {
        this.relay = null;
        return this;
    }
}
