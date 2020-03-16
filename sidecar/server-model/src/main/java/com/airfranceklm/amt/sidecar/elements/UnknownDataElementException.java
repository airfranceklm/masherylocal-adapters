package com.airfranceklm.amt.sidecar.elements;

public class UnknownDataElementException extends DataElementException {

    public UnknownDataElementException(String elemName) {
        super(elemName);
    }

    public UnknownDataElementException(String elemName, String message) {
        super(elemName, message);
    }

    public UnknownDataElementException(String elemName, String message, Throwable cause) {
        super(elemName, message, cause);
    }

    public UnknownDataElementException(String elemName, Throwable cause) {
        super(elemName, cause);
    }

    public UnknownDataElementException(String elemName, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(elemName, message, cause, enableSuppression, writableStackTrace);
    }
}
