package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.model.RequestModificationCommand;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarOutputTerminationCommand;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;

import static com.airfranceklm.amt.sidecar.CommonExpressions.allocOrGet;
import static com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput.allocOrGetModify;
import static com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput.allocOrGetReply;

public class SidecarPreProcessorOutputDSLImpl extends BasicSidecarOutputDSLImpl<RequestModificationCommand
        , SidecarPreProcessorOutput
        , JsonSidecarPreProcessorOutput>

        implements SidecarPreProcessorOutputDSL {

    public SidecarPreProcessorOutputDSLImpl(JsonSidecarPreProcessorOutput data) {
        super(data);
    }

    @Override
    public ReplyCommandDSL reply() {
        return new ReplyCommandDSLImpl(allocOrGetReply(bean()));
    }

    @Override
    public PreProcessorRequestModifyDSL modify() {
        return new PreProcessorRequestModifyDSLImpl(allocOrGetModify(bean()));
    }

    @Override
    public TerminateDSL terminate() {
        return new TerminateDSLImpl(allocOrGet(bean()::getTerminate, bean()::setTerminate, JsonSidecarOutputTerminationCommand::new));
    }

}
