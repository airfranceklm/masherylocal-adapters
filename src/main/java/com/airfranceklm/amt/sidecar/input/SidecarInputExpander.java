package com.airfranceklm.amt.sidecar.input;

import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

import java.io.IOException;

@FunctionalInterface
public interface SidecarInputExpander<T extends ProcessorEvent> {
    void accept(T pe, SidecarInput lambdaInput) throws IOException;
}
