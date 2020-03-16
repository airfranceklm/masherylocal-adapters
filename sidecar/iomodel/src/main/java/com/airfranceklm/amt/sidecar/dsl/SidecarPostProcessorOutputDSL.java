package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.model.ResponseModificationCommand;
import com.airfranceklm.amt.sidecar.model.SidecarPostProcessorOutput;

import java.util.Objects;
import java.util.function.Consumer;

public interface SidecarPostProcessorOutputDSL extends BasicSidecarOutputDSL<ResponseModificationCommand
        , SidecarPostProcessorOutput> {

    default SidecarPostProcessorOutputDSL modify(Consumer<PostProcessorResponseModifyDSL> c) {
        Objects.requireNonNull(c).accept(modify());
        return this;
    }

    PostProcessorResponseModifyDSL modify();
}
