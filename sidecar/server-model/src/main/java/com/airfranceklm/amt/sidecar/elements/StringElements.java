package com.airfranceklm.amt.sidecar.elements;


import static com.airfranceklm.amt.sidecar.elements.ElementScope.Both;
import static com.airfranceklm.amt.sidecar.elements.ElementScope.PreProcessor;

public enum StringElements implements ElementSpec {

    PackageKey(StandardElements.PackageKey, Both)
    , TokenGrantType(StandardElements.TokenGrantType, Both)
    , TokenScope(StandardElements.TokenScope, Both)
    , TokenUserContext(StandardElements.TokenUserContext, Both)
    , RoutingHost(StandardElements.RoutingHost, PreProcessor)
    , HttpVerb(StandardElements.HttpVerb, Both)
    , ResourcePath(StandardElements.ResourcePath, Both)
    , RemoteAddress(StandardElements.RemoteAddress, Both)
    , MessageId(StandardElements.MessageId, Both)

            ;

    StringElements(String elementName, ElementScope scope) {
        this.elementName = elementName;
        this.scope = scope;
    }

    private String elementName;
    private ElementScope scope;


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
        return String.class;
    }


}
