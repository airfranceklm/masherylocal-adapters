package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.model.json.JsonResponseModificationCommand;

class PostProcessorResponseModifyDSLImpl
        extends BaseModifyDSLImpl<JsonResponseModificationCommand>
        implements PostProcessorResponseModifyDSL {

    public PostProcessorResponseModifyDSLImpl(JsonResponseModificationCommand target) {
        super(target);
    }

    public PostProcessorResponseModifyDSLImpl statusCode(int code) {
        target.setStatusCode(code);
        return this;
    }
}
