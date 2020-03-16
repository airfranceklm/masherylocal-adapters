package com.airfranceklm.amt.sidecar.stack;

public interface SidecarStackConfiguration {
    /**
     * Checks whether this configuration is valid, i.e. all fields supplied and formatted according
     * to the requirements.
     * @return true if configuration is correctly configured, false otherwise.
     */
    boolean isValid();
}
