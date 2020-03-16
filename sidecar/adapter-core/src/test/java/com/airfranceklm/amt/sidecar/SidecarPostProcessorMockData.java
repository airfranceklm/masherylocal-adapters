package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.ResponseModificationCommand;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPostProcessorOutput;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SidecarPostProcessorMockData extends SidecarInvocationPointMockData<ResponseModificationCommand, JsonSidecarPostProcessorOutput> {

    public JsonSidecarPostProcessorOutput getOutput() {
        return output;
    }

    public void setOutput(JsonSidecarPostProcessorOutput o) {
        this.output = o;
    }

    @Override
    protected JsonSidecarPostProcessorOutput create() {
        return new JsonSidecarPostProcessorOutput();
    }
}
