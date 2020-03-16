package com.airfranceklm.amt.sidecar.identity;

import com.airfranceklm.amt.sidecar.model.alcp.RSAKeyOps;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.NetworkInterface;
import java.security.InvalidKeyException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

@Slf4j
@NoArgsConstructor
public class RSAKeyPairDescriptor extends RSAPublicKeyDescriptor {

    @JsonProperty("pkc8.priv") @Getter
    @Setter
    private String privateKeyMaterial;
    private RSAPrivateKey decodedPrivateKey;

    @Builder(builderMethodName = "keypair")
    public RSAKeyPairDescriptor(String publicKeyMaterial, String privateKeyMaterial) {
        super(publicKeyMaterial);
        this.privateKeyMaterial = privateKeyMaterial;
    }

    public RSAPrivateKey privateKeyUsing(String pwd) {
        if (decodedPrivateKey == null) {
            try {
                decodedPrivateKey = RSAKeyOps.privateKeyFromPKCS8(privateKeyMaterial, pwd);
            } catch (IOException | InvalidKeyException | InvalidKeySpecException e) {
                log.error(String.format("Private key cannot be unmarshalled: %s", e.getMessage()), e);
            }
        }
        return decodedPrivateKey;
    }

    /**
     * Returns the private key that was previously unsealed.
     * @return instance of the private key.
     */
    @JsonIgnore
    public RSAPrivateKey getPrivateKey() {
        return Objects.requireNonNull(this.decodedPrivateKey);
    }

}
