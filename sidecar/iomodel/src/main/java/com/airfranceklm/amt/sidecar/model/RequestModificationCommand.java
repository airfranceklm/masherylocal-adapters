package com.airfranceklm.amt.sidecar.model;

/**
 * Request modification command
 */
public interface RequestModificationCommand extends CallModificationCommand {
    RequestRoutingChangeBean getChangeRoute();
}
