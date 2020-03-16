package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.ProcessorServices;
import com.airfranceklm.amt.sidecar.identity.ClusterIdentity;
import com.airfranceklm.amt.sidecar.identity.ProcessorKeySet;
import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.CallModificationCommand;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.model.SidecarOutput;

import java.io.IOException;

/**
 * Abstraction over the stack used to invoke the lambda functionality
 */
public interface SidecarStack {

    String getStackName();

    /**
     * Perform an invocation of a sidecar.
     *
     * @param cfg transport configuration for the endpoint
     * @param cmd input required
     * @return result of the invocation
     * @throws IOException if the sidecar could not be reached, or if the circuit breaker is currently open.
     */
    <T extends CallModificationCommand, U extends SidecarOutput<T>> U invoke(SidecarStackConfiguration cfg,
                                                                             SidecarInvocationData cmd,
                                                                             Class<U> expectedType) throws IOException;

    /**
     * Derive the configuration.
     *
     * @param cfg lambda sidecar configuration
     * @return an opaque object representing necessary derived configuration for the subsequent use in
     * the {@link #invoke(SidecarStackConfiguration, SidecarInvocationData, Class)}
     * method.
     */
    SidecarStackConfiguration configureFrom(SidecarConfiguration cfg);

    void useProcessorServices(ProcessorServices ps);

    void useAlcpIdentities(ProcessorKeySet ci);

    ProcessorServices getProcessorServices();
}
