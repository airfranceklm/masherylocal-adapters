package com.airfranceklm.amt.sidecar.impl.model;

import com.airfranceklm.amt.sidecar.model.SidecarPostProcessorOutput;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SidecarPostProcessorOutputImpl extends AbstractSidecarOutputImpl implements SidecarPostProcessorOutput {
    private ResponseModificationCommandImpl modify;

    @Override
    public ResponseModificationCommandImpl getModify() {
        return modify;
    }

    public void setModify(ResponseModificationCommandImpl modify) {
        this.modify = modify;
    }

    @Override
    public boolean modifies() {
        return modify != null;
    }
}
