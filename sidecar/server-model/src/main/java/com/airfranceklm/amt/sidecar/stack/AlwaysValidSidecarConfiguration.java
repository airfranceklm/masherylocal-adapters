package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.stack.SidecarStackConfiguration;

/**
 * A sidecar configuration that is always valid.
 */
public class AlwaysValidSidecarConfiguration implements SidecarStackConfiguration {
    @Override
    public boolean isValid() {
        return true;
    }
}
