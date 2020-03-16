package com.airfranceklm.amt.sidecar.config;

import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;

@FunctionalInterface
public interface SecondPassFunction<SCType extends SidecarConfiguration> {
    int apply(SCType config, String key, String value);
}
