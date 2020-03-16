package com.airfranceklm.amt.sidecar.elements;

/**
 * Exception indicating that the configuration is trying to use filter that doesn't exist or is not compatible with
 * the element type.
 */
public class UnknownFilterAlgorithmException extends DataElementException {

    private String algorithm;

    public UnknownFilterAlgorithmException(String elemName, String algorithm) {
        super(elemName);
        this.algorithm = algorithm;
    }

    public UnknownFilterAlgorithmException(String elemName, String message, String algorithm) {
        super(elemName, message);
        this.algorithm = algorithm;
    }

    public UnknownFilterAlgorithmException(String elemName, String message, Throwable cause, String algorithm) {
        super(elemName, message, cause);
        this.algorithm = algorithm;
    }

    public UnknownFilterAlgorithmException(String elemName, Throwable cause, String algorithm) {
        super(elemName, cause);
        this.algorithm = algorithm;
    }

    public UnknownFilterAlgorithmException(String elemName, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String algorithm) {
        super(elemName, message, cause, enableSuppression, writableStackTrace);
        this.algorithm = algorithm;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
