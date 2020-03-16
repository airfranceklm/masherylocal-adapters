package com.airfranceklm.amt.sidecar.identity;

import com.airfranceklm.amt.sidecar.model.alcp.RSAKeyOps;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

@NoArgsConstructor
@Slf4j
public class RSAPublicKeyDescriptor {
    @JsonProperty("pkc8.pub")
    @Getter
    @Setter
    private String publicKeyMaterial;
    private RSAPublicKey unmarshalledPublicKey;

    public RSAPublicKeyDescriptor(String publicKeyMaterial) {
        this.publicKeyMaterial = publicKeyMaterial;
    }

    @JsonIgnore
    public RSAPublicKey getPublicKey() {
        // This isn't a fully correct extraction as it doesn't cater for DSA keys.
        // Not required for now, but may be required when extra algorithms with
        // DSA keys will surface.
        if (unmarshalledPublicKey == null) {
            try {
                unmarshalledPublicKey = RSAKeyOps.publicKeyFromPCKS8(publicKeyMaterial);
            } catch (IOException | InvalidKeySpecException e) {
                log.error(String.format("Cannot recover public key from supplied key material: %s", e.getMessage())
                        , e);
            }
        }
        return unmarshalledPublicKey;
    }
}
