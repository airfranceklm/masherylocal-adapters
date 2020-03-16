package com.airfranceklm.amt.sidecar.elements.impl;

import com.airfranceklm.amt.sidecar.elements.*;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import com.mashery.trafficmanager.model.core.ExtendedAttributes;

import static com.airfranceklm.amt.sidecar.elements.DataElementSalience.FAIL_FAST;
import static com.airfranceklm.amt.sidecar.elements.ParameterizedStringElement.EAV;
import static com.airfranceklm.amt.sidecar.elements.ParameterizedStringElement.PackageKeyEAV;
import static com.airfranceklm.amt.sidecar.model.SidecarInput.Accessor.allocOrGetEAVs;
import static com.airfranceklm.amt.sidecar.model.SidecarInput.Accessor.allocOrGetPackageKeyEAVs;

public class EAVElementsFactory {
    private static DataElement<ProcessorEvent,String> createApplicationEAV(String param) {
        SimpleDataElement<ProcessorEvent, String> retVal = new SimpleDataElement<>(EAV, FAIL_FAST);

        retVal.extractWith((pe) -> {
            final ExtendedAttributes appAttrs = pe.getKey().getApplication().getExtendedAttributes();
            if (appAttrs != null) {
                return appAttrs.getValue(param);
            } else {
                return null;
            }
        });

        retVal.convertUsing((v, sid) -> {
            if (v != null) {
                allocOrGetEAVs(sid.getInput()).put(param, v);
            }
        });

        retVal.reportFilterMatchUsing((label, v, sid) -> {
            CommonElementsFactory.reportParameterizedFilterMatch(label, v, sid, EAV.getElementName(), param);
        });

        return retVal;
    }

    private static DataElement<ProcessorEvent, String> createPackageKeyEAV(String param) {
        SimpleDataElement<ProcessorEvent, String> retVal = new SimpleDataElement<>(PackageKeyEAV, FAIL_FAST);

        retVal.extractWith((pe) -> {
            final ExtendedAttributes extendedAttributes = pe.getKey().getExtendedAttributes();
            if (extendedAttributes != null) {
                return extendedAttributes.getValue(param);
            } else {
                return null;
            }
        });
        retVal.convertUsing((v, sid) -> {
            if (v != null) {
                allocOrGetPackageKeyEAVs(sid.getInput()).put(param, v);
            }
        });

        retVal.reportFilterMatchUsing((label, v, sid) -> {
            CommonElementsFactory.reportParameterizedFilterMatch(label, v, sid, PackageKeyEAV.getElementName(), param);
        });

        return retVal;
    }

    public static void fill(ElementsFactoryBuilder b) {
        b.addCommonElement(EAV, EAVElementsFactory::createApplicationEAV);
        b.addCommonElement(PackageKeyEAV, EAVElementsFactory::createPackageKeyEAV);
    }
}
