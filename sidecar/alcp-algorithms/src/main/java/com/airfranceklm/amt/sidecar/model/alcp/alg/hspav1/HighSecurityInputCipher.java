package com.airfranceklm.amt.sidecar.model.alcp.alg.hspav1;

import com.airfranceklm.amt.sidecar.identity.ClusterIdentity;
import com.airfranceklm.amt.sidecar.JsonIO;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.alcp.*;
import com.airfranceklm.amt.sidecar.model.alcp.alg.AbstractRecipientCipher;
import com.airfranceklm.amt.sidecar.model.alcp.alg.KeyAndIV;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;

/**
 * A helper class that can convert {@link com.airfranceklm.amt.sidecar.model.SidecarInput} objects into
 * the corresponding {@link HighSecuritySidecarInput}.
 */
class HighSecurityInputCipher
        extends AbstractRecipientCipher<HighSecuritySidecarInput, HighSecuritySidecarOutput, HighSecurityProtectionAlgorithmV1>
        implements MasheryEncryptionCipher<HighSecuritySidecarInput> {

    private ClusterIdentity clusterIdent;

    public HighSecurityInputCipher(HighSecurityProtectionAlgorithmV1 alg, SidecarAuthenticationChannel sac) {
        super(alg, sac.getSidecarIdentity());
        this.clusterIdent = sac.getMasheryIdentity();
    }

    public EncryptedMessage<HighSecuritySidecarInput> encrypt(SidecarInput input, JsonIO unm) throws IOException {
        if (input == null) {
            return null;
        } else {
            final byte[] rawData = unm.toTransportOptimizedJSON(input);
            final HighSecuritySidecarInput hms = doEncrypt(rawData);

            return new EncryptedMessage<>(input.getSynchronicity(), hms);
        }
    }

    private HighSecuritySidecarInput doEncrypt(byte[] buf) throws IOException {
        try {
            HighSecuritySidecarInput so = new HighSecuritySidecarInput();

            // Compute the key and initialization vector based on the combination of
            // parameters of the salted password.
            KeyAndIV kiv = getAlgorithm().createAESParams(getCounterpartIdentity());
//            System.out.println(kiv.toString());
            so.setOneTimePassword(rsaEncryptForRecipient(kiv.getOneTimePass()));

            so.setRequestMaterial(aesEncryptForRecipient(kiv, buf));

            RequestMessageHeader rmh = new RequestMessageHeader(getAlgorithm(), clusterIdent, buf);
            so.setMessageHeader(aesEncryptForRecipient(kiv, rmh.toString()));

            return so;

        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            // TODO: Log this.
            throw new IOException("Encryption is not possible");
        }
    }

}
