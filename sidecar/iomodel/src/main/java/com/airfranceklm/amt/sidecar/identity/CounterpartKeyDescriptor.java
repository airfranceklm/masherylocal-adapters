package com.airfranceklm.amt.sidecar.identity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

@Slf4j
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class CounterpartKeyDescriptor<T extends RSAPublicKeyDescriptor> implements IdentifiableKey {

    private CounterpartIdentityImpl counterpartIdent = new CounterpartIdentityImpl();

    @Getter @Setter @JsonProperty("id")
    KeyIdentifier keyIdentifier;

    @Getter @Setter @JsonProperty("rsa")
    T rsaKey;

    @JsonProperty("slt") @Getter @Setter
    private String passwordSalt;

    @JsonProperty("smk") @Getter @Setter
    private String symmetricKey;

    private byte[] encryptionKeyDecoded;

    public CounterpartKeyDescriptor() {
    }

    @Builder(builderMethodName = "counterpartKeyBuilder")
    public CounterpartKeyDescriptor(KeyIdentifier keyIdentifier, T rsaKey, String passwordSalt, String symmetricKey) {
        this.keyIdentifier = keyIdentifier;
        this.rsaKey = rsaKey;
        this.passwordSalt = passwordSalt;
        this.symmetricKey = symmetricKey;
    }

    /**
     * Decoded encryption key that is suitable for symmetric encryption.
     * @return byte array if {@link #setSymmetricKey(String)} was called before with Base64-encoded string.
     */
    public byte[] decodedEncryptionKey() {
        if (encryptionKeyDecoded == null && symmetricKey != null) {
            encryptionKeyDecoded = Base64.getDecoder().decode(symmetricKey);
        }
        return encryptionKeyDecoded;
    }

    public CounterpartIdentity asCounterpartIdentity() {
        return counterpartIdent;
    }

    public void rehydrate() {
        if (rsaKey != null && rsaKey.getPublicKeyMaterial() != null) {
            PublicKey pk = rsaKey.getPublicKey();
            if (pk == null) {
                log.error(String.format("Could not recover the public key for %s; null return value"
                        , safeKeyId()));
            }
        }
    }

    protected String safeKeyId() {
        return keyIdentifier != null ? keyIdentifier.getKeyId() : "unidentified key";
    }

    class CounterpartIdentityImpl implements CounterpartIdentity {
        @Override
        public String getPasswordSalt() {
            return passwordSalt;
        }

        @Override
        public RSAPublicKey getPublicKey() {
            return rsaKey != null ? rsaKey.getPublicKey() : null;
        }
    }
}
