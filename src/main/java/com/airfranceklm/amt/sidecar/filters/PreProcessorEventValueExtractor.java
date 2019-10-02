package com.airfranceklm.amt.sidecar.filters;

import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;

@FunctionalInterface
public interface PreProcessorEventValueExtractor<T> {
    T accept(PreProcessEvent ppe, String label);
}
