package com.airfranceklm.amt.sidecar.model.alcp.alg.caav1;

import com.airfranceklm.amt.sidecar.JsonIO;
import com.airfranceklm.amt.sidecar.identity.ClusterIdentity;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.alcp.EncryptedMessage;
import com.airfranceklm.amt.sidecar.model.alcp.MasheryEncryptionCipher;
import com.airfranceklm.amt.sidecar.model.alcp.SidecarAuthenticationChannel;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.airfranceklm.amt.sidecar.model.alcp.alg.caav1.CallerAuthenticityAlgorithmV1.AUTH_CONTEXT_NAME;
import static com.airfranceklm.amt.sidecar.model.alcp.alg.caav1.CallerAuthenticityAlgorithmV1.PASSPHRASE_CONTEXT_NAME;

public class CallerAuthenticityEC implements MasheryEncryptionCipher<SidecarInput> {
    private SidecarAuthenticationChannel ch;
    private CallerAuthenticityAlgorithmV1 alg;

    private Algorithm jwtAlg;
    private long jwtTTL;
    private long jwtRecompileThreshold;

    private long recompileTime = -1;
    private String compiledJwt = null;

    public CallerAuthenticityEC(CallerAuthenticityAlgorithmV1 alg, SidecarAuthenticationChannel ch) {
        this.alg = alg;
        this.ch = ch;

        jwtTTL = alg.getJwtTimeToLive();
        jwtRecompileThreshold = Math.round(jwtTTL * 0.75);

        final ClusterIdentity masheryIdentity = ch.getMasheryIdentity();

        this.jwtAlg = Algorithm.RSA256(masheryIdentity.getPublicKey()
                , masheryIdentity.getPrivateKey());
    }

    @Override
    public EncryptedMessage<SidecarInput> encrypt(SidecarInput input, JsonIO unm) throws IOException {

        EncryptedMessage.EncryptedMessageBuilder<SidecarInput> retVal = EncryptedMessage
            .<SidecarInput>builder()
                .synchronicity(input.getSynchronicity())
                .payload(input)
                .contextEntry(AUTH_CONTEXT_NAME, createJWT());

        if (alg.getPassphrase() != null) {
            retVal = retVal.contextEntry(PASSPHRASE_CONTEXT_NAME, alg.getPassphrase());
        }

        return retVal.build();
    }

    protected String createJWT() throws IOException {
        if (System.currentTimeMillis() < recompileTime) {
            return compiledJwt;
        } else {
            synchronized (this) {
                try {
                    final Date issuedAt = new Date();
                    final String retVal = JWT.create()
                            .withIssuer("AFKLM.ML.CAAV1")
                            .withSubject(ch.getMasheryIdentity().getAreaId())
                            .withKeyId(ch.getMasheryIdentity().getKeyId())
                            .withIssuedAt(issuedAt)
                            .withNotBefore(issuedAt)
                            .withExpiresAt(new Date(issuedAt.getTime() + jwtTTL))
                            .withAudience(CallerAuthenticityAlgorithmV1.ALG_AUDIENCE)
                            .sign(jwtAlg);

                    compiledJwt = retVal;
                    recompileTime = System.currentTimeMillis() + jwtRecompileThreshold;

                    return retVal;
                } catch (JWTCreationException ex) {
                    throw new IOException(ex);
                }
            }
        }
    }
}
