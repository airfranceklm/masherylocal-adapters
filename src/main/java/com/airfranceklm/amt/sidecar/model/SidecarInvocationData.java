package com.airfranceklm.amt.sidecar.model;

import com.airfranceklm.amt.sidecar.stack.AFKLMSidecarStack;
import com.mashery.trafficmanager.cache.Cache;
import com.mashery.trafficmanager.debug.DebugContext;

import java.io.Serializable;

/**
 * A command object used to carry
 */
public class SidecarInvocationData {

    private SidecarInput input;
    private AFKLMSidecarStack stack;
    private AFKLMSidecarStack.AFKLMSidecarStackConfiguration stackConfiguration;

    private String serviceId;
    private String endpointId;
    private boolean idempotentAware;

    private Cache cache;

    private DebugContext debugCtx;

    public SidecarInvocationData(SidecarInput input,
                                 AFKLMSidecarStack stack,
                                 AFKLMSidecarStack.AFKLMSidecarStackConfiguration stackConfiguration) {
        this.input = input;
        this.stack = stack;
        this.stackConfiguration = stackConfiguration;
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

    public AFKLMSidecarStack getStack() {
        return stack;
    }

    public AFKLMSidecarStack.AFKLMSidecarStackConfiguration getStackConfiguration() {
        return stackConfiguration;
    }

    /**
     * Gets the cache key where the idempotent responses should be remembered.
     *
     * @return String representing a combination of serviceId, endpointId, and the payload checksum.
     */
    public String getCacheKey() {
        return String.format("idempotent::%s_%s_%s", serviceId, endpointId, input == null ? "null" : input.getPayloadChecksum());
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
}
