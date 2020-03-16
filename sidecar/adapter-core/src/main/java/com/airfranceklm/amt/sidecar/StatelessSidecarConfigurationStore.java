package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.builders.PostProcessSidecarInputBuilder;
import com.airfranceklm.amt.sidecar.elements.ElementsFactory;
import com.airfranceklm.amt.sidecar.model.MasheryProcessorPointReference;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;

import java.util.Objects;
import java.util.Set;

/**
 * Stateless sidecar configuration store. This store is provided to support the unit testing. The store will rebuild
 * and compile the configuration and builder on each attempt. Not suitable for production purposes.
 */
public class StatelessSidecarConfigurationStore implements SidecarConfigurationStore {

    private SidecarProcessor processor;
    private ElementsFactory factory;

    @Override
    public void bindTo(SidecarProcessor processor) {
        this.processor = processor;
        this.factory = processor.getSupportedElements();
        Objects.requireNonNull(this.processor);
        Objects.requireNonNull(this.factory);
    }

    public StatelessSidecarConfigurationStore() {
    }

    @Override
    public String getName() {
        return "Stateless";
    }

    public void useFactory(ElementsFactory factory) {
        Objects.requireNonNull(factory);
        this.factory = factory;
    }

    @Override
    public PreProcessorSidecarRuntime getPreProcessor(PreProcessEvent ppe) {
        return ConfigurationStoreHelper.createPreProcessorRuntime(ppe, processor, factory);
    }

    @Override
    public PostProcessSidecarInputBuilder getPostProcessor(PostProcessEvent ppe) {
        return ConfigurationStoreHelper.getPostProcessSidecarInputBuilder(ppe, this.processor, this.factory);
    }

    @Override
    public void acceptConfigurationChange(MasheryProcessorPointReference endpRef, String declaredInFile, PreProcessorSidecarRuntime cfg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void acceptConfigurationChange(MasheryProcessorPointReference endpRef, String declaredInFile, PostProcessSidecarInputBuilder cfg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forget(MasheryProcessorPointReference ref) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<MasheryProcessorPointReference> getDeclaredIn(String file) {
        throw new UnsupportedOperationException();
    }
}
