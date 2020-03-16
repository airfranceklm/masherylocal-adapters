package com.airfranceklm.amt.sidecar.elements;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
class ElementWithParameterSpecImpl implements ElementWithParameterSpec {

    @Getter
    private String elementName;
    @Getter
    private String elementParameter;
    @Getter
    private ElementScope elementScope;
    @Getter
    private Class<?> elementClass;

    public ElementWithParameterSpecImpl(String elementName) {
        this.elementName = elementName;
    }
}
