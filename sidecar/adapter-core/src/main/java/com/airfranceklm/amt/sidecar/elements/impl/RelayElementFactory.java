package com.airfranceklm.amt.sidecar.elements.impl;

import com.airfranceklm.amt.sidecar.elements.DataElement;
import com.airfranceklm.amt.sidecar.elements.ElementsFactoryBuilder;
import com.airfranceklm.amt.sidecar.elements.ObjectElements;
import com.airfranceklm.amt.sidecar.elements.SimpleDataElement;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;

import static com.airfranceklm.amt.sidecar.SidecarProcessorConstants.RELAY_CONTEXT_VALUE;
import static com.airfranceklm.amt.sidecar.SidecarProcessorConstants.RELAY_PARAM_NAME;
import static com.airfranceklm.amt.sidecar.elements.DataElementSalience.FAIL_FAST;
import static com.airfranceklm.amt.sidecar.elements.ObjectElements.Relay;

public class RelayElementFactory {

    private static DataElement<PostProcessEvent, Object> createRelay(String param) {
        SimpleDataElement<PostProcessEvent, Object> retVal = new SimpleDataElement<>(Relay, FAIL_FAST);
        retVal.extractWith((ppe) -> ppe.getDebugContext().removeEntry(RELAY_CONTEXT_VALUE));
        retVal.convertUsing((o, sid) -> SidecarInput.Accessor.allocOrGetParams(sid.getInput()).put(RELAY_PARAM_NAME, o));

        return retVal;
    }

    public static void fill(ElementsFactoryBuilder b) {
        b.addPostProcessorElement(Relay, RelayElementFactory::createRelay);
    }
}
