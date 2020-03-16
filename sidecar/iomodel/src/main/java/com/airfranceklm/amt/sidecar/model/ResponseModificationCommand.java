package com.airfranceklm.amt.sidecar.model;

public interface ResponseModificationCommand extends CallModificationCommand {
    Integer getStatusCode();
}
