package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.RequestModificationCommand;
import com.airfranceklm.amt.sidecar.model.json.JsonRequestModificationCommand;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SidecarPreProcessorMockData extends SidecarInvocationPointMockData<RequestModificationCommand, JsonSidecarPreProcessorOutput> {

    public JsonSidecarPreProcessorOutput getOutput() {
        return this.output; // Should not be called
    }

    public void setOutput(JsonSidecarPreProcessorOutput o) {
        this.output = o;
    }

    @Override
    protected JsonSidecarPreProcessorOutput create() {
        return new JsonSidecarPreProcessorOutput();
    }
}
