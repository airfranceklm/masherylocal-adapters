package com.airfranceklm.amt.sidecar.elements.impl;

import com.airfranceklm.amt.sidecar.elements.*;
import com.airfranceklm.amt.sidecar.elements.DataElement;
import com.airfranceklm.amt.sidecar.model.SidecarInputRouting;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;


import java.net.MalformedURLException;
import java.net.URL;

import static com.airfranceklm.amt.sidecar.elements.DataElementSalience.FAIL_FAST;
import static com.airfranceklm.amt.sidecar.elements.ObjectElements.Routing;
import static com.airfranceklm.amt.sidecar.elements.StringElements.RoutingHost;
import static com.airfranceklm.amt.sidecar.model.SidecarInput.Accessor.allocOrGetParams;

public class RoutingFactory {

    private static DataElement<PreProcessEvent, SidecarInputRouting> createRouting(String param) {
        SimpleDataElement<PreProcessEvent, SidecarInputRouting> retVal = new SimpleDataElement<>(Routing, FAIL_FAST);

        retVal.extractWith((pe) -> {
            SidecarInputRouting extracted = new SidecarInputRouting();
            extracted.setUri(pe.getClientRequest().getURI());
            extracted.setHttpVerb(pe.getClientRequest().getMethod().toLowerCase());

            return extracted;
        });

        retVal.convertUsing((v, sid) -> sid.getInput().setRouting(v));

        return retVal;
    }

    private static DataElement<PreProcessEvent, String> createRoutingHost(String param) {
        SimpleDataElement<PreProcessEvent, String> retVal = new SimpleDataElement<>(RoutingHost, FAIL_FAST);

        retVal.extractWith((pe) -> {
            try {
                URL current = new URL(pe.getClientRequest().getURI());
                return current.getHost();
            } catch (MalformedURLException ex) {
                return null;
            }
        });

        retVal.convertUsing((v, sid) -> allocOrGetParams(sid.getInput()).put(RoutingHost.getElementName(), v));

        return retVal;
    }

    public static void fill(ElementsFactoryBuilder b) {
        b.addPreProcessorElement(RoutingHost, RoutingFactory::createRoutingHost);
        b.addPreProcessorElement(Routing, RoutingFactory::createRouting);
    }
}
