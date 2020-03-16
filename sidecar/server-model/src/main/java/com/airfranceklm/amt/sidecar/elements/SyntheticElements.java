package com.airfranceklm.amt.sidecar.elements;

/**
 * Elements which mention changes the behaviour of other elements.
 */
public enum SyntheticElements implements ElementSpec {

    SuppressHeaders(StandardElements.NoHeaders)
    , KillSwitch(StandardElements.KillSwitch)
    , ServiceBreaker(StandardElements.ServiceBreaker);

    private String elementName;

    SyntheticElements(String l) {
        this.elementName = l;
    }

    public String getElementName() {
        return elementName;
    }

    @Override
    public ElementScope getElementScope() {
        return ElementScope.Both;
    }

    @Override
    public Class<?> getElementClass() {
        return Void.class;
    }


}
