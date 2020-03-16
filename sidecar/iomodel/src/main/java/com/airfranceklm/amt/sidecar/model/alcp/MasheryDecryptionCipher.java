package com.airfranceklm.amt.sidecar.model.alcp;

import com.airfranceklm.amt.sidecar.model.CallModificationCommand;
import com.airfranceklm.amt.sidecar.model.SidecarOutput;
import com.airfranceklm.amt.sidecar.JsonIO;

import java.io.IOException;

public interface MasheryDecryptionCipher<TOutput> {

    <T extends CallModificationCommand, U extends SidecarOutput<T>> U decrypt(TOutput raw
            , JsonIO unm
            , Class<U> clazz) throws IOException;
}
