package com.airfranceklm.amt.sidecar.elements;

/**
 * Specification for the element
 */
public interface ElementSpec {

    String getElementName();

    ElementScope getElementScope();

    Class<?> getElementClass();
}
