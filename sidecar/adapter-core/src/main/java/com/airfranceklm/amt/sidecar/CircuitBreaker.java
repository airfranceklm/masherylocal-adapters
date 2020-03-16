package com.airfranceklm.amt.sidecar;

public interface CircuitBreaker {
    boolean isOpen(String endpointID);

    default void trackError(String endpointId) {}

    default void trackSuccess(String endpoint) {}
}
