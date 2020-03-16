package com.airfranceklm.amt.sidecar.identity;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Representation of the counterpart identity in the exchange.
 */
public interface CounterpartIdentity {
    String getPasswordSalt();
    RSAPublicKey getPublicKey();
}
