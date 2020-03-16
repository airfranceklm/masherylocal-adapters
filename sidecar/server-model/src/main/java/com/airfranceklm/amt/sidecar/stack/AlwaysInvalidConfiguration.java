package com.airfranceklm.amt.sidecar.stack;

public class AlwaysInvalidConfiguration implements SidecarStackConfiguration {

    public static final SidecarStackConfiguration INVALID_CFG = new AlwaysInvalidConfiguration();

    private AlwaysInvalidConfiguration() {
    }

    @Override
    public boolean isValid() {
        return false;
    }
}
