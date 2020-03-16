package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.model.json.JsonSidecarOutputTerminationCommand;

class TerminateDSLImpl
    extends PayloadCreatorDSLImpl<JsonSidecarOutputTerminationCommand>
        implements TerminateDSL {

    public TerminateDSLImpl(JsonSidecarOutputTerminationCommand target) {
        super(target);
    }

    @Override
    public TerminateDSLImpl withCode(int code) {
        target.setStatusCode(code);
        return this;
    }

    @Override
    public TerminateDSLImpl withMessage(String msg) {
        target.setMessage(msg);
        return this;
    }
}
