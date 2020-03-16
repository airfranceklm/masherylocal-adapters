package com.airfranceklm.amt.sidecar.model;

/**
 * Definition of what should happen if the payload of the message being processed will exceed the defined limits.
 */
public enum MaxPayloadSizeExcessAction {
    NoopSidecarCall, BlockSidecarCall
}
