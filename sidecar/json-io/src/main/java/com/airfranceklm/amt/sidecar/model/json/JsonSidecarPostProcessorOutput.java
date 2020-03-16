package com.airfranceklm.amt.sidecar.model.json;

import com.airfranceklm.amt.sidecar.model.SidecarPostProcessorOutput;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

import static com.airfranceklm.amt.sidecar.CommonExpressions.allocOrGet;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonSidecarPostProcessorOutput extends JsonAbstractSidecarOutput implements SidecarPostProcessorOutput {
    @Getter @Setter
    protected JsonResponseModificationCommand modify;

    public static JsonResponseModificationCommand allocOrGetModify(JsonSidecarPostProcessorOutput t) {
        return allocOrGet(t::getModify, t::setModify, JsonResponseModificationCommand::new);
    }
}
