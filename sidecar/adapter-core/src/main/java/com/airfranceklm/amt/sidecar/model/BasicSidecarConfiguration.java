package com.airfranceklm.amt.sidecar.model;

import lombok.Builder;
import lombok.Singular;

import java.util.List;
import java.util.Map;

/**
 * Buildable basic {@link SidecarConfiguration} that is used mainly at the unit tests.
 */
public class BasicSidecarConfiguration extends SidecarConfiguration {

    public BasicSidecarConfiguration() {
        super();
    }

    @Builder
    public BasicSidecarConfiguration(String serviceId
            , String endpointId
            , MaxPayloadSizeSetting maximumRequestPayloadSize
            , SidecarSynchronicity synchronicity
            , boolean failsafe
            , StackDemand stack
            , @Singular  Map<String, Object> sidecarParams
            , long timeout, int errors
            , @Singular  List<ElementDemand> elements
            , @Singular List<String> messages
            , ALCPConfiguration alcpConfiguration) {
        super(serviceId, endpointId, maximumRequestPayloadSize, synchronicity, failsafe, stack, sidecarParams, timeout, errors, elements, messages, alcpConfiguration);
    }

    @Override
    public SidecarInputPoint getPoint() {
        return SidecarInputPoint.PreCondition;
    }
}
