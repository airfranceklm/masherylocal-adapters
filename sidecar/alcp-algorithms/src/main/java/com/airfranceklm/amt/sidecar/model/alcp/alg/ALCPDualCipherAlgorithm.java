package com.airfranceklm.amt.sidecar.model.alcp.alg;

import com.airfranceklm.amt.sidecar.model.alcp.ALCPAlgorithm;
import com.airfranceklm.amt.sidecar.model.alcp.AlgorithmActivation;
import lombok.Getter;
import lombok.Setter;

/**
 * Algorithm that is using dual ciphers, one cipher for the agreement of the encryption key, and the scond one
 * for the encryption
 * @param <TIn>
 * @param <TOut>
 */
public abstract class ALCPDualCipherAlgorithm<TIn, TOut> extends ALCPAlgorithm<TIn, TOut> {

    @Getter @Setter
    private String keyAgreementCipher;
    @Getter @Setter
    private String messageEncryptionCipher;

    public ALCPDualCipherAlgorithm(AlgorithmActivation activation, String keyAgreementCipher, String messageEncryptionCipher) {
        super(activation);
        this.keyAgreementCipher = keyAgreementCipher;
        this.messageEncryptionCipher = messageEncryptionCipher;
    }
}
