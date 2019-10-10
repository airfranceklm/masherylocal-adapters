package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.model.*;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;

import java.io.IOException;

public class AWSSNSStack implements AFKLMSidecarStack {

    @Override
    public SidecarPreProcessorOutput invokeAtPreProcessor(AFKLMSidecarStackConfiguration cfg, SidecarInvocationData cmd, ProcessorServices services) throws IOException {
        return null;
    }

    @Override
    public SidecarPostProcessorOutput invokeAtPostProcessor(AFKLMSidecarStackConfiguration cfg, SidecarInvocationData cmd, ProcessorServices services) throws IOException {
        return null;
    }

    @Override
    public AFKLMSidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return new SNSStackConfig(cfg);
    }

    class SNSStackConfig implements AFKLMSidecarStackConfiguration {

        SNSStackConfig(SidecarConfiguration cfg) {
            // TODO
        }

        @Override
        public boolean isValid() {
            return false;
        }
    }
}
