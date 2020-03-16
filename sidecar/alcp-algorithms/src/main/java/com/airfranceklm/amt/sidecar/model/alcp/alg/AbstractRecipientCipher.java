package com.airfranceklm.amt.sidecar.model.alcp.alg;

import com.airfranceklm.amt.sidecar.model.alcp.ALCPAlgorithm;
import com.airfranceklm.amt.sidecar.identity.CounterpartIdentity;
import lombok.AccessLevel;
import lombok.Getter;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Base class for the RSA+AES cipher
 */
public abstract class AbstractRecipientCipher<TIn, TOut, TALCP extends ALCPAlgorithm<TIn, TOut> & DualCipherAlgorithm>
        extends AbstractDualCipher<TIn, TOut, TALCP> {

    @Getter(AccessLevel.PROTECTED)
    private CounterpartIdentity counterpartIdentity;
    protected Base64.Encoder encoder = Base64.getEncoder();

    public AbstractRecipientCipher( TALCP algorithm, CounterpartIdentity cpIdent) {
        super(algorithm);
        this.counterpartIdentity = cpIdent;
    }

    protected String rsaEncryptForRecipient(byte[] buf) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        checkNonNullRecipient();
        return rsaEncryptForRecipient(buf, this.counterpartIdentity);
    }

    private void checkNonNullRecipient() {
        if (this.counterpartIdentity == null) {
            throw new IllegalStateException("Fixed counterpart identity not specified. Specify identity or use explicit encryption methods.");
        }
    }

    protected String rsaEncryptForRecipient(byte[] buf, CounterpartIdentity counterpartIdentity) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(getRSACipherName());
        cipher.init(Cipher.ENCRYPT_MODE, counterpartIdentity.getPublicKey());
        return Base64.getEncoder().encodeToString(cipher.doFinal(buf));
    }

    protected String aesEncryptForRecipient(KeyAndIV kiv, String str) throws IOException, InvalidKeyException {
        if (str == null) {
            return null;
        } else {
            return encoder.encodeToString(aesEncryptDataForRecipient(kiv, str.getBytes()));
        }
    }

    protected String aesEncryptForRecipient(KeyAndIV kiv, byte[] buf) throws IOException, InvalidKeyException  {
        return encoder.encodeToString(aesEncryptDataForRecipient(kiv, buf));
    }

    protected byte[] aesEncryptDataForRecipient(KeyAndIV kiv, byte[] buf) throws IOException, InvalidKeyException  {
        try {
            Cipher ch = Cipher.getInstance(getAESCipherName());

            SecretKeySpec sks = new SecretKeySpec(kiv.getKey(), "AES");

            ch.init(Cipher.ENCRYPT_MODE, sks, kiv.getIVParameterSpec());
            return ch.doFinal(buf);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            // TODO log this
            e.printStackTrace(System.out);
            throw new IOException("AES encryption is not possible");
        }
    }
}
