package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.model.*;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;

import java.io.IOException;

/**
 * Abstraction over the stack used to invoke the lambda functionality
 */
public interface AFKLMSidecarStack {
    /**
     * Perform an invocation of a sidecar.
     * @param cfg transport configuration for the endpoint
     * @param cmd input required
     * @return result of the invocation
     * @throws IOException if the sidecar could not be reached, or if the circuit breaker is currently open.
     */
    SidecarPreProcessorOutput invokeAtPreProcessor(AFKLMSidecarStackConfiguration cfg, SidecarInvocationData cmd, ProcessorServices services) throws IOException;

    SidecarPostProcessorOutput invokeAtPostProcessor(AFKLMSidecarStackConfiguration cfg, SidecarInvocationData cmd, ProcessorServices services) throws IOException;

    /**
     * Derive the configuration.
     * @param cfg lambda sidecar configuration
     * @return an opaque object representing necessary derived configuration for the subsequent use in
     * the {@link #invokeAtPreProcessor(AFKLMSidecarStackConfiguration, SidecarInvocationData, ProcessorServices)}
     * or {@link #invokeAtPostProcessor(AFKLMSidecarStackConfiguration, SidecarInvocationData, ProcessorServices)}
     * method.
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
