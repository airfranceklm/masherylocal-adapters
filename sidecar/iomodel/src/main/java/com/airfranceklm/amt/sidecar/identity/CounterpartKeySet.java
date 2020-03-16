package com.airfranceklm.amt.sidecar.identity;

import lombok.extern.slf4j.Slf4j;

import java.security.PublicKey;

/**
 * Key set for the sidecar counterparts.
 */
@Slf4j
public class CounterpartKeySet extends KeySet<CounterpartKeyDescriptor<RSAPublicKeyDescriptor>> {

    public void rehydrateKeys() {
        if (getKeys() != null) getKeys().forEach((k) -> {
            RSAPublicKeyDescriptor pd = k.getRsaKey();

            if (pd != null && pd.getPublicKeyMaterial() != null) {
                PublicKey pk = pd.getPublicKey();
                if (pk == null) {
                    log.error(String.format("Public key cannot be recovered for key %s", k.getKeyIdentifier()));
                }
            }
        });
    }
}
