package com.airfranceklm.amt.sidecar.model.alcp;

import com.airfranceklm.amt.sidecar.JsonIO;

import java.io.IOException;

public interface SidecarDecryptionCipher<T> {

    ProtectedSidecarRequest decipher(EncryptedMessage<T> input, JsonIO unm) throws IOException;
}
