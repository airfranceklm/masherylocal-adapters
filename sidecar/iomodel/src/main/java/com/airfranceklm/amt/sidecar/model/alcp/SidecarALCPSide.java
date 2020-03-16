package com.airfranceklm.amt.sidecar.model.alcp;

import com.airfranceklm.amt.sidecar.identity.CounterpartIdentity;
import com.airfranceklm.amt.sidecar.model.CallModificationCommand;
import com.airfranceklm.amt.sidecar.JsonIO;
import com.airfranceklm.amt.sidecar.model.SidecarOutput;

import java.io.IOException;

public class SidecarALCPSide<TProtectedInput, TProtectedOutput> extends ALCPAlgorithmSide<TProtectedInput, TProtectedOutput> {

    private SidecarDecryptionCipher<TProtectedInput> decrypter;
    private SidecarEncryptionCipher<TProtectedOutput> encrypter;

    public SidecarALCPSide(ALCPAlgorithm<TProtectedInput, TProtectedOutput> alg,
                           SidecarDecryptionCipher<TProtectedInput> decrypter,
                           SidecarEncryptionCipher<TProtectedOutput> encrypter) {
        super(alg);

        this.decrypter = decrypter;
        this.encrypter = encrypter;
    }

    public ProtectedSidecarRequest decrypt(EncryptedMessage<TProtectedInput> input, JsonIO unm) throws IOException {
        return decrypter.decipher(input, unm);
    }

    public <T extends CallModificationCommand> TProtectedOutput encrypt(SidecarOutput<T> response, JsonIO unm, CounterpartIdentity masheryClusterFor) throws IOException {
        return encrypter.encrypt(response, unm, masheryClusterFor);
    }
}
