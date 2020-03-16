package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.SidecarInputHTTPMessage;

/**
 * List of standard elements
 */
public enum PayloadElements implements ElementSpec {

    RequestPayload(StandardElements.RequestPayload, ElementScope.Both),
    RequestPayloadFragment(StandardElements.RequestPayloadFragment, ElementScope.Both),
    ResponsePayload(StandardElements.ResponsePayload, ElementScope.PostProcessor),
    ResponsePayloadFragment(StandardElements.ResponsePayloadFragment, ElementScope.PostProcessor)
    ;

    private String elementName;
    private ElementScope scope;

    PayloadElements(String l, ElementScope scope) {
        this.elementName = l;
        this.scope = scope;
    }

    public String getElementName() {
        return elementName;
    }

    @Override
    public ElementScope getElementScope() {
        return scope;
    }

    @Override
    public Class<?> getElementClass() {
        return SidecarInputHTTPMessage.class;
    }


}
