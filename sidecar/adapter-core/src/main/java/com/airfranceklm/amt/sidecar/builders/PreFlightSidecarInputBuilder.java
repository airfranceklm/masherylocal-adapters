package com.airfranceklm.amt.sidecar.builders;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.model.PreFlightSidecarConfiguration;
import com.airfranceklm.amt.sidecar.elements.*;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;

import static com.airfranceklm.amt.sidecar.model.SidecarInputPoint.Preflight;
import static com.airfranceklm.amt.sidecar.model.SidecarSynchronicity.RequestResponse;

public class PreFlightSidecarInputBuilder extends AbstractSidecarInputBuilder<PreProcessEvent, PreFlightSidecarConfiguration> {

    public PreFlightSidecarInputBuilder(ElementsFactory factory, PreFlightSidecarConfiguration configuration) {
        super(factory, configuration);
    }

    @Override
    protected void initializeInput(SidecarInvocationData sid, PreProcessEvent eventFrom) {
        sid.setIdempotentAware(true);

        SidecarInput input = sid.getInput();
        input.setEndpointId(eventFrom.getEndpoint().getExternalID());
        input.setServiceId(eventFrom.getEndpoint().getAPI().getExternalID());

        input.setSynchronicity(RequestResponse);
        input.setPoint(Preflight);

        if (getConfiguration().getSidecarParams() != null) {
            input.setParams(getConfiguration().getSidecarParams());
        }
    }

    @Override
    protected DataElement<? super PreProcessEvent, ?> createElement(ElementsFactory factory, ElementDemand config) throws DataElementException {
        // TODO: Check the elements that are not allowed for pre-processor.
        return factory.produceForPreProcessor(config);
    }
}
