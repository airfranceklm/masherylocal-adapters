package com.airfranceklm.amt.sidecar.model.alcp.alg.caav1;

import com.airfranceklm.amt.sidecar.identity.ClusterIdentity;
import com.airfranceklm.amt.sidecar.identity.SidecarIdentity;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.alcp.*;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Getter;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Caller authenticity algorithm, that will add a self-signed JWT token to the parameter of the {@link EncryptedMessage}
 * class without encrypting the actual content.
 * <p/>
 * The algorithm applies only for request from Mashery to sidecar, thus it applies for {@link AlgorithmActivation#RequestOnly}
 * activation. Attempts to create unsupported methods will throw {@link UnsupportedOperationException}.
 */
public class CallerAuthenticityAlgorithmV1 extends ALCPAlgorithm<SidecarInput, CallerAuthenticityAlgorithmV1.ResponseType> {

    public static final String ALGORITHM_REF_NAME = "caav1";

    static final String AUTH_CONTEXT_NAME = "auth";
    static final String PASSPHRASE_CONTEXT_NAME = "pass";

    static final String ALG_AUDIENCE = "Mashery Sidecar";

    public static final String ALG_PARAM_PASSPHRASE = "passphrase";

    @Getter
    private String passphrase;

    @Getter
    private Long validitySpan;

    @Getter
    private int leewayWindow = 150;

    @Getter
    private Function<DecodedJWT, Boolean> specificValidator;

    @Getter
    private long jwtTimeToLive = TimeUnit.MINUTES.toMillis(2);

    public CallerAuthenticityAlgorithmV1(TimeUnit unit, int span) {
        super(AlgorithmActivation.RequestOnly);

        this.validitySpan = unit.toMillis(span);
        specificValidator = this::validateTimeSpan;
    }

    public CallerAuthenticityAlgorithmV1(TimeUnit unit, int span, String passphrase) {
        this(unit, span);
        this.passphrase = passphrase;
    }

    public void setTimeToLive(TimeUnit tu, int incr) {
        this.jwtTimeToLive = tu.toMillis(incr);
    }

    public static ALCPAlgorithm<SidecarInput, ResponseType> fromSpec(ALCPAlgorithmSpec spec) {
        TimeUnit tu = TimeUnit.MINUTES;
        int span = 2;
        String passPhrase = null;

        if (spec != null) {
            passPhrase = spec.getParam(ALG_PARAM_PASSPHRASE);
        }

        return new CallerAuthenticityAlgorithmV1(tu, span, passPhrase);
    }

    @Override
    public boolean isChannelSufficient(SidecarAuthenticationChannel ch) {
        if (ch != null) {
            final ClusterIdentity mashId = ch.getMasheryIdentity();
            if (mashId != null) {
                return mashId.getAreaId() != null && mashId.getKeyId() != null && mashId.getPrivateKey() != null;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean validateTimeSpan(DecodedJWT jwt) {
        Date nbf = jwt.getNotBefore();
        Date exp = jwt.getExpiresAt();
        if (nbf != null && exp != null) {
            return exp.getTime() - nbf.getTime() <= validitySpan;
        } else {
            return false;
        }
    }

    @Override
    public String getName() {
        return "Caller Authenticity Algorithm V1";
    }

    @Override
    protected Class<ResponseType> getProtectedOutputType() {
        return ResponseType.class;
    }

    @Override
    protected MasheryEncryptionCipher<SidecarInput> createMasheryEncryptionCipher(SidecarAuthenticationChannel ch) {
        return new CallerAuthenticityEC(this, ch);
    }

    @Override
    protected MasheryDecryptionCipher<ResponseType> createMasheryDecryptionCipher(SidecarAuthenticationChannel ch) {
        throw new UnsupportedOperationException("This operation is not supported by this algorithm");
    }

    @Override
    protected SidecarDecryptionCipher<SidecarInput> createSidecarDecryptionCipher(SidecarIdentity siIdent, KnownMasheryIdentities mashIdents) {
        return new CallerAuthenticityDC(this, mashIdents);
    }

    @Override
    protected SidecarEncryptionCipher<ResponseType> createSidecarEncryptionCipher(SidecarIdentity ch) {
        throw new UnsupportedOperationException("This operation is not supported by this algorithm");
    }

    public interface ResponseType extends Map<String, Object> {
    }
}
