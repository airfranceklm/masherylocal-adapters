package com.airfranceklm.amt.sidecar.dsl;

import java.util.Objects;
import java.util.function.Consumer;

public interface PreProcessorRequestModifyDSL extends BaseCallModificationDSL {

    default PreProcessorRequestModifyDSL changeRoute(Consumer<ChangeRouteDSL> c) {
        Objects.requireNonNull(c).accept(changeRoute());
        return this;
    }

    ChangeRouteDSL changeRoute();
}
