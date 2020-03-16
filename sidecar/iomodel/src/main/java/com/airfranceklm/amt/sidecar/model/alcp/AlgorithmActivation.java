package com.airfranceklm.amt.sidecar.model.alcp;

/**
 * Activation of the protocol.
 */
public enum AlgorithmActivation {
    RequestOnly, ResponseOnly, Bidirectional;

    public boolean requiresRequestEncryption() {
        switch (this) {
            case RequestOnly:
            case Bidirectional:
                return true;
            default:
                return false;
        }

    }

    public boolean requiresResponseDecryption() {
        switch (this) {
            case ResponseOnly:
            case Bidirectional:
                return true;
            default:
                return false;
        }
    }
}
