package com.airfranceklm.amt.sidecar.elements;

public class DataElementException extends Exception {

    private String elementName;

    public DataElementException(String elemName) {
        super();
        this.elementName = elemName;
    }

    public DataElementException(String elemName, String message) {
        super(message);
        this.elementName = elemName;
    }

    public DataElementException(String elemName, String message, Throwable cause) {
        super(message, cause);
        this.elementName = elemName;
    }

    public DataElementException(String elemName, Throwable cause) {
        super(cause);
        this.elementName = elemName;
    }

    protected DataElementException(String elemName, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.elementName = elemName;
    }

    public String getElementName() {
        return elementName;
    }
}
