package com.airfranceklm.amt.sidecar.model.alcp.alg.hspav1;

import com.airfranceklm.amt.sidecar.model.CallModificationCommand;
import com.airfranceklm.amt.sidecar.model.SidecarOutput;
import com.airfranceklm.amt.sidecar.JsonIO;
import com.airfranceklm.amt.sidecar.identity.ClusterIdentity;
import com.airfranceklm.amt.sidecar.identity.CounterpartIdentity;
import com.airfranceklm.amt.sidecar.model.alcp.MasheryDecryptionCipher;
import com.airfranceklm.amt.sidecar.model.alcp.SidecarAuthenticationChannel;
import com.airfranceklm.amt.sidecar.model.alcp.alg.AbstractSenderDecipher;
import com.airfranceklm.amt.sidecar.model.alcp.alg.KeyAndIV;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

class HighSecurityOutputDecipher
        extends AbstractSenderDecipher<HighSecuritySidecarInput, HighSecuritySidecarOutput, HighSecurityProtectionAlgorithmV1, ClusterIdentity>
        implements MasheryDecryptionCipher<HighSecuritySidecarOutput> {

    private CounterpartIdentity sidecarIdentity;

    public HighSecurityOutputDecipher(HighSecurityProtectionAlgorithmV1 alg, SidecarAuthenticationChannel sac) {
        super(alg, sac.getMasheryIdentity());
        this.sidecarIdentity = sac.getSidecarIdentity();
    }

    @Override
    public <T extends CallModificationCommand, U extends SidecarOutput<T>> U decrypt(HighSecuritySidecarOutput raw, JsonIO unm, Class<U> clazz) throws IOException {
        if (raw == null) {
            return null;
        }

        try {
            raw.checkFields();
            return unm.readTransportOptimizedJSON(decryptBody(raw), clazz);
        } catch (IllegalMessageHeaderException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | MalformedHighSecuritySidecarOutput | IllegalBlockSizeException | InvalidResponseMessageHeaderException e) {
            // TODO log this.
            throw new IOException(String.format("Unmarshalling is not possible: %s", e.getMessage()), e);
        }
    }

    private byte[] decryptBody(HighSecuritySidecarOutput hso) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, IllegalMessageHeaderException, InvalidResponseMessageHeaderException {
        // We have a valid object; let's process it.
        byte[] otp = rsaDecryptForSelf(hso.getOneTimePassword());
        KeyAndIV kiv = getAlgorithm().createAESParams(otp, getPartyIdentity());

        final byte[] body = decrypt(kiv, hso.getResponseMaterial());

        String header = decryptToString(kiv, hso.getResponseHeader());
        MessageHeader mh = MessageHeader.parse(header);

        validateRequestMessageHeader(mh, body);

        return body;
    }

    protected void validateRequestMessageHeader(MessageHeader rmh, byte[] transportData) throws InvalidResponseMessageHeaderException {
        if (getAlgorithm().isTimeWiggleAccepted(rmh.getEpochSeconds())) {
            if (sidecarIdentity != null) {
                // We need to check the signature, to make sure that the authenticity signature was really signed
                // by the private key we would be willing to trust.
                if (!getAlgorithm().verifySignature(sidecarIdentity, rmh.getAuthenticationSignatureBytes(),
                        // The signature should comprise epoch seconds followed by the data being transported.
                        String.valueOf(rmh.epochSeconds).getBytes(),
                        transportData)) {
                    throw new InvalidResponseMessageHeaderException("Timestamp signature check has failed");
                }
            }
        } else {
            throw new InvalidResponseMessageHeaderException(String.format("Reference time stamp %d outside of acceptable window", rmh.getEpochSeconds()));
        }
    }

}
