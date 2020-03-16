package com.airfranceklm.amt.sidecar.model.alcp.alg.hspav1;

import com.airfranceklm.amt.sidecar.model.CallModificationCommand;
import com.airfranceklm.amt.sidecar.JsonIO;
import com.airfranceklm.amt.sidecar.model.SidecarOutput;
import com.airfranceklm.amt.sidecar.identity.CounterpartIdentity;
import com.airfranceklm.amt.sidecar.model.alcp.SidecarEncryptionCipher;
import com.airfranceklm.amt.sidecar.identity.SidecarIdentity;
import com.airfranceklm.amt.sidecar.model.alcp.alg.AbstractRecipientCipher;
import com.airfranceklm.amt.sidecar.model.alcp.alg.KeyAndIV;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Helper class that will encrypt a sidecar output for the response to Mashery
 */
class SidecarHighSecurityOutputCipher
        extends AbstractRecipientCipher<HighSecuritySidecarInput, HighSecuritySidecarOutput, HighSecurityProtectionAlgorithmV1>
        implements SidecarEncryptionCipher<HighSecuritySidecarOutput> {

    private SidecarIdentity sidecarIdentity;

    public SidecarHighSecurityOutputCipher(HighSecurityProtectionAlgorithmV1 alg, SidecarIdentity sidecarIdentity) {
        super(alg, null);
        this.sidecarIdentity = sidecarIdentity;
    }

    @Override
    public <T extends CallModificationCommand> HighSecuritySidecarOutput encrypt(SidecarOutput<T> output, JsonIO unm, CounterpartIdentity recipient) throws IOException {
      if (output == null) {
            return null;
        } else {
            return doEncrypt(unm.toTransportOptimizedJSON(output), recipient);
        }
    }

    private HighSecuritySidecarOutput doEncrypt(byte[] payloadInTransit, CounterpartIdentity ci) throws IOException {
        try {
            HighSecuritySidecarOutput so = new HighSecuritySidecarOutput();

            // Compute the key and initialization vector based on the combination of
            // parameters of the salted password.
            KeyAndIV kiv = getAlgorithm().createAESParams(ci);
            so.setOneTimePassword(rsaEncryptForRecipient(kiv.getOneTimePass(), ci));

            so.setResponseMaterial(aesEncryptForRecipient(kiv, payloadInTransit));

            MessageHeader mh = new MessageHeader(getAlgorithm(), sidecarIdentity, payloadInTransit);
            so.setResponseHeader(aesEncryptForRecipient(kiv, mh.toString()));

            return so;

        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            // TODO: Log this.
            throw new IOException("Encryption is not possible");
        }
    }

}
