package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

import java.util.List;
import java.util.function.Function;

import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.ServiceNotReady;
import static com.airfranceklm.amt.sidecar.elements.DataElementSalience.OBVIOUS_ERRORS;

/**
 * A breaker element that will be used by the builder if the configuration wouldn't have been possible
 * to create without errors.
 */
public class ServiceBreakerElement implements DataElement<ProcessorEvent, Void> {

    private List<String> messages;

    public ServiceBreakerElement() {
    }

    public ServiceBreakerElement(List<String> message) {
        this.messages = message;
    }

    public void addFilter(Function<Void, DataElementRelevance> filterFunction, String label) {
        // do nothing.
    }

    @Override
    public DataElementRelevance extract(ProcessorEvent eventFrom, SidecarInvocationData into) {
        if (messages != null && messages.size() > 0) {
            int count = 0;
            for (String s: messages) {
                into.getDebugContext().logEntry(String.format("X-ServiceBreaker-%d", ++count), s);
            }
        }

        return ServiceNotReady;
    }

    @Override
    public int getSalience() {
        return OBVIOUS_ERRORS;
    }

    /**
     * Add a filter without the label
     * @param filterFunction filtering function determining the relevance of the data element
     *                       to the sidecar invocation.
     */
    public void addFilter(Function<Void, DataElementRelevance> filterFunction) {
        addFilter(filterFunction, null);
    }

    @Override
    public ElementSpec getElementSpec() {
        return SyntheticElements.ServiceBreaker;
    }
}
