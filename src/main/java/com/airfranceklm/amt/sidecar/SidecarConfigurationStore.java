package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.config.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.input.SidecarInputBuilder;
import com.airfranceklm.amt.sidecar.input.SidecarInputBuilderImpl;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

import java.util.Map;
import java.util.Set;

/**
 * Store of pre-compiled builders that will be actually producing the sidecar.
 */
public interface SidecarConfigurationStore {

    void bindTo(AFKLMSidecarProcessor processor);

    SidecarConfiguration getConfiguration(ProcessorEvent event);

    SidecarConfiguration getConfiguration(SidecarInputPoint point, String serviceId, String endpointId, Map<String, String> mashConfig);

    /**
     * Retrieves the input builder for the specific endpoint.
     * @param cfg configuration of the sidecar processing, as obtained earlier from
     * {@link #getConfiguration(SidecarInputPoint, String, String, Map)}.
     * @return instance of the {@link SidecarInputBuilderImpl} that will be used to build the actual input to the sidecar
     * function.
     */
    SidecarInputBuilder<PreProcessEvent> getPreProcessorInputBuilder(SidecarConfiguration cfg);
    SidecarInputBuilder<PostProcessEvent> getPostProcessorInputBuilder(SidecarConfiguration cfg);

    /**
     * Retrieves the pre-flight configuration builder
     * @param cfg Sidecar configuration.
     * @return A builder that will build the pre-flight inputs.
     */
    SidecarInputBuilder<PreProcessEvent> getPreflightInputBuilder(SidecarConfiguration cfg);


    Set<MasheryPreprocessorPointReference> getDeclaredIn(String file);

    /**
     * Notification from the local files watcher service that will notify the store where a local configuration has changed
     * @param endpRef reference to the Mashery endpoint
     * @param declaredInFile path of the file where this configuration is declared.
     * @param cfg Configuration object.
     */
    void acceptConfigurationChange(MasheryPreprocessorPointReference endpRef, String declaredInFile, SidecarConfiguration cfg);

    /**
     * Notification from the local files watcher service that the local configuration for this endpoint has been removed.
     * @param ref reference to Mashery endpoint
     */
    void forget(MasheryPreprocessorPointReference ref);
}
