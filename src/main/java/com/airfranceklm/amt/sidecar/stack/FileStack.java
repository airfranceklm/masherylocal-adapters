package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.SidecarInput;
import com.airfranceklm.amt.sidecar.SidecarOutput;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;

import java.io.IOException;

/**
 * File stack, suitable for capturing the asynchronous output of the APIs. Usable at the investigation / PoC stage.
 * Not recommended for production purposes.
 */
public class FileStack implements AFKLMSidecarStack {
    @Override
    public SidecarOutput invoke(AFKLMSidecarStackConfiguration cfg, SidecarInput input) throws IOException {
        // TODO: write the output
        return null;
    }

    @Override
    public AFKLMSidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return new FileStackConfiguration(cfg);
    }

    class FileStackConfiguration implements AFKLMSidecarStackConfiguration {
        private String path;
        private Long maxMessagesPerFile;


        public FileStackConfiguration(SidecarConfiguration cfg) {
            this.path = cfg.getStackParams().get("path");
            final String msgSize = cfg.getStackParams().get("message-per-file");

            if (msgSize != null) {
                this.maxMessagesPerFile = Long.parseLong(msgSize);
            } else {
                this.maxMessagesPerFile = 1000L;
            }
        }

        @Override
        public boolean isValid() {
            return path != null;
        }
    }
}
