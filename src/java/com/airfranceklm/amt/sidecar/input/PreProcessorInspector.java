package com.airfranceklm.amt.sidecar.input;

import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;

/**
 * An interface for classes used to determine whether the pre-processor event is matching a condition.
 */
public interface PreProcessorInspector {

    /**
     * Inspect the pre-processor event
     * @param ppe event to inspect
     * @return value indicating whether the inspection passed of failed.
     */
    InspectionResult inspect(PreProcessEvent ppe);
}
