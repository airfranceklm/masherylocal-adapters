package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.ProcessorServices;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.*;

import java.io.IOException;

/**
 * File stack, suitable for capturing the asynchronous output of the APIs. Usable at the investigation / PoC stage.
 * Not recommended for production purposes.
 */
public class FileStack extends CommonStack {

    public static final String STACK_NAME = "fs";
    private ProcessorServices processorServices;

    @Override
    public String getStackName() {
        return STACK_NAME;
    }

    @Override
    public <T extends CallModificationCommand, U extends SidecarOutput<T>> U invoke(SidecarStackConfiguration cfg, SidecarInvocationData cmd, Class<U> expectedType) throws IOException {
        // TODO: Log the output.
        return processorServices.doNothing(expectedType);
    }

    @Override
    public SidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return new FileStackConfiguration(cfg);
    }

    class FileStackConfiguration implements SidecarStackConfiguration {
        private String path;
        private Long maxMessagesPerFile;


        public FileStackConfiguration(SidecarConfiguration cfg) {
            this.path = cfg.getStack().getParams().get("path");
            final String msgSize = cfg.getStack().getParams().get("message-per-file");

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
