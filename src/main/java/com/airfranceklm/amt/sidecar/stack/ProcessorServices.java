package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.model.SidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;

import java.io.IOException;

/**
 * Processor services available to {@link com.airfranceklm.amt.sidecar.stack.AFKLMSidecarStack} stacks.
 */
public interface ProcessorServices {
    SidecarPreProcessorOutput asPreProcessor(String jawJSON) throws IOException;
    SidecarPostProcessorOutput asPostProcessor(String jawJSON) throws IOException;

    SidecarPreProcessorOutput doNothingForPreProcessing();
    SidecarPostProcessorOutput doNothingForPostProcessing();
}
