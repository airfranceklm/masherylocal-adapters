package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import lombok.NonNull;

/**
 * Parameterized data element type where the value is extracted with the chainable extractor.
 *
 * @param <EType> Type of the event to extract from
 * @param <T>     Type of the extracted value
 */
public class ChainedExtractionDataElement<EType extends ProcessorEvent, T> extends AbstractDataElement<EType, T> {

    private String parameter;
    private ChainableExtractor<EType, T> extractor;

    public ChainedExtractionDataElement(@NonNull ElementSpec ne, String parameter, int salience) {
        super(ne, salience);
        this.parameter = parameter;
    }

    @Override
    public String describe(T value) {
        return String.format("%d@%s(%s) >= %s", getSalience(), getElementSpec().getElementName(), getParameterForDescribe(), value);
    }

    private String getParameterForDescribe() {
        return parameter != null ? parameter : "<no.parameter>";
    }

    @Override
    protected T doExtract(EType eventFrom, SidecarInvocationData sid) {
        if (extractor != null) {
            return extractor.extract(eventFrom, sid);
        } else {
            return null;
        }
    }

    public void extractUsing(ChainableExtractor<EType, T> extractor) {
        this.extractor = extractor;
    }
}
