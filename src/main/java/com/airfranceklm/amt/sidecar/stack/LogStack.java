package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.AFKLMSidecarProcessor;
import com.airfranceklm.amt.sidecar.SidecarInput;
import com.airfranceklm.amt.sidecar.SidecarOutput;
import com.airfranceklm.amt.sidecar.SidecarOutputImpl;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The stack that writes the outputs to the <code>proxy_error.log</code> Mashery log file. This stack is useful
 * as a starting point investigation of how the adapter works.
 */
public class LogStack implements AFKLMSidecarStack {

    private static Logger log = LoggerFactory.getLogger(LogStack.class);
    private static SidecarOutput noopOutput = new SidecarOutputImpl();
    private static ObjectWriter writer = AFKLMSidecarProcessor.objectMapper.writerWithDefaultPrettyPrinter();

    private static AtomicLong counter = new AtomicLong(0);


    @Override
    public SidecarOutput invoke(AFKLMSidecarStackConfiguration cfg, SidecarInput input) throws IOException {

        String msg = writer.writeValueAsString(input);
        log.info(String.format("[--- Sidecar Input Message #%d--------]\n%s", counter.incrementAndGet(), msg));

        return noopOutput;
    }

    @Override
    public AFKLMSidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return null;
    }
}
