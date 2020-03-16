package com.airfranceklm.amt.sidecar;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SidecarInteractions {
    @JsonProperty("pre-flight")
    private SidecarPreProcessorMockData preflight;
    @JsonProperty("pre-processor")
    private SidecarPreProcessorMockData preProcessor;
    @JsonProperty("post-processor")
    private SidecarPostProcessorMockData postProcessor;

}
