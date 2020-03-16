package com.airfranceklm.amt.sidecar.elements;

import static com.airfranceklm.amt.sidecar.elements.ElementScope.Both;
import static com.airfranceklm.amt.sidecar.elements.ElementScope.PostProcessor;

/**
 * List of standard elements that require a parameter to function.
 */
public enum ParameterizedStringElement implements ElementSpec {

    RequestHeader(StandardElements.RequestHeader, Both),
    ResponseHeader(StandardElements.ResponseHeader, PostProcessor),
    RequestHeadersSkipping(StandardElements.RequestHeadersSkipping, Both),
    ResponseHeadersSkipping(StandardElements.ResponseHeadersSkipping, PostProcessor),
    EAV(StandardElements.EAV, Both),
    PackageKeyEAV(StandardElements.PackageKeyEAV, Both);

    private String elementName;
    private ElementScope scope;

    ParameterizedStringElement(String l, ElementScope scope) {
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
        return String.class;
    }
}
