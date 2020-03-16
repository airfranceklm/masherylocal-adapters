package com.airfranceklm.amt.sidecar.config;

import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;

import java.util.regex.Matcher;

@FunctionalInterface
public interface FirstPassFunction<SCType extends SidecarConfiguration> {
    int apply(SCType config, Matcher key, String value);
}
