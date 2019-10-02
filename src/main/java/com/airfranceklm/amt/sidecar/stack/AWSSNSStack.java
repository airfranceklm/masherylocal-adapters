package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.SidecarInput;
import com.airfranceklm.amt.sidecar.SidecarOutput;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;

import java.io.IOException;

public class AWSSNSStack implements AFKLMSidecarStack {
    @Override
    public SidecarOutput invoke(AFKLMSidecarStackConfiguration cfg, SidecarInput input) throws IOException {
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
