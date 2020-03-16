package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.model.ResponseModificationCommand;
import com.airfranceklm.amt.sidecar.model.SidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.json.JsonResponseModificationCommand;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarOutputTerminationCommand;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPostProcessorOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static com.airfranceklm.amt.sidecar.CommonExpressions.allocOrGet;
import static com.airfranceklm.amt.sidecar.model.json.JsonSidecarPostProcessorOutput.allocOrGetModify;

public class SidecarPostProcessorOutputDSLImpl
        extends BasicSidecarOutputDSLImpl<ResponseModificationCommand
        , SidecarPostProcessorOutput
        , JsonSidecarPostProcessorOutput>
        implements SidecarPostProcessorOutputDSL {

    public SidecarPostProcessorOutputDSLImpl(JsonSidecarPostProcessorOutput data) {
        super(data);
    }

    @Override
    public PostProcessorResponseModifyDSL modify() {
        return new PostProcessorResponseModifyDSLImpl(allocOrGetModify(bean()));
    }

    @Override
    public TerminateDSL terminate() {
        return new TerminateDSLImpl(allocOrGet(bean()::getTerminate, bean()::setTerminate, JsonSidecarOutputTerminationCommand::new));
    }
}
