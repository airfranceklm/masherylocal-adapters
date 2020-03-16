package com.airfranceklm.amt.sidecar.model;

import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.SidecarInputPoint;

import static com.airfranceklm.amt.sidecar.model.SidecarInputPoint.Preflight;

public class PreFlightSidecarConfiguration extends SidecarConfiguration {

    private Boolean enabled;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public boolean preflightDemanded() {
        return (enabled != null && enabled) ||
                (getSidecarParams() != null && getSidecarParams().size() > 0) ||
                (getElements() != null && getElements().size() > 0);

    }

    @Override
    public SidecarInputPoint getPoint() {
        return Preflight;
    }
}
