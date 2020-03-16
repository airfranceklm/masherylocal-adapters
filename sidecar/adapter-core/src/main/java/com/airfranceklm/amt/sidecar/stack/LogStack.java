package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import static com.airfranceklm.amt.sidecar.JsonHelper.toPrettyJSON;

/**
 * The stack that writes the outputs to the <code>proxy_error.log</code> Mashery log file. This stack is useful
 * as a starting point investigation of how the adapter works.
 */
@Slf4j
public class LogStack extends CommonStack {

    public static final String STACK_NAME = "log";

    private static AtomicLong counter = new AtomicLong(0);

    @Override
    public String getStackName() {
        return STACK_NAME;
    }

    @Override
    public <T extends CallModificationCommand, U extends SidecarOutput<T>> U invoke(SidecarStackConfiguration cfg, SidecarInvocationData cmd, Class<U> expectedType) throws IOException {
        write(cmd.getInput());
        return getProcessorServices().doNothing(expectedType);
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
    public SidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return new AlwaysValidSidecarConfiguration();
    }
}
