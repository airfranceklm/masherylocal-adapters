package com.airfranceklm.amt.sidecar.model.alcp;

import com.airfranceklm.amt.sidecar.JsonIO;
import com.airfranceklm.amt.sidecar.model.SidecarInput;

import java.io.IOException;

public interface MasheryEncryptionCipher<TInput> {
    EncryptedMessage<TInput> encrypt(SidecarInput input, JsonIO unm) throws IOException;
}
