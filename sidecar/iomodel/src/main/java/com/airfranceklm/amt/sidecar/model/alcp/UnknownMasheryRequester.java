package com.airfranceklm.amt.sidecar.model.alcp;

/**
 * Exception denoting that Mashery requester is not known, and the policy does not allow processing
 * the messages from unverified sources.
 */
public class UnknownMasheryRequester extends Exception {
    public UnknownMasheryRequester(String message) {
        super(message);
    }
}
