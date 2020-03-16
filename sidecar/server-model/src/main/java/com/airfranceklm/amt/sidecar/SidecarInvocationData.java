package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.ProcessorServices;
import com.airfranceklm.amt.sidecar.elements.DataElementRelevance;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.alcp.MasheryALCPSide;
import com.airfranceklm.amt.sidecar.stack.SidecarStack;
import com.airfranceklm.amt.sidecar.stack.SidecarStackConfiguration;
import com.mashery.trafficmanager.cache.Cache;
import com.mashery.trafficmanager.debug.DebugContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A command object used to carry
 */
public class SidecarInvocationData {

    public static final String RAW_REQUEST_PAYLOAD_KEY  = "/aflkm/RawRequestPayload";
    public static final String RAW_RESPONSE_PAYLOAD_KEY  = "/aflkm/RawResponsePayload";

    private SidecarInput input;
    private SidecarStack stack;
    private SidecarStackConfiguration stackConfiguration;

    private String serviceId;
    private String endpointId;
    private boolean idempotentAware;

    private Cache cache;

    private DebugContext debugCtx;

    private MasheryALCPSide<?,?> applicationLevelCallProtection;

    private DataElementRelevance relevance;

    public SidecarInvocationData() {
        input = new SidecarInput();
    }

    public SidecarInvocationData(SidecarInput input) {
        this.input = input;
    }

    public SidecarInvocationData(SidecarInput input,
                                 SidecarStack stack,
                                 SidecarStackConfiguration stackConfiguration) {
        this.input = input;
        this.stack = stack;
        this.stackConfiguration = stackConfiguration;
    }

    public DataElementRelevance getRelevance() {
        return relevance;
    }

    public void setRelevance(DataElementRelevance relevance) {
        this.relevance = relevance;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public boolean isIdempotentAware() {
        return idempotentAware;
    }

    public void setIdempotentAware(boolean idempotentAware) {
        this.idempotentAware = idempotentAware;
    }

    public SidecarInput getInput() {
        return input;
    }

    public SidecarStack getStack() {
        return stack;
    }

    public SidecarStackConfiguration getStackConfiguration() {
        return stackConfiguration;
    }

    public MasheryALCPSide<?,?> getApplicationLevelCallProtection() {
        return applicationLevelCallProtection;
    }

    public void setApplicationLevelCallProtection(MasheryALCPSide applicationLevelCallProtection) {
        this.applicationLevelCallProtection = applicationLevelCallProtection;
    }

    /**
     * Gets the cache key where the idempotent responses should be remembered.
     *
     * @return String representing a combination of serviceId, endpointId, and the payload checksum.
     */
    public String getCacheKey() {
        return String.format("idempotent::%s_%s_%s", serviceId, endpointId, input == null ? "null" : input.getInputChecksum());
    }

    public Cache getCache() {
        return cache;
    }

    public void setDebugContext(com.mashery.trafficmanager.debug.DebugContext ctx) {
        this.debugCtx = ctx;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public void logEntry(String key, Serializable message) {
        this.debugCtx.logEntry(key, message);
    }

    public DebugContext getDebugContext() {
        return debugCtx;
    }

    public Map<? super Object,? super Object> intermediaries;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n")
                .append(">serviceId=").append(serviceId).append("\n")
                .append(">endpointId=").append(endpointId).append("\n")
                .append(">idempotentAware=").append(idempotentAware).append("\n")
                .append(">stack=").append(stack).append("\n")
                .append(">stackConfiguration=").append(stackConfiguration).append("\n")
                .append(">input=").append(input).append("\n");

        return sb.toString();
    }

    public String getRequestPayload() {
        return null;
    }

    public Map<String,Object> getRequestPayloadAsJSON() {
        return null;
    }

    public String getResponsePayload() {
        return null;
    }

    public Map<String,Object> getResponsePayloadAsJSON() {
        return null;
    }

    @SuppressWarnings("unchecked")
    public <IType> IType getIntermediary(Object key) {
        if (intermediaries != null) {
            return (IType) intermediaries.get(key);
        } else {
            return null;
        }
    }

    public boolean hasIntermediary(Object key) {
        return intermediaries != null && intermediaries.containsKey(key);
    }

    public void useIntermediary(Object key, Object intermediary) {
        if (intermediaries == null) {
            intermediaries = new HashMap<>();
        }
        intermediaries.put(key, intermediary);
    }

    public ClassLoader getStackClassLoader() {
        if (stack != null) {
            return stack.getClass().getClassLoader();
        } else {
            return this.getClass().getClassLoader();
        }
    }

}
