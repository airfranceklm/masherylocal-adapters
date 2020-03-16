package com.airfranceklm.amt.sidecar.elements;

import static com.airfranceklm.amt.sidecar.elements.ElementScope.Both;
import static com.airfranceklm.amt.sidecar.elements.ElementScope.PostProcessor;

public enum NumericElements implements ElementSpec {

    ResponseCode(StandardElements.ResponseCode, Both, Integer.class)
    , RequestPayloadSize(StandardElements.RequestPayloadSize, Both, Long.class)
    , ResponsePayloadSize(StandardElements.ResponsePayloadSize, PostProcessor, Long.class)
    ;

    private String elementName;
    private ElementScope scope;
    private Class<?> elementClass;

    NumericElements(String l, ElementScope scope, Class<?> elemCls) {
        this.elementName = l;
        this.scope = scope;
        this.elementClass = elemCls;
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
        return elementClass;
    }


}
