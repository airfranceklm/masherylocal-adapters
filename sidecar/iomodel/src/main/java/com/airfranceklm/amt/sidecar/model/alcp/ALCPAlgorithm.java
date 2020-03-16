package com.airfranceklm.amt.sidecar.model.alcp;

import com.airfranceklm.amt.sidecar.identity.SidecarIdentity;

import java.util.Base64;

public abstract class ALCPAlgorithm<TProtectedInput, TProtectedOutput>  {

    private static Base64.Decoder decoder = Base64.getDecoder();
    private static Base64.Encoder encoder = Base64.getEncoder();
    AlgorithmActivation activation;

    public ALCPAlgorithm(AlgorithmActivation activation) {
        this.activation = activation;
    }

    public static String toBase64(byte[] rawAuthSignature) {
        return encoder.encodeToString(rawAuthSignature);
    }

    public static byte[] fromBase64(String authSignature) {
        return decoder.decode(authSignature);
    }

    public abstract String getName();

    /**
     * Indication whether the algorithm actually requires the sidecar counterpart identity to be defined
     * @return true if the channel is sufficient for the operation of algorithm, false otherwise.
     */
    public abstract boolean isChannelSufficient(SidecarAuthenticationChannel ch);

    public AlgorithmActivation getActivation() {
        return activation;
    }

    public MasheryALCPSide<TProtectedInput, TProtectedOutput> getMasherySide(SidecarAuthenticationChannel ch) {

        MasheryEncryptionCipher<TProtectedInput> enc = activation.requiresRequestEncryption() ?
                createMasheryEncryptionCipher(ch)
                : null;

        MasheryDecryptionCipher<TProtectedOutput> decr = activation.requiresResponseDecryption() ?
                createMasheryDecryptionCipher(ch)
                : null;

        return new MasheryALCPSide<>(this, enc, decr, getProtectedOutputType());
    }

    /**
     * Concrete implementation of the output type to be expected
     * @return Class which is a concrete instnace of <code>TProtectedOutput</code> type this algorithm
     * implements.
     */
    protected abstract Class<TProtectedOutput> getProtectedOutputType();

    protected abstract MasheryEncryptionCipher<TProtectedInput> createMasheryEncryptionCipher(SidecarAuthenticationChannel ch);
    protected abstract MasheryDecryptionCipher<TProtectedOutput> createMasheryDecryptionCipher(SidecarAuthenticationChannel ch);

    public SidecarALCPSide<TProtectedInput, TProtectedOutput> getSidecarSide(SidecarIdentity ident, KnownMasheryIdentities kmi) {
        SidecarDecryptionCipher<TProtectedInput> decr = activation.requiresRequestEncryption() ?
                createSidecarDecryptionCipher(ident, kmi)
                : null;

        SidecarEncryptionCipher<TProtectedOutput> encr = activation.requiresResponseDecryption() ?
                createSidecarEncryptionCipher(ident) : null;

        return new SidecarALCPSide<>(this, decr, encr);
    }

    protected abstract SidecarDecryptionCipher<TProtectedInput> createSidecarDecryptionCipher(SidecarIdentity siIdent, KnownMasheryIdentities mashIdents);
    protected abstract SidecarEncryptionCipher<TProtectedOutput> createSidecarEncryptionCipher(SidecarIdentity ch);
}
