package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import lombok.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.*;
import static com.airfranceklm.amt.sidecar.model.SidecarInput.Accessor.allocOrGetParams;

public abstract class AbstractDataElement<EType extends ProcessorEvent, T>
        implements FilterableDataElement<EType, T>, NormalizableDataElement<T> {

    private ElementSpec elementSpec;
    private int salience;

    private DataElementNormalizer<T> normalizer;
    private BiConsumer<T, SidecarInvocationData> converter;
    private FilterMatchReporter<T> filterMatchReporter;
    private Map<Function<T, DataElementRelevance>, String> filters;

    private DataElementRelevance onNoFilterMatched = Ignored;

    public AbstractDataElement(@NonNull ElementSpec elementSpec, int salience) {
        this.elementSpec = elementSpec;
        this.salience = salience;

        this.filterMatchReporter = this::reportFilterMatch;
    }

    @Override
    public ElementSpec getElementSpec() {
        return elementSpec;
    }

    public abstract String describe(T value);

    public void convertUsing(BiConsumer<T, SidecarInvocationData> converter) {
        this.converter = converter;
    }

    public void addNormalizer(DataElementNormalizer<T> newNorm) {
        normalizer = normalizer != null ? normalizer.andThen(newNorm) : newNorm;
    }

    public void addFilter(Function<T, DataElementRelevance> checker, String label) {
        if (filters == null) {
            filters = new LinkedHashMap<>();
        }
        filters.put(checker, label);
    }

    /**
     * Method to be overridden by data elements requiring access to the advanced services
     * @param eventFrom event to extract data from
     * @param sid invocation data
     * @return value of the data element, or null if it is not found.
     */
    protected T doExtract(EType eventFrom, SidecarInvocationData sid) {
        return doExtract(eventFrom);
    }

    /**
     * Value extraction, wherein the extraction is stateless and requires no other elements.
     * @param eventFrom source element
     * @return value of the data element, or null if it was not found in the API call processing event.
     */
    protected T doExtract(EType eventFrom) {
        throw new UnsupportedOperationException("Data extraction has not been implemented correctly.");
    }

    public DataElementRelevance getOnNoFilterMatched() {
        return onNoFilterMatched;
    }

    public void setOnNoFilterMatched(DataElementRelevance onNoFilterMatched) {
        this.onNoFilterMatched = onNoFilterMatched;
    }

    @Override
    public DataElementRelevance extract(EType eventFrom, SidecarInvocationData sid) {
        T value = doExtract(eventFrom, sid);

        boolean wasAccepted = false;
        String latestAcceptedLabel = null;

        if (filters != null && filters.size() > 0) {

            for (Map.Entry<Function<T, DataElementRelevance>,String> entry: filters.entrySet()) {
                final DataElementRelevance v = entry.getKey().apply(value);
                switch (v) {
                    case ClientError:
                    case Noop:
                    case InternalFault:
                        return v;
                    case Invoke:
                        wasAccepted = true;
                        latestAcceptedLabel = entry.getValue();
                    case Ignored:
                        continue;
                    default:
                        return InternalFault;
                }
            }
        } else {
            // No filters, the element is accepted.
            wasAccepted = true;
        }

        DataElementRelevance finalConclusion = wasAccepted ? Invoke : onNoFilterMatched;

        if (finalConclusion == Invoke) {
            if (converter != null) {
                // Value passed to the sidecar can be normalized, e.g. lowercased, uppercased,
                // orderd, etc.
                T valueForSidecar = normalizer != null ? normalizer.apply(value, sid) : value;
                converter.accept(valueForSidecar, sid);
            }

            if (latestAcceptedLabel != null) {
                filterMatchReporter.report(latestAcceptedLabel, value, sid);
            }
        }

        return finalConclusion;
    }

    public void reportFilterMatchUsing(FilterMatchReporter<T> filterMatcher) {
        this.filterMatchReporter = filterMatcher;
    }

    private void reportFilterMatch(String label, T value, SidecarInvocationData into) {
        if (value != null) {
            Map<String, Object> target = allocOrGetParams(into.getInput());

            if (label != null) {
                target.put(String.format("%sLabel", getElementSpec().getElementName()), label);
            }
        }
    }


    @Override
    public int getSalience() {
        return salience;
    }

    /**
     * Add a filter without the label
     * @param filterFunction filtering function determining the relevance of the data element
     *                       to the sidecar invocation.
     */
    public void addFilter(Function<T, DataElementRelevance> filterFunction) {
        addFilter(filterFunction, null);
    }
}
