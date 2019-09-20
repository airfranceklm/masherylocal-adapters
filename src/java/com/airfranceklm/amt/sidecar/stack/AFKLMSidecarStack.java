package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.SidecarInput;
import com.airfranceklm.amt.sidecar.SidecarOutput;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;

import java.io.IOException;

/**
 * Abstraction over the stack used to invoke the lambda functionality
 */
public interface AFKLMSidecarStack {
    /**
     * Perform an invocation of a sidecar.
     * @param cfg transport configuration for the endpoint
     * @param input input required
     * @return result of the invocation
     * @throws IOException if the sidecar could not be reached, or if the circuit breaker is currently open.
     */
    SidecarOutput invoke(AFKLMSidecarStackConfiguration cfg, SidecarInput input) throws IOException;

    /**
     * Derive the configuration.
     * @param cfg lambda sidecar configuration
     * @return an opaque object representing necessary derived configuration for the subsequent use in
     * the {@link #invoke(AFKLMSidecarStackConfiguration, SidecarInput)} method.
     */
    AFKLMSidecarStackConfiguration configureFrom(SidecarConfiguration cfg);

    interface AFKLMSidecarStackConfiguration {
        /**
         * Checks whether this configuration is valid, i.e. all fields supplied and formatted according
         * to the requirements.
         * @return true if configuration is correctly configured, false otherwise.
         */
        boolean isValid();
    }
}
