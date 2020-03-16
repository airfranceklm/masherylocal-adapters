package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.builders.PostProcessSidecarInputBuilder;
import com.airfranceklm.amt.sidecar.model.MasheryProcessorPointReference;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;

import java.util.Set;

/**
 * Store of pre-compiled builders that will be actually producing the sidecar.
 */
public interface SidecarConfigurationStore {

    void bindTo(SidecarProcessor processor);


    PreProcessorSidecarRuntime getPreProcessor(PreProcessEvent ppe);

    PostProcessSidecarInputBuilder getPostProcessor(PostProcessEvent ppe);

    Set<MasheryProcessorPointReference> getDeclaredIn(String file);

    /**
     * Notification from the local files watcher service that will notify the store where a local configuration has changed
     * @param endpRef reference to the Mashery endpoint
     * @param declaredInFile path of the file where this configuration is declared.
     * @param cfg Configuration object.
     */
    void acceptConfigurationChange(MasheryProcessorPointReference endpRef, String declaredInFile, PreProcessorSidecarRuntime cfg);
    void acceptConfigurationChange(MasheryProcessorPointReference endpRef, String declaredInFile, PostProcessSidecarInputBuilder cfg);

    /**
     * Notification from the local files watcher service that the local configuration for this endpoint has been removed.
     * @param ref reference to Mashery endpoint
     */
    void forget(MasheryProcessorPointReference ref);

    String getName();
}
