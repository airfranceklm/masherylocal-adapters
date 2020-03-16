package com.airfranceklm.amt.sidecar.elements.impl;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.elements.*;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.mashery.http.server.HTTPServerRequest;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import com.mashery.trafficmanager.model.core.APICall;
import com.mashery.trafficmanager.model.core.ApplicationRequest;

import java.util.Map;
import java.util.function.Function;

import static com.airfranceklm.amt.sidecar.elements.DataElementSalience.*;
import static com.airfranceklm.amt.sidecar.model.SidecarInput.Accessor.allocOrGetParameterGroup;
import static com.airfranceklm.amt.sidecar.model.SidecarInput.Accessor.allocOrGetResponse;

public class CommonElementsFactory {


    static final String MASH_MSG_ID_HEADER = "X-Mashery-Message-ID";


    private static DataElement<PostProcessEvent, Integer> createResponseCode(String param) {
        SimpleDataElement<PostProcessEvent, Integer> retVal = new SimpleDataElement<>(NumericElements.ResponseCode, DATA_STRUCTS);
        retVal.extractWith((ppe) -> ppe.getClientResponse().getStatusCode());
        retVal.convertUsing((code, sid) -> allocOrGetResponse(sid.getInput(), code));

        return retVal;
    }

    private static DataElement<ProcessorEvent, Void> createKillSwitch(String param) {
        return new KillSwitchElement();
    }

    private static DataElement<ProcessorEvent, String> createPackageKey(String param) {
        SimpleDataElement<ProcessorEvent, String> retVal = new SimpleDataElement<>(StringElements.PackageKey, FAIL_FAST);

        retVal.extractWith((ppe) -> {
            if (ppe.getKey() != null) {
                return ppe.getKey().getExternalID();
            } else {
                return null;
            }
        });
        retVal.convertUsing((pk, sid) -> sid.getInput().setPackageKey(pk));

        return retVal;
    }

    private static DataElement<ProcessorEvent, ProcessorEvent> createEndpointIdentification(String param) {
        SimpleDataElement<ProcessorEvent, ProcessorEvent> retVal = new SimpleDataElement<>(ObjectElements.EndpointIdentification, MANDATORY_OPERATIONS);

        retVal.extractWith((ppe) -> ppe);
        retVal.convertUsing((ppe, sid) -> {
            sid.getInput().setServiceId(ppe.getEndpoint().getAPI().getExternalID());
            sid.getInput().setEndpointId(ppe.getEndpoint().getExternalID());
        });

        return retVal;
    }

    private static DataElement<PreProcessEvent, String> createMasheryMessageIdForPreProcessor(String param) {
        SimpleDataElement<PreProcessEvent, String> retVal = new SimpleDataElement<>(StringElements.MessageId, MANDATORY_OPERATIONS);

        retVal.extractWith((ppe) -> {
            String messageId = ppe.getClientRequest().getHeaders().get(MASH_MSG_ID_HEADER);
            if (messageId == null) {
                messageId = ppe.getCallContext().getResponse().getHTTPResponse().getHeaders().get(MASH_MSG_ID_HEADER);
            }
            return messageId;
        });

        retVal.convertUsing((msgId, sid) -> sid.getInput().setMasheryMessageId(msgId));

        // No filtering is possible here.
        return retVal;
    }

    private static DataElement<PostProcessEvent, String> createMasheryMessageIdForPostProcessor(String param) {
        SimpleDataElement<PostProcessEvent, String> retVal = new SimpleDataElement<>(StringElements.MessageId, MANDATORY_OPERATIONS);

        retVal.extractWith((ppe) -> ppe.getCallContext().getResponse().getHTTPResponse().getHeaders().get(MASH_MSG_ID_HEADER));

        retVal.convertUsing((msgId, sid) -> sid.getInput().setMasheryMessageId(msgId));

        // No filtering is possible here.
        return retVal;
    }

    private static DataElement<ProcessorEvent, String> createRemoteAddress(String param) {
        SimpleDataElement<ProcessorEvent, String> retVal = new SimpleDataElement<>(StringElements.RemoteAddress, FAIL_FAST);

        retVal.extractWith((ppe) -> fromAPIClientRequest(ppe, HTTPServerRequest::getRemoteAddr));
        retVal.convertUsing((remoteAddr, sid) -> sid.getInput().setRemoteAddress(remoteAddr));

        return retVal;
    }

    private static <T> T fromAPIClientRequest(ProcessorEvent pe, Function<HTTPServerRequest, T> converter) {
        // TODO: this might be further simplified with null-guard functions.
        if (pe.getCallContext() != null) {
            APICall ctx = pe.getCallContext();
            if (ctx.getRequest() != null) {
                ApplicationRequest ar = ctx.getRequest();
                if (ar != null && ar.getHTTPRequest() != null) {
                    return converter.apply(ar.getHTTPRequest());
                }
            }
        }

        return null;
    }

    public static void reportParameterizedFilterMatch(String label, String v, SidecarInvocationData sid, String elemName, String param) {
        if (v != null) {
            final Map<String, Object> pGroup = allocOrGetParameterGroup(sid.getInput(), elemName);

            if (pGroup != null) {
                pGroup.put(param, v);

                if (label != null) {
                    pGroup.put(String.format("%sLabel", param), label);
                }
            }
        }
    }

    private static class KillSwitchElement implements DataElement<ProcessorEvent, Void> {

        @Override
        public DataElementRelevance extract(ProcessorEvent eventFrom, SidecarInvocationData into) {
            return DataElementRelevance.InternalFault;
        }

        @Override
        public int getSalience() {
            return OBVIOUS_ERRORS;
        }

        @Override
        public ElementSpec getElementSpec() {
            return SyntheticElements.KillSwitch;
        }
    }

    public static void fill(ElementsFactoryBuilder builder) {
        builder.addCommonElement(StringElements.RemoteAddress, CommonElementsFactory::createRemoteAddress);
        builder.addCommonElement(StringElements.PackageKey, CommonElementsFactory::createPackageKey);
        builder.addCommonElement(ObjectElements.EndpointIdentification, CommonElementsFactory::createEndpointIdentification);

        builder.addPreProcessorElement(StringElements.MessageId, CommonElementsFactory::createMasheryMessageIdForPreProcessor);
        builder.addPostProcessorElement(StringElements.MessageId, CommonElementsFactory::createMasheryMessageIdForPostProcessor);

        builder.addPostProcessorElement(NumericElements.ResponseCode, CommonElementsFactory::createResponseCode);

        builder.addCommonElement(SyntheticElements.KillSwitch, CommonElementsFactory::createKillSwitch);
    }


}
