package com.airfranceklm.amt.sidecar.builders;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.model.PreProcessorSidecarConfiguration;
import com.airfranceklm.amt.sidecar.elements.*;
import com.airfranceklm.amt.sidecar.elements.impl.PayloadElementsFactory;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;

import static com.airfranceklm.amt.sidecar.elements.PayloadElements.RequestPayload;
import static com.airfranceklm.amt.sidecar.model.SidecarInputPoint.PreProcessor;

public class PreProcessSidecarInputBuilder extends AbstractSidecarInputBuilder<PreProcessEvent, PreProcessorSidecarConfiguration> {

    public PreProcessSidecarInputBuilder(ElementsFactory factory, PreProcessorSidecarConfiguration configuration) {
        super(factory, configuration);
    }

    @Override
    protected void initializeInput(SidecarInvocationData sid, PreProcessEvent eventFrom) {
        initializeCommon(sid, eventFrom, PreProcessor);

        sid.setIdempotentAware(getConfiguration().isIdempotentAware());
    }

    @Override
    protected DataElement<? super PreProcessEvent, ?> createElement(ElementsFactory factory, ElementDemand config) throws DataElementException {
        return factory.produceForPreProcessor(config);
    }

    @Override
    protected void applyLimiters() throws DataElementException {
        if (getConfiguration().demandsElement(RequestPayload)) {
            addPayloadSizeLimiter(PayloadElementsFactory::createRequestBodySizeExtractor, getConfiguration().getMaximumRequestPayloadSize());
        }
    }
}
