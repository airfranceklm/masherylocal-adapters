package com.airfranceklm.amt.sidecar.model.alcp.alg.hspav1;

import com.airfranceklm.amt.sidecar.identity.ClusterIdentity;
import com.airfranceklm.amt.sidecar.identity.CounterpartIdentity;
import com.airfranceklm.amt.sidecar.identity.PartyIdentity;
import com.airfranceklm.amt.sidecar.identity.SidecarIdentity;
import com.airfranceklm.amt.sidecar.model.alcp.*;
import com.airfranceklm.amt.sidecar.model.alcp.alg.DualCipherAlgorithm;
import com.airfranceklm.amt.sidecar.model.alcp.alg.KeyAndIV;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Base class for the high-security protocol.
 */
@Slf4j
public class HighSecurityProtectionAlgorithmV1 extends ALCPAlgorithm<HighSecuritySidecarInput, HighSecuritySidecarOutput>
implements DualCipherAlgorithm {

    public static final String ALGORITHM_REF_NAME = "hspav1";

    static final SecureRandom secureRandom = new SecureRandom();

    private String rsaCipher = "RSA/ECB/PKCS1Padding";
    private String aesCipher = "AES/CBC/PKCS5Padding";
    private String signingAlgorithm = "SHA256withRSA";

    private int aesVariablePasswordLength = 32;
    private int keyLength = 16;
    private int ivLength = 16;
    private String IVInferenceDigestAlgorithm = "SHA-256";
    private long maximumClockInaccuracy = TimeUnit.MINUTES.toSeconds(5);

    private static final int gcmIvLength = 12;
    private static final int gcmIvTagLengthBits = 128;

    private Function<byte[], AlgorithmParameterSpec> ivParamCreator;

    HighSecurityProtectionAlgorithmV1(AlgorithmActivation actv) {
       super(actv);
       ivParamCreator = this::createCBCAlgParameterSpec;
    }

    @Override
    public String getName() {
        return String.format("High-security message protocol, %s key exchange, %s encryption", rsaCipher, aesCipher);
    }

    @Override
    public boolean isChannelSufficient(SidecarAuthenticationChannel ch) {
        if (ch != null) {
            final ClusterIdentity mashId = ch.getMasheryIdentity();
            if (mashId != null) {
                if (mashId.getAreaId() == null
                        && mashId.getKeyId() == null
                        && mashId.getPrivateKey() == null
                        && mashId.getPasswordSalt() == null) {
                    return false;
                }
            } else {
                return false;
            }

            final CounterpartIdentity skId = ch.getSidecarIdentity();
            if (skId != null) {
                return skId.getPublicKey() != null && skId.getPasswordSalt() != null;
            } else {
                return false;
            }

        } else {
            return false;
        }
    }

    @Override
    protected Class<HighSecuritySidecarOutput> getProtectedOutputType() {
        return HighSecuritySidecarOutput.class;
    }

    public void setGCMMode() {
        this.ivLength = gcmIvLength;
        this.aesCipher = "AES/GCM/NoPadding";
        this.ivParamCreator = this::createGCMAlgParameterSpec;
//        System.out.println("GCM Mode");
    }

    public long epochSecondsNow() {
        return Math.round(System.currentTimeMillis() / 1000f);
    }

    String getSigningAlgorithm() {
        return signingAlgorithm;
    }

    int getAesVariablePasswordLength() {
        return aesVariablePasswordLength;
    }

    String getIVInferenceDigestAlgorithm() {
        return IVInferenceDigestAlgorithm;
    }

    long getMaximumClockInaccuracy() {
        return maximumClockInaccuracy;
    }

    void setRsaCipher(String rsaCipher) {
        this.rsaCipher = rsaCipher;
    }

    void setAesCipher(String aesCipher) {
        this.aesCipher = aesCipher;
    }

    void setSigningAlgorithm(String signingAlgorithm) {
        this.signingAlgorithm = signingAlgorithm;
    }

    void setAesVariablePasswordLength(int aesVariablePasswordLength) {
        this.aesVariablePasswordLength = aesVariablePasswordLength;
    }

    void setIVInferenceDigestAlgorithm(String IVInferenceDigestAlgorithm) {
        this.IVInferenceDigestAlgorithm = IVInferenceDigestAlgorithm;
    }

    void setMaximumClockInaccuracy(long maximumClockInaccuracy) {
        this.maximumClockInaccuracy = maximumClockInaccuracy;
    }

    int getKeyLength() {
        return keyLength;
    }

    void setKeyLength(int keyLength) {
        this.keyLength = keyLength;
    }

    public byte[] sign(PartyIdentity ident, byte[]... dataBuffers) throws IOException {
        try {
            Signature sig = Signature.getInstance(signingAlgorithm);
            sig.initSign(ident.getPrivateKey());

            for (byte[] buf: dataBuffers) {
                sig.update(buf);
            }

            return sig.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            log.error(String.format("Failed to encrypt epoch seconds: %s", ex.getMessage()), ex);
            throw new IOException("Signing impossible");
        }
    }

    /**
     * Verifies signature
     * @param ci data of the counterpart
     * @param signature signature provided by the counterpart.
     * @param dataBuffers data to be checked, optionally split in distinct data buffers.
     * @return <code>true</code> if the signature verification succeeds, <code>false</code> otherwise.
     */
    public boolean verifySignature(CounterpartIdentity ci, byte[] signature, byte[]... dataBuffers) {
        try {
            Signature sig = Signature.getInstance(signingAlgorithm);
            sig.initVerify(ci.getPublicKey());

            for (byte[] buf: dataBuffers) {
                sig.update(buf);
            }


            return sig.verify(signature);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            log.error(String.format("Signature verification failed: %s (%s)", e.getMessage(), e.getClass().getName()), e);
            return false;
        }
    }

    /**
     * Helper method to establish whether the time wiggle will be accepted
     *
     * @param otherRef timestamp from a desynchronized clock
     * @return true if wiggle is acceptable, false otherwise.
     */
    public boolean isTimeWiggleAccepted(long otherRef) {
        long diff = Math.abs(epochSecondsNow() - otherRef);
        return maximumClockInaccuracy >= diff;
    }

    public KeyAndIV createAESParams(byte[] oneTimePassword, PartyIdentity selfIdent) {
        return inferAESKeyAndIV(oneTimePassword, fullAESPassword(oneTimePassword, selfIdent.getPasswordSalt().getBytes()));
    }

    /**
     * Creates the AES encoding parameters to be decrypted by the counterpart
     *
     * @param counterpartIdent identity of the counterpart party that needs receiving.
     * @return AES encryption key and initialization vector data.
     */
    public KeyAndIV createAESParams(CounterpartIdentity counterpartIdent) {
        byte[] aesPwd = new byte[aesVariablePasswordLength];
        secureRandom.nextBytes(aesPwd);

        return inferAESKeyAndIV(aesPwd, fullAESPassword(aesPwd, counterpartIdent.getPasswordSalt().getBytes()));
    }

    private static byte[] fullAESPassword(byte[] aesPwd, byte[] aesPasswordSalt) {
        byte[] fullPwq = new byte[aesPwd.length + aesPasswordSalt.length];
        System.arraycopy(aesPwd, 0, fullPwq, 0, aesPwd.length);
        System.arraycopy(aesPasswordSalt, 0, fullPwq, aesPwd.length, aesPasswordSalt.length);
        return fullPwq;
    }

    private AlgorithmParameterSpec createCBCAlgParameterSpec(byte[] iv) {
        return new IvParameterSpec(iv);
    }

    private AlgorithmParameterSpec createGCMAlgParameterSpec(byte[] iv) {
        return new GCMParameterSpec(gcmIvTagLengthBits, iv);
    }

    private KeyAndIV inferAESKeyAndIV(byte[] aesPwd, byte[] fullPwq) {
        try {
            MessageDigest md = MessageDigest.getInstance(IVInferenceDigestAlgorithm);
            md.update(fullPwq);

            byte[] digest = md.digest();

            byte[] key = new byte[keyLength];

            byte[] iv = new byte[ivLength];
            int ivOffset = digest.length - ivLength - 1;

            System.arraycopy(digest, 0, key, 0, keyLength);
            System.arraycopy(digest, ivOffset, iv, 0, ivLength);

            return new KeyAndIV(aesPwd, key, iv, ivParamCreator.apply(iv));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Message digest must be supported by the JVM");
        }
    }

    // ---------------------------------------------------------------
    // Methods for creating the ciphers.


    @Override
    protected MasheryEncryptionCipher<HighSecuritySidecarInput> createMasheryEncryptionCipher(SidecarAuthenticationChannel sac) {
        return new HighSecurityInputCipher(this, sac);
    }

    @Override
    protected MasheryDecryptionCipher<HighSecuritySidecarOutput> createMasheryDecryptionCipher(SidecarAuthenticationChannel sac) {
        return new HighSecurityOutputDecipher(this, sac);
    }

    @Override
    protected SidecarDecryptionCipher<HighSecuritySidecarInput> createSidecarDecryptionCipher(SidecarIdentity siIdent, KnownMasheryIdentities mashIdents) {
        return new SidecarHighSecurityInputDecipher(this, siIdent, mashIdents);
    }

    @Override
    protected SidecarEncryptionCipher<HighSecuritySidecarOutput> createSidecarEncryptionCipher(SidecarIdentity ch) {
        return new SidecarHighSecurityOutputCipher(this, ch);
    }

    // -----------------
    // Dual cipher algorithm

    @Override
    public String getAssymetricCipherName() {
        return rsaCipher;
    }

    @Override
    public String getSymmetricCipherName() {
        return aesCipher;
    }

    // -----------------------------------------------------------------------------------
    // Static methods.

    public static ALCPAlgorithm<HighSecuritySidecarInput, HighSecuritySidecarOutput> fromSpec(ALCPAlgorithmSpec spec) {
        return OptionsMapper.fromOptions(spec.getActivation(), spec.getParams());
    }
}
