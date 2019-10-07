package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.config.MasheryConfigSidecarConfigurationBuilder;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.config.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.input.SidecarInputBuilder;
import com.airfranceklm.amt.sidecar.input.SidecarRuntimeCompiler;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

import java.util.Map;
import java.util.Set;

/**
 * Stateless sidecar configuration store. This store is provided to support the unit testing. The store will rebuild
 * and compile the configuration and builder on each attempt. Not suitable for production purposes.
 */
public class StatelessSidecarConfigurationStore implements SidecarConfigurationStore {

    private SidecarRuntimeCompiler compiler;

    @Override
    public void bindTo(AFKLMSidecarProcessor processor) {
        compiler = new SidecarRuntimeCompiler(processor);
    }

    public StatelessSidecarConfigurationStore() {
    }

    private MasheryConfigSidecarConfigurationBuilder mashBuilder = new MasheryConfigSidecarConfigurationBuilder();

    @Override
    public SidecarConfiguration getConfiguration(ProcessorEvent event) {
        return mashBuilder.buildFrom(event);
    }

    @Override
    public SidecarConfiguration getConfiguration(SidecarInputPoint point, String serviceId, String endpointId, Map<String, String> mashConfig) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SidecarInputBuilder<PreProcessEvent> getPreProcessorInputBuilder(SidecarConfiguration cfg) {
        return compiler.compilePreProcessor(cfg);
    }

    @Override
    public SidecarInputBuilder<PostProcessEvent> getPostProcessorInputBuilder(SidecarConfiguration cfg) {
        return compiler.compilePostProcessor(cfg);
    }

    @Override
    public SidecarInputBuilder<PreProcessEvent> getPreflightInputBuilder(SidecarConfiguration cfg) {
        return compiler.compilePreFlight(cfg);
    }

    @Override
    public void acceptConfigurationChange(MasheryPreprocessorPointReference ref,
                                          String declaredIn,
                                          SidecarConfiguration cfg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forget(MasheryPreprocessorPointReference ref) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<MasheryPreprocessorPointReference> getDeclaredIn(String file) {
        throw new UnsupportedOperationException();
    }
}
