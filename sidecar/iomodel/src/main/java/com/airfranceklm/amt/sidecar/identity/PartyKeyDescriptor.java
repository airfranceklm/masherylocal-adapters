package com.airfranceklm.amt.sidecar.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.security.PrivateKey;

@NoArgsConstructor
@Slf4j
public class PartyKeyDescriptor extends CounterpartKeyDescriptor<RSAKeyPairDescriptor> {

    @JsonProperty("psw") @Getter @Setter
    BasicCredentialsDescriptor passwordCredentials;

    @Builder(builderMethodName = "partyKeyBuilder")
    public PartyKeyDescriptor(KeyIdentifier keyIdentifier, RSAKeyPairDescriptor rsaKeyPair, String passwordSalt, String symmetricKey, BasicCredentialsDescriptor passwordCredentials) {
        super(keyIdentifier, rsaKeyPair, passwordSalt, symmetricKey);
        this.passwordCredentials = passwordCredentials;
    }

    public void rehydrate(String pwd) {
        super.rehydrate();

        if (getRsaKey() != null) {
            if (getRsaKey().getPrivateKeyMaterial() != null) {
                PrivateKey pk = getRsaKey().privateKeyUsing(pwd);
                if (pk == null) {
                    log.error(String.format("Could not recover the private key for %s; null return value", safeKeyId()));
                }
            }
        }
    }

}
