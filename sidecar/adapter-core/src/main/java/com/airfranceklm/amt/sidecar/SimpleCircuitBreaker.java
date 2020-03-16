package com.airfranceklm.amt.sidecar;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple circuit breaker. The circuit breaker tracks successful and error calls towards an endpoint. The circuit
 * breaker opens up when {@link #getCircuitOpenThreshold()} consecutive faults will occur when trying to call the
 * endpoint. The circuit remains open for @{link {@link #getCircuitOpenTime()}} milliseconds, after this the
 * condition clears.
 * <p/>
 * The circuit breaker can be tuned using constructors {@link #SimpleCircuitBreaker(TimeUnit, long)} defining the
 * circuit open time and {@link #SimpleCircuitBreaker(long, TimeUnit, long)} which defines all parameters.
 */
public class SimpleCircuitBreaker implements CircuitBreaker {
    Map<String, CircuitBreakerArc> faults;

    @Getter
    long circuitOpenTime = TimeUnit.SECONDS.toMillis(30);
    @Getter
    long circuitOpenThreshold = 50;

    /**
     * A sensible default constructor.
     */
    public SimpleCircuitBreaker() {
        this.faults = new HashMap<>();
    }

    /**
     * Parameterizes opening threshold and the time the circuit breaker should remain open.
     * @param threshold opening threshold
     * @param tu time unit for the <code>duration</code> parameter
     * @param duration the duration of the circuit breaker staying open, in <code>tu</code> units.
     */
    public SimpleCircuitBreaker(long threshold, TimeUnit tu, long duration) {
        this(tu, duration);

        if (threshold <= 0) {
            throw new IllegalArgumentException("Threshold must be a positive value");
        }

        this.circuitOpenThreshold = threshold;
    }

    public SimpleCircuitBreaker(TimeUnit tu, long duration) {
        this();
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be a positive value");
        }
        circuitOpenTime = tu.toMillis(duration);
    }

    @Override
    public boolean isOpen(String endpointID) {
        CircuitBreakerArc arc = faults.get(endpointID);

        if (arc == null) {
            return false;
        } else {
            if (arc.openUntil > 0) {
                if (arc.openUntil > System.currentTimeMillis()) {
                    return true;
                } else {
                    faults.remove(endpointID);
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    public int getFaultsCount() {
        return this.faults.size();
    }

    @Override
    public void trackError(String endpointId) {
        if (faults.containsKey(endpointId)) {
            CircuitBreakerArc arc = faults.get(endpointId);
            int cnt = arc.count.incrementAndGet();
            if (cnt == circuitOpenThreshold) {
                arc.openUntil = System.currentTimeMillis() + circuitOpenTime;
            }
        } else {
            synchronized (this) {
                faults.put(endpointId, new CircuitBreakerArc());
            }
        }
    }

    @Override
    public void trackSuccess(String endpoint) {
        if (faults.containsKey(endpoint)) {
            // Guard for the race condition between two threads.

            CircuitBreakerArc arc = faults.get(endpoint);
            if (arc != null) {
                int cnt = arc.count.decrementAndGet();
                if (cnt == 0) {
                    faults.remove(endpoint);
                }
            }
        }
    }

    static class CircuitBreakerArc {
        long openUntil = -1;
        AtomicInteger count;

        public CircuitBreakerArc() {
            count = new AtomicInteger(1);
        }
    }
}
