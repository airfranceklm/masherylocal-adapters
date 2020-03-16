package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.model.CallModificationCommand;
import com.airfranceklm.amt.sidecar.model.SidecarOutput;

import java.util.function.Consumer;

public interface BasicSidecarOutputDSL<TCFg extends CallModificationCommand, U extends SidecarOutput<TCFg>> {

    U output();

    default BasicSidecarOutputDSL<TCFg, U> terminate(Consumer<TerminateDSL> c) {
        c.accept(terminate());
        return this;
    }

    TerminateDSL terminate();
}
