package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;

/**
 * Factory that will supply the data elements the sidecar input will be built from.
 */
public interface ElementsFactory {

    DataElement<? super PreProcessEvent, ?> produceForPreProcessor(ElementDemand cfg)
            throws DataElementException;

    DataElement<? super PostProcessEvent, ?> produceForPostProcessor(ElementDemand cfg)
            throws DataElementException;


}
