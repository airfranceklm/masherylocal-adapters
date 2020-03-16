package com.airfranceklm.amt.sidecar.model.alcp.alg.hspav1;

import com.airfranceklm.amt.sidecar.identity.CounterpartIdentity;
import com.airfranceklm.amt.sidecar.identity.SidecarIdentity;
import com.airfranceklm.amt.sidecar.JsonIO;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.alcp.*;
import com.airfranceklm.amt.sidecar.model.alcp.alg.AbstractSenderDecipher;
import com.airfranceklm.amt.sidecar.model.alcp.alg.KeyAndIV;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

/**
 * Helper class that validates the high-security protocol.
 */
class SidecarHighSecurityInputDecipher
        extends AbstractSenderDecipher<HighSecuritySidecarInput, HighSecuritySidecarOutput, HighSecurityProtectionAlgorithmV1, SidecarIdentity>
        implements SidecarDecryptionCipher<HighSecuritySidecarInput> {

    private KnownMasheryIdentities masheryIdentities;

    public SidecarHighSecurityInputDecipher(HighSecurityProtectionAlgorithmV1 alg, SidecarIdentity sidecarIdentity, KnownMasheryIdentities kmi) {
        super(alg, sidecarIdentity);
        this.masheryIdentities = kmi;
    }

    @Override
    public ProtectedSidecarRequest decipher(EncryptedMessage<HighSecuritySidecarInput> input, JsonIO unm) throws IOException {
        try {
            return new ProtectedSidecarRequest(decipher(input.getPayload(), unm));
        } catch (UnknownMasheryRequester | MalformedHighSecuritySidecarInput | IllegalMessageHeaderException |
                BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException |
                IllegalBlockSizeException | InvalidRequestMessageHeaderException ex) {

            // TODO: log this.
            throw new IOException(String.format("Decryption is not possible: %s", ex.getMessage()), ex);
        }
    }

    protected SidecarInput decipher(HighSecuritySidecarInput secureInput, JsonIO unm) throws MalformedHighSecuritySidecarInput, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, InvalidAlgorithmParameterException, IllegalMessageHeaderException, IOException, UnknownMasheryRequester, InvalidRequestMessageHeaderException {
        if (secureInput.getOneTimePassword() == null || secureInput.getMessageHeader() == null
                || secureInput.getRequestMaterial() == null) {
            throw new MalformedHighSecuritySidecarInput("Input is malformed");
        }

        byte[] aesOTP = rsaDecryptForSelf(secureInput.getOneTimePassword());
        KeyAndIV decrKey = getAlgorithm().createAESParams(aesOTP, getPartyIdentity());

        byte[] body = decrypt(decrKey, secureInput.getRequestMaterial());

        String header = decryptToString(decrKey, secureInput.getMessageHeader());
        validateRequestMessageHeader(RequestMessageHeader.parse(header), body);

        return unm.readTransportOptimizedJSON(body, SidecarInput.class);
    }

    protected void validateRequestMessageHeader(RequestMessageHeader rmh, byte[] dataInTransit) throws UnknownMasheryRequester, InvalidRequestMessageHeaderException {
        if (getAlgorithm().isTimeWiggleAccepted(rmh.getEpochSeconds())) {
            CounterpartIdentity mashIdent = masheryIdentities.getMasheryIdentity(rmh.getAreaId(), rmh.getKeyId());
            if (mashIdent != null) {
                // We need to check the signature, to make sure that the authenticity signature was really signed
                // by the private key we would be willing to trust.
                if (!getAlgorithm().verifySignature(mashIdent, rmh.getAuthenticationSignatureBytes(),
                        // Signature should sign epoch seconds followed by the data being transmitted.
                        String.valueOf(rmh.epochSeconds).getBytes(),
                        dataInTransit)) {
                    throw new InvalidRequestMessageHeaderException("Timestamp signature check has failed");
                }
            }
        } else {
            throw new InvalidRequestMessageHeaderException(String.format("Reference time stamp %d outside of acceptable window", rmh.getEpochSeconds()));
        }
    }

}
