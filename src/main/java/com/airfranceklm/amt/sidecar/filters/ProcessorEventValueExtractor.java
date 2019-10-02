package com.airfranceklm.amt.sidecar.filters;

import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

/**
 * Functional interface for lambda that requires only {@link com.mashery.trafficmanager.event.processor.model.PreProcessEvent}
 * to extract the desired value.
 * @param <T>
 */

@FunctionalInterface
public interface ProcessorEventValueExtractor<T> {
    T accept(ProcessorEvent pe, String param);
}
