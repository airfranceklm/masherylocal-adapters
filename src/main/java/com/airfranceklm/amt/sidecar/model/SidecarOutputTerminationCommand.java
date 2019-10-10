package com.airfranceklm.amt.sidecar.model;

import java.util.Map;

/**
 * Termination command.
 */
public interface SidecarOutputTerminationCommand extends PayloadCarrier {
    Integer getCode();

    String getMessage();

    boolean specifiesContentType();

    Map<String, String> getHeaders();

    void setHeaders(Map<String, String> headers);
}
