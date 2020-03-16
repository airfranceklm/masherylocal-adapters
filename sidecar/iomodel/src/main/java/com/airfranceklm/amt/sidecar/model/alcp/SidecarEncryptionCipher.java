package com.airfranceklm.amt.sidecar.model.alcp;

import com.airfranceklm.amt.sidecar.identity.CounterpartIdentity;
import com.airfranceklm.amt.sidecar.model.CallModificationCommand;
import com.airfranceklm.amt.sidecar.JsonIO;
import com.airfranceklm.amt.sidecar.model.SidecarOutput;

import java.io.IOException;

public interface SidecarEncryptionCipher<TOutput> {

    <T extends CallModificationCommand> TOutput encrypt(SidecarOutput<T> output, JsonIO unm, CounterpartIdentity recipient) throws IOException;
}
