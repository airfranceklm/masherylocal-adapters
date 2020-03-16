package com.airfranceklm.amt.sidecar.elements;

import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

import java.util.function.Function;
import java.util.function.Predicate;

public interface ElementsFactoryBuilder {
    void addCommonElement(ElementSpec ne, Function<String, DataElement<ProcessorEvent, ?>> creator);
    void addPreProcessorElement(ElementSpec ne, Function<String, DataElement<PreProcessEvent, ?>> creator);
    void addPostProcessorElement(ElementSpec ne, Function<String, DataElement<PostProcessEvent, ?>> creator);

    default void addStringFilter(String alg, Predicate<String> creator) {
        addFilter(alg, String.class, (owner, expr) -> creator);
    }

    default void addStringFilter(String alg, ElementFilterCreator<String> creator) {
        addFilter(alg, String.class, creator);
    }

    <T> void addFilter(String alg, Class<T> type, ElementFilterCreator<T> creator);

    <T> void addNormalizer(String name, DataElementNormalizer<T> norm);

    <T> void addNormalizer(String name, AtomicDataElementNormalizer<T> norm);
}
