package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Interface for a filterable data element
 * @param <EType>
 * @param <T>
 */
public interface FilterableDataElement<EType extends ProcessorEvent, T> extends DataElement<EType, T> {
    /**
     * Add a filter without the label
     * @param filterFunction filtering function determining the relevance of the data element
     *                       to the sidecar invocation.
     */
    default void addFilter(Function<T, DataElementRelevance> filterFunction) {
        addFilter(filterFunction, null);
    }

    void addFilter(Function<T, DataElementRelevance> filterFunction, String label);

    DataElementRelevance getOnNoFilterMatched();

    void setOnNoFilterMatched(DataElementRelevance onNoFilterMatched);
}
