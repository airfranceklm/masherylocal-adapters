package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.*;

import static com.airfranceklm.amt.sidecar.elements.ElementScope.*;

public enum ObjectElements implements ElementSpec {

    Routing(StandardElements.Routing, PreProcessor, SidecarInputRouting.class)
    , Token(StandardElements.Token, Both, SidecarInputToken.class)
    , FullToken(StandardElements.FullToken, Both, SidecarInputToken.class)
    , Operation(StandardElements.Operation, Both, SidecarInputOperation.class)
    , FullOperation(StandardElements.FullOperation, Both, SidecarInputOperation.class)
    , EndpointIdentification(StandardElements.EndpointIdentification, Both, SidecarInput.class)
    ,  UserContextField(StandardElements.TokenUserContextField, Both, Object.class)
    ,  Relay(StandardElements.Relay, PostProcessor, Object.class)
    , AllRequestHeaders(StandardElements.AllRequestHeaders, Both, SidecarInputHTTPMessage.class)
    , AllResponseHeaders(StandardElements.AllResponseHeaders, PostProcessor, SidecarInputHTTPResponseMessage.class)
    ;

    ObjectElements(String elementName, ElementScope scope, Class<?> cls) {
        this.elementName = elementName;
        this.scope = scope;
        this.elementClass = cls;
    }

    private String elementName;
    private ElementScope scope;
    private Class<?> elementClass;

    @Override
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
