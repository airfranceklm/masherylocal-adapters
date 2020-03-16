package com.airfranceklm.amt.sidecar.model.alcp.alg;

import com.airfranceklm.amt.sidecar.model.alcp.ALCPAlgorithm;
import com.airfranceklm.amt.sidecar.identity.PartyIdentity;
import lombok.AccessLevel;
import lombok.Getter;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Base for the cipher that sends the data to the party
 * @param <TIdent> type of the party that will decipher data for itself.
 */
public abstract class AbstractSenderDecipher<TIn, TOut, TACLP extends ALCPAlgorithm<TIn, TOut> & DualCipherAlgorithm, TIdent extends PartyIdentity>
extends AbstractDualCipher<TIn, TOut, TACLP> {

    @Getter(AccessLevel.PROTECTED)
    private TIdent partyIdentity;
    protected Base64.Decoder decoder = Base64.getDecoder();

    public AbstractSenderDecipher(TACLP alg, TIdent partyIdentity) {
        super(alg);
        this.partyIdentity = partyIdentity;
    }

    protected byte[] rsaDecryptForSelf(String base64msg) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] b64Decoded = decoder.decode(base64msg);

        try {
            Cipher ch = Cipher.getInstance(getRSACipherName());
            ch.init(Cipher.DECRYPT_MODE, partyIdentity.getPrivateKey());

            return ch.doFinal(b64Decoded);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            // TODO Log this, as this should have never occurred.
            throw new IllegalStateException("RSA algorithm must be supported by the JVM");
        }
    }

    protected String decryptToString(KeyAndIV kiv, String base64) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        return new String(decrypt(kiv, base64));
    }

    protected byte[] decrypt(KeyAndIV kiv, String base64) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] b64Bytes = decoder.decode(base64);

        try {
            Cipher ch = Cipher.getInstance(getAESCipherName());
            ch.init(Cipher.DECRYPT_MODE, kiv.getSecretKey(), kiv.getIVParameterSpec());

            return ch.doFinal(b64Bytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            throw new IllegalStateException("AES cipher must be available to JVM");
        }
    }
}
