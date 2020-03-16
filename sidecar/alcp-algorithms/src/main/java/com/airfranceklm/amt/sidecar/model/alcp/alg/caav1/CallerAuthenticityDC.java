package com.airfranceklm.amt.sidecar.model.alcp.alg.caav1;

import com.airfranceklm.amt.sidecar.identity.CounterpartIdentity;
import com.airfranceklm.amt.sidecar.JsonIO;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.alcp.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class CallerAuthenticityDC implements SidecarDecryptionCipher<SidecarInput> {

    CallerAuthenticityAlgorithmV1 alg;
    Algorithm jwtAlg;
    JWTVerifier verifier;
    KnownMasheryIdentities kmi;

    public CallerAuthenticityDC(CallerAuthenticityAlgorithmV1 alg, KnownMasheryIdentities kmi) {
        this.alg = alg;

        this.kmi = kmi;
        jwtAlg = Algorithm.RSA256(new RSAKeyProviderImpl());
        this.verifier = JWT.require(jwtAlg)
                .acceptLeeway(alg.getLeewayWindow())
                .acceptExpiresAt(alg.getLeewayWindow())
                .withAudience(CallerAuthenticityAlgorithmV1.ALG_AUDIENCE)
                .build();
    }

    @Override
    public ProtectedSidecarRequest decipher(EncryptedMessage<SidecarInput> input, JsonIO unm) throws IOException {
        if (input != null && input.getContext() != null) {
            String jwt = input.getContext().get(CallerAuthenticityAlgorithmV1.AUTH_CONTEXT_NAME);
            if (jwt != null) {
                try {
                    DecodedJWT djwt = verifier.verify(jwt);
                    if (alg.getSpecificValidator() != null) {
                        if (!alg.getSpecificValidator().apply(djwt)) {
                            throw new IOException("Supplied token is not valid");
                        }
                    }
                    final CounterpartIdentity masheryIdentity = kmi.getMasheryIdentity(djwt.getSubject(), djwt.getKeyId());

                    return new ProtectedSidecarRequest(input.getPayload(), masheryIdentity);
                } catch (JWTVerificationException | UnknownMasheryRequester ex) {
                    throw new IOException(ex);
                }
            }
        }

        throw new IOException("Caller's authenticity is not established");
    }

    class RSAKeyProviderImpl implements RSAKeyProvider {
        @Override
        public RSAPublicKey getPublicKeyById(String s) {
            try {
                return kmi.getMasheryIdentity(null, s).getPublicKey();
            } catch (UnknownMasheryRequester r) {
                return null;
            }
        }

        @Override
        public RSAPrivateKey getPrivateKey() {
            return null;
        }

        @Override
        public String getPrivateKeyId() {
            return null;
        }
    }
}
