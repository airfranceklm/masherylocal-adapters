package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

/**
 * Data element extraction and validation interface.
 */
public interface DataElement<EType extends ProcessorEvent, T> {

    DataElementRelevance extract(EType eventFrom, SidecarInvocationData into);

    /**
     * Salience, or relative weight of processing this data element: the higher the value, the more expensive the element
     * is to produce. Especially in the contexts where the scope filtering is involved, it is necessary to extract and
     * check cheapest elements first before attempting to produce more expensive ones.
     * <p/>
     * The implementations are advised to prefer constants defined
     * in {@link DataElementSalience} class.
     * @return integer representing weight of processing.
     */
    int getSalience();

    ElementSpec getElementSpec();
}
