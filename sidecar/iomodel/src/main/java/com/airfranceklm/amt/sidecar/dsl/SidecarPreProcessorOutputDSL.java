package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.model.RequestModificationCommand;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;

import java.util.Objects;
import java.util.function.Consumer;

public interface SidecarPreProcessorOutputDSL extends BasicSidecarOutputDSL<RequestModificationCommand
        , SidecarPreProcessorOutput> {

    default SidecarPreProcessorOutputDSL reply(Consumer<ReplyCommandDSL> c) {
        Objects.requireNonNull(c).accept(reply());
        return this;
    }

    default SidecarPreProcessorOutputDSL modify(Consumer<PreProcessorRequestModifyDSL> c) {
        Objects.requireNonNull(c).accept(modify());
        return this;
    }

    ReplyCommandDSL reply();
    PreProcessorRequestModifyDSL modify();

}
