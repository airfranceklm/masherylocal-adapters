package com.airfranceklm.amt.sidecar.elements;

import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import lombok.NonNull;

import java.util.function.Function;

/**
 * Base implementation of a simple data element (such as, e.g. a remote address) that doesn't support any
 * parametrization and can be extracted without any special means.
 * @param <EType> type of the event
 * @param <T> type of the data element
 */
public class SimpleDataElement<EType extends ProcessorEvent, T> extends AbstractDataElement<EType, T> {

    public SimpleDataElement(@NonNull ElementSpec elemSpec, int salience) {
        super(elemSpec, salience);
    }

    private Function<EType, T> extractor;

    @Override
    protected T doExtract(EType eventFrom) {
        if (extractor != null) {
            return extractor.apply(eventFrom);
        } else {
            return null;
        }
    }

    @Override
    public String describe(T value) {
        return String.format("%d@%s >= %s", getSalience(), getElementSpec().getElementName(), value);
    }

    public void extractWith(Function<EType, T> extractor) {
        this.extractor = extractor;
    }



}
