package com.airfranceklm.amt.sidecar.builders;

import com.airfranceklm.amt.sidecar.ProcessorServices;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.elements.*;
import com.airfranceklm.amt.sidecar.filters.*;
import com.airfranceklm.amt.sidecar.model.*;
import com.airfranceklm.amt.sidecar.identity.ClusterIdentity;
import com.airfranceklm.amt.sidecar.identity.CounterpartIdentity;
import com.airfranceklm.amt.sidecar.model.alcp.MasheryALCPSide;
import com.airfranceklm.amt.sidecar.stack.SidecarStack;
import com.airfranceklm.amt.sidecar.stack.SidecarStackConfiguration;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static com.airfranceklm.amt.sidecar.model.ElementVisibility.Filters;
import static com.airfranceklm.amt.sidecar.model.MaxPayloadSizeExcessAction.BlockSidecarCall;
import static com.airfranceklm.amt.sidecar.elements.DataElementFilterIntent.MatchDescopes;
import static com.airfranceklm.amt.sidecar.elements.DataElementFilterIntent.MatchProhibits;
import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.*;
import static com.airfranceklm.amt.sidecar.model.SidecarSynchronicity.RequestResponse;

public abstract class AbstractSidecarInputBuilder<EType extends ProcessorEvent, SCType extends SidecarConfiguration> {

    @Getter
    private ElementsFactory factory;
    @Getter
    private SCType configuration;

    private List<DataElement<? super EType, ?>> elements;

    @Getter @Setter
    private SidecarStack stack;
    @Getter @Setter
    private SidecarStackConfiguration stackConfiguration;

    @Getter @Setter
    private ClusterIdentity selfIdentity;

    @Getter @Setter
    private MasheryALCPSide<?,?> alcp;

    @Getter @Setter
    private CounterpartIdentity sidecarIdentity;

    @Getter @Setter
    private ProcessorServices processorServices;

    public AbstractSidecarInputBuilder(ElementsFactory factory, SCType configuration) {
        this.factory = factory;
        this.configuration = configuration;
        this.elements = new ArrayList<>();

        if (configuration.getElements() != null) {
            try {
                createDataElements();
            } catch (DataElementException ex) {
                configuration.incrementError();
                configuration.addMessage(String.format("Demanded elements cannot be created: %s reported %s", ex.getElementName(), ex.getMessage()));
            }
        }
    }

    void useConfiguration(SCType configuration) {
        this.configuration = configuration;
    }

    public SidecarInvocationData build(EType event) {
        SidecarInvocationData retVal = new SidecarInvocationData(new SidecarInput(),
                stack,
                stackConfiguration);

        retVal.setApplicationLevelCallProtection(this.alcp);

        retVal.setCache(event.getCache());
        retVal.setDebugContext(event.getDebugContext());
        retVal.setServiceId(event.getEndpoint().getAPI().getExternalID());
        retVal.setEndpointId(event.getEndpoint().getExternalID());

        initializeInput(retVal, event);

        for (DataElement<? super EType, ?> de: elements) {
            DataElementRelevance relevance = de.extract(event, retVal);
            switch (relevance) {
                case ServiceNotReady:
                case ClientError:
                case InternalFault:
                case Noop:
                    retVal.setRelevance(relevance);
                    return retVal;
                case Invoke:
                case Ignored:
                default:
                    // Continue building elements for the accepted elements.
                    break;
            }
        }

        retVal.setRelevance(Invoke);
        return retVal;
    }

    public boolean isFailsafe() {
        return configuration.isFailsafe();
    }



    protected abstract void initializeInput(SidecarInvocationData sid, EType eventFrom);

    /**
     * Initializes common parameters of the sidecar
     * @param sid invocation data
     * @param eventFrom source event
     * @param point invocation point
     */
    protected void initializeCommon(SidecarInvocationData sid, ProcessorEvent eventFrom, SidecarInputPoint point) {

        SidecarInput input = sid.getInput();
        input.setEndpointId(eventFrom.getEndpoint().getExternalID());
        input.setServiceId(eventFrom.getEndpoint().getAPI().getExternalID());

        input.setPoint(point);

        // The builder must always indicate synchronicity. In case configuration doesn't explicitly define it,
        // then the Request-Response is implied.
        final SidecarSynchronicity cfgSyc = configuration.getSynchronicity();
        input.setSynchronicity(cfgSyc != null ? cfgSyc : RequestResponse);


        if (getConfiguration().getSidecarParams() != null) {
            input.setParams(getConfiguration().getSidecarParams());
        }
    }

    protected abstract DataElement<? super EType, ?> createElement(ElementsFactory factory, ElementDemand config) throws DataElementException;

    protected void createDataElements() throws DataElementException {

        boolean suppressHeaders = configuration.popElement(SyntheticElements.SuppressHeaders.getElementName(), null) != null;

        configuration.getElements().forEach((ed) -> {
            if (suppressHeaders && ParameterizedStringElement.RequestHeader.getElementName().equals(ed.getName())) {
                ed.setVisibility(Filters);
            }

            try {
                DataElement<? super EType, ?> elem = createElement(factory, ed);
                elements.add(elem);
            } catch (DataElementException ex) {
                // add an error message
                configuration.incrementError();
                configuration.addMessage(String.format("Data element %s(%s) could not have been created",
                        ed.getName(),
                        ed.getParameter()));
            }
        });

        applyLimiters();
    }

    protected void applyLimiters() throws DataElementException {
    }

    public void configurationCompleted() {
        // If configuration has errors, then we need to add a breaker
        // to prevent
        if (configuration.hasErrors()) {
            elements.add(new ServiceBreakerElement(configuration.getMessages()));
        }

        // Sort according ot the data salience.
        elements.sort(Comparator.comparingInt(DataElement::getSalience));
    }

    protected void addPayloadSizeLimiter(Function<String, DataElement<? super EType, Long>> creator, MaxPayloadSizeSetting pReqSizeCap) throws DataElementException {
        MaxPayloadSizeSetting reqSizeCap = MaxPayloadSizeSetting.effectiveFrom(pReqSizeCap);

        DataElementFilterIntent intent = reqSizeCap.getAction() == BlockSidecarCall ?
                MatchProhibits : MatchDescopes;

        DataElement<? super EType, Long> elem = creator.apply(null);

        Function<Long,Boolean> comparator = NumericAlgorithmsFactory.gt(reqSizeCap.getLimit())::test;
        StandardElementsFactory.addFilterTo(elem, comparator, intent, null);

        elements.add(elem);
    }
}
