package com.airfranceklm.amt.sidecar.filters;

import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;

@FunctionalInterface
public interface PostProcessorEventValueExtractor<T> {
    T accept(PostProcessEvent ppe, String label);
}
