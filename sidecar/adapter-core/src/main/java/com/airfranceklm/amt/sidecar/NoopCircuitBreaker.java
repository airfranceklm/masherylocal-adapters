package com.airfranceklm.amt.sidecar;

/**
 * Circuit breaker meant to be used for unit tests where errors need to be returned back to the
 * calling stack no matter how many these have occurred.
 */
class NoopCircuitBreaker implements CircuitBreaker {
    @Override
    public boolean isOpen(String endpointID) {
        return false;
    }
}
