package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.model.CallModificationCommand;
import com.airfranceklm.amt.sidecar.model.SidecarOutput;

public interface TerminateDSL extends PayloadCreatorDSL {

    TerminateDSL withCode(int code);

    TerminateDSL withMessage(String msg);
}
