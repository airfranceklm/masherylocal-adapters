package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.builders.PreFlightSidecarInputBuilder;
import com.airfranceklm.amt.sidecar.builders.PreProcessSidecarInputBuilder;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;
import lombok.Getter;
import lombok.Setter;

public class PreProcessorSidecarRuntime {

    @Getter @Setter
    private JsonSidecarPreProcessorOutput staticModification;
    @Getter @Setter
    private PreFlightSidecarInputBuilder preflightBuilder;
    @Getter @Setter
    private PreProcessSidecarInputBuilder preProcessBuilder;

    @Getter @Setter
    private boolean executePostProcessing = false;

    public boolean demandsPreflightHandling() {
        return preflightBuilder != null;
    }

    public boolean demandsSidecarHandling() {
        return preProcessBuilder != null;
    }
}
