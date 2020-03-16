package com.airfranceklm.amt.sidecar.model.alcp.alg.hspav1;

/**
 * Exception indicating that the {@link RequestMessageHeader} is not valid, e.g. it contains a claim that cannot
 * be verified.
 */
public class InvalidRequestMessageHeaderException extends Exception {
    public InvalidRequestMessageHeaderException(String message) {
        super(message);
    }
}
