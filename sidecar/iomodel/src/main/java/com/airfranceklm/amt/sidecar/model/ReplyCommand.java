package com.airfranceklm.amt.sidecar.model;

import java.util.Map;

/**
 * Command from the sidecar to reply back to the requester.
 */
public interface ReplyCommand extends PayloadCarrier {

    Integer getStatusCode();

}
