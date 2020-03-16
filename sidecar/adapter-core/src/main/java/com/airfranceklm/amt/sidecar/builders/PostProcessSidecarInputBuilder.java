package com.airfranceklm.amt.sidecar.builders;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.model.PostProcessorSidecarConfiguration;
import com.airfranceklm.amt.sidecar.elements.*;
import com.airfranceklm.amt.sidecar.elements.impl.PayloadElementsFactory;
import com.airfranceklm.amt.sidecar.model.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;

import static com.airfranceklm.amt.sidecar.elements.PayloadElements.ResponsePayload;

public class PostProcessSidecarInputBuilder extends AbstractSidecarInputBuilder<PostProcessEvent, PostProcessorSidecarConfiguration> {

    public PostProcessSidecarInputBuilder(ElementsFactory factory, PostProcessorSidecarConfiguration configuration) {
        super(factory, configuration);
    }

    @Override
    protected void initializeInput(SidecarInvocationData sid, PostProcessEvent eventFrom) {
        initializeCommon(sid, eventFrom, SidecarInputPoint.PostProcessor);
    }

    @Override
    protected DataElement<? super PostProcessEvent,?> createElement(ElementsFactory factory, ElementDemand config) throws DataElementException {
        return factory.produceForPostProcessor(config);
    }

    @Override
    protected void applyLimiters() throws DataElementException {
        super.applyLimiters();

        if (getConfiguration().demandsElement(ResponsePayload)) {
            addPayloadSizeLimiter(PayloadElementsFactory::createResponseBodySizeExtractor, getConfiguration().getMaximumResponsePayloadSize());
        }
    }
}
