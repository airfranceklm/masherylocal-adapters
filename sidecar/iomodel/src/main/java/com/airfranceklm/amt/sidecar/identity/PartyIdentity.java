package com.airfranceklm.amt.sidecar.identity;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Identity of the party to the algorithm, identified through a concrete sub-interface of this interface.
 */
public interface PartyIdentity {
    /**
     * Private key that will be used for the encrypting and key establishment
     * @return instance of the private key
     */
    RSAPrivateKey getPrivateKey();

    /**
     * Public key
     * @return
     */
    RSAPublicKey getPublicKey();

    /**
     * Salt part that will be applied to the random-generated one-time password. The purpose of the salt is to make
     * the high-security protocol even harder to break.
     *
     * @return a string containing the salt part for the variable-bit password.
     */
    String getPasswordSalt();

    /**
     * Returns the (global) ID of this key where a counterpart will be able to find it's key
     * @return string representing the key Id of this implementation.
     */
    String getKeyId();
}
