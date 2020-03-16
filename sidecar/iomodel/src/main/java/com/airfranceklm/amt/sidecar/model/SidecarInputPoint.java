package com.airfranceklm.amt.sidecar.model;

/**
 * Specifies the point at which this sidecar input is captured.
 * <ul>
 *     <li>{@link #StaticModification}: static modification of every call using a fixed, pre-supplied output. Used
 *     mainly to override SaaS-connfigured output API call routing with the local routing. The sidecar implmenetations
 *     will never receive this value.</li>
 *     <li>{@link #Preflight}: coarse-grained check, involving idempotent calls. Used primary for authentication /
 *     authorization use cases and globcal call expansion.</li>
 *     <li>{@link #PreProcessor} and {@link #PostProcessor} apply for the individual call.</li>
 * </ul>
 */
public enum SidecarInputPoint {
    PreCondition, PreProcessor, PostProcessor, Preflight, StaticModification;


    static SidecarInputPoint[] getAllInputPoint() {
        return new SidecarInputPoint[]{Preflight, PreProcessor, PostProcessor};
    }

    public static SidecarInputPoint[] getOperationalPoints() {
        return new SidecarInputPoint[]{PreProcessor, PostProcessor};
    }

    public boolean isOperationalPoint() {
        return this == PreProcessor || this == PostProcessor;
    }

    public MasheryProcessorPointReference at(String serviceId, String endpointId) {
        return new MasheryProcessorPointReference(serviceId, endpointId, this);
    }
}
