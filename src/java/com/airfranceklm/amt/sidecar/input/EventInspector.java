package com.airfranceklm.amt.sidecar.input;

import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

/**
 * Interface for the function that inspects a pre-processor.
 * @param <T>
 */
@FunctionalInterface
public interface EventInspector<T extends ProcessorEvent> {
    InspectionResult accept(T event);
}
