package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

import java.util.List;

public interface MasheryConfigurationReader<T extends SidecarConfiguration> {
    T read(ProcessorEvent pe);
    List<String> relevantKeys();
}
