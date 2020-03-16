package com.airfranceklm.amt.sidecar.model.alcp;

import com.airfranceklm.amt.sidecar.JsonIO;
import com.airfranceklm.amt.sidecar.model.*;

import java.io.IOException;

public class MasheryALCPSide<TProtectedInput, TProtectedOutput> extends ALCPAlgorithmSide<TProtectedInput, TProtectedOutput> {

    private MasheryEncryptionCipher<TProtectedInput> encryptionCipher;
    private MasheryDecryptionCipher<TProtectedOutput> decryptionCipher;
    private Class<TProtectedOutput> sidecarResponseClass;

    MasheryALCPSide(ALCPAlgorithm<TProtectedInput, TProtectedOutput> alg, MasheryEncryptionCipher<TProtectedInput> encryptionCipher, MasheryDecryptionCipher<TProtectedOutput> decryptionCipher,
                    Class<TProtectedOutput> sidecarResponseClass) {
        super(alg);
        this.encryptionCipher = encryptionCipher;
        this.decryptionCipher = decryptionCipher;
        this.sidecarResponseClass = sidecarResponseClass;
    }

    public EncryptedMessage<TProtectedInput> encrypt(SidecarInput input, JsonIO unm) throws IOException {
        return encryptionCipher.encrypt(input, unm);
    }

    public Class<TProtectedOutput> getSidecarResponseClass() {
        return sidecarResponseClass;
    }

    public <T extends CallModificationCommand, U extends SidecarOutput<T>> U decrypt(TProtectedOutput raw, JsonIO unm, Class<U> expectedOutput) throws IOException {
        return decryptionCipher.decrypt(raw, unm, expectedOutput);
    }

}
