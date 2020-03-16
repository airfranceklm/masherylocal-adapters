package com.airfranceklm.amt.sidecar.config;

import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;

import java.util.regex.Matcher;

/**
 * Interface for the parser that performs a simple translation of the paramter into the extracting
 * <code>SCType</code> configuration. For the function that may need to perform validation, use
 * {@link FirstPassFunction} or {@link SecondPassFunction}.
 * @param <SCType>
 */
@FunctionalInterface
public interface SimplePassFunction<SCType extends SidecarConfiguration> {
    void apply(SCType cfg, Matcher m, String value);

    default int yieldScore(SCType cfg, Matcher m, String value) {
        apply(cfg, m, value);
        return 0;
    }
}
