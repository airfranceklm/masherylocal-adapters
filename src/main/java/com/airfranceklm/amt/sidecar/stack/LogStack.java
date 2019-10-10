package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.JsonHelper;
import com.airfranceklm.amt.sidecar.model.*;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import static com.airfranceklm.amt.sidecar.JsonHelper.toPrettyJSON;

/**
 * The stack that writes the outputs to the <code>proxy_error.log</code> Mashery log file. This stack is useful
 * as a starting point investigation of how the adapter works.
 */
public class LogStack implements AFKLMSidecarStack {

    private static Logger log = LoggerFactory.getLogger(LogStack.class);

    private static AtomicLong counter = new AtomicLong(0);

    @Override
    public SidecarPreProcessorOutput invokeAtPreProcessor(AFKLMSidecarStackConfiguration cfg, SidecarInvocationData cmd, ProcessorServices services) throws IOException {
        write(cmd.getInput());
        return services.doNothingForPreProcessing();
    }

    @Override
    public SidecarPostProcessorOutput invokeAtPostProcessor(AFKLMSidecarStackConfiguration cfg, SidecarInvocationData cmd, ProcessorServices services) throws IOException {
        write(cmd.getInput());
        return services.doNothingForPostProcessing();
    }

    private void write(SidecarInput input)  {

        String s = null;
        try {
            s = toPrettyJSON(input);
        } catch (JsonProcessingException e) {
            s = String.format("Error: %s", e.getMessage());
        }

        log.info(String.format("[--- Sidecar Input Message #%d--------]\n%s",
                counter.incrementAndGet(),
                s));
    }

    @Override
    public AFKLMSidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return new AlwaysValidConfiguration();
    }
}
