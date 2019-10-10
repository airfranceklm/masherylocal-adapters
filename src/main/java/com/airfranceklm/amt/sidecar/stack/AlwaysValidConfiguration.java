package com.airfranceklm.amt.sidecar.stack;

public class AlwaysValidConfiguration implements AFKLMSidecarStack.AFKLMSidecarStackConfiguration {
    @Override
    public boolean isValid() {
        return true;
    }
}
