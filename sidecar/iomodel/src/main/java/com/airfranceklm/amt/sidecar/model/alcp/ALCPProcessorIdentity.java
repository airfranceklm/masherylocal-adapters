package com.airfranceklm.amt.sidecar.model.alcp;

import com.airfranceklm.amt.sidecar.identity.ClusterIdentity;
import com.airfranceklm.amt.sidecar.identity.PartyKeyDescriptor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Identify of this processor for the application-level call protection.
 */
@AllArgsConstructor
public class ALCPProcessorIdentity implements ClusterIdentity {

    @Getter
    private String areaId;
    @NonNull
    private PartyKeyDescriptor key;

    // ------------------------------------------------
    // Cluster Identity implementation.
    @Override
    public RSAPrivateKey getPrivateKey() {
        return key.getRsaKey() != null ? key.getRsaKey().getPrivateKey() : null;
    }

    @Override
    public RSAPublicKey getPublicKey() {
        return key.getRsaKey() != null ? key.getRsaKey().getPublicKey() : null;
    }

    @Override
    public String getPasswordSalt() {
        return key.getPasswordSalt();
    }

    @Override
    public String getKeyId() {
        return key.getKeyIdentifier() != null ? key.getKeyIdentifier().getKeyId() : null;
    }
}
