package com.airfranceklm.amt.sidecar.model;

import lombok.Getter;
import lombok.Setter;

import static com.airfranceklm.amt.sidecar.model.SidecarInputPoint.PreProcessor;

public class PreProcessorSidecarConfiguration extends SidecarConfiguration {

    @Getter @Setter
    private boolean idempotentAware = false;
    @Getter @Setter
    private boolean postProcessOnRouteChange;

    @Override
    public SidecarInputPoint getPoint() {
        return PreProcessor;
    }
}
