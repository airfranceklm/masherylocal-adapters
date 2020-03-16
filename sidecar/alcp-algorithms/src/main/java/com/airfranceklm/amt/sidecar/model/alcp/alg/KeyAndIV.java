package com.airfranceklm.amt.sidecar.model.alcp.alg;

import lombok.Getter;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;

/**
 * Class containing the key and the initialization vector for encryption algorithms.
 */
public class KeyAndIV {

    @Getter
    byte[] oneTimePass;
    @Getter
    byte[] key;
    @Getter
    byte[] IV;

    private SecretKey secretKey;
    private AlgorithmParameterSpec IVSpec;

    public KeyAndIV(byte[] oneTimePass, byte[] key, byte[] IV) {
        this.oneTimePass = oneTimePass;
        this.key = key;
        this.IV = IV;
    }

    public KeyAndIV(byte[] oneTimePass, byte[] key, AlgorithmParameterSpec IVSpec) {
        this.oneTimePass = oneTimePass;
        this.key = key;
        this.IVSpec = IVSpec;
    }

    public KeyAndIV(byte[] oneTimePass, byte[] key, byte[] IV, AlgorithmParameterSpec IVSpec) {
        this.oneTimePass = oneTimePass;
        this.key = key;
        this.IV = IV;
        this.IVSpec = IVSpec;
    }

    public SecretKey getSecretKey() {
        if (secretKey == null) {
            secretKey = new SecretKeySpec(key, 0, key.length, "AES");
        }
        return secretKey;
    }

    /**
     * Method that may need to be overridden by subclasses to provide an algorithm specification which is
     * in line with the cipher algorithm used.
     * @return instance of the algorithm specification
     */
    public AlgorithmParameterSpec getIVParameterSpec() {
        if (IVSpec == null) {
            final byte[] iv = getIV();
            Objects.requireNonNull(iv);

            IVSpec = new IvParameterSpec(iv);
        }
        return IVSpec;
    }

    private String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    private String encodeHexString(byte[] byteArray) {
        StringBuilder hexStringBuffer = new StringBuilder();
        for (byte b : byteArray) {
            hexStringBuffer.append(byteToHex(b));
        }
        return hexStringBuffer.toString();
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s", encodeHexString(oneTimePass), encodeHexString(key), encodeHexString(IV));
    }
}
