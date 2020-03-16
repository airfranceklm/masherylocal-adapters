package com.airfranceklm.amt.sidecar.identity;

/**
 * Identification of the identity of the cluster that will be used for the encryption and decryption operations.
 */
public interface ClusterIdentity extends PartyIdentity {
    String getAreaId();
}
