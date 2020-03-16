package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.model.CallModificationCommand;
import com.airfranceklm.amt.sidecar.model.SidecarOutput;
import com.airfranceklm.amt.sidecar.model.json.JsonAbstractSidecarOutput;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarOutputTerminationCommand;

import java.util.function.Consumer;

import static com.airfranceklm.amt.sidecar.CommonExpressions.allocOrGet;


/**
 * Base class for the sidecar output
 * @param <T> concrete type of the output being built.
 */
abstract class BasicSidecarOutputDSLImpl<TCFg extends CallModificationCommand, U extends SidecarOutput<TCFg>, T extends JsonAbstractSidecarOutput & SidecarOutput<TCFg>>
implements BasicSidecarOutputDSL<TCFg, U> {

    private T data;

    public BasicSidecarOutputDSLImpl(T data) {
        this.data = data;
    }

    /**
     * Retrieves the built result
     * @return built result
     */
    @Override
    @SuppressWarnings("unchecked")
    public U output() {
        return (U)data;
    }

    protected T bean() {
        return data;
    }
}
