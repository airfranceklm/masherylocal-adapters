package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.dsl.SidecarPostProcessorOutputDSL;
import com.airfranceklm.amt.sidecar.dsl.SidecarPreProcessorOutputDSL;
import com.airfranceklm.amt.sidecar.model.*;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

/**
 * An echo stack that replies with inputs it receives.
 * <p>
 * <b>THIS STACK HAS DEVELOPMENT PURPOSE ONLY</b>. The motivation for this stack is to provide an easy
 * visibility into sidecar inputs that the API traffic will generate for a particular API in the early stages of
 * sidecar development project for a given endpoint. After the project will gain sufficient understanding of
 * how API inputs are converted into sidecar outputs, it will be replaced with a different stack that will communicate
 * with the actually developed sidecar.
 * </p>
 */
public class EchoStack extends CommonStack {

    public static final String STACK_NAME = "echo";

    @Override
    public String getStackName() {
        return STACK_NAME;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CallModificationCommand, U extends SidecarOutput<T>> U invoke(SidecarStackConfiguration cfg, SidecarInvocationData cmd, Class<U> expectedType) throws IOException {

        ResponseBean rb = new ResponseBean();
        rb.checksum = cmd.getInput().getInputChecksum();
        rb.input = cmd.getInput();

        if (SidecarPreProcessorOutput.class.isAssignableFrom(expectedType)) {

            SidecarPreProcessorOutputDSL dsl = getProcessorServices().atPreProcessorPoint();
            dsl.reply((reply) -> reply.statusCode(200)
                    .passHeader("X-AFKLM-Stack", STACK_NAME)
                    .passJsonData(getProcessorServices().toMap(rb)));

            return (U)dsl.output();
        } else if (SidecarPostProcessorOutput.class.isAssignableFrom(expectedType)) {

            SidecarPostProcessorOutputDSL dsl = getProcessorServices().atPostProcessorPoint();
            dsl.modify((mod) -> mod.statusCode(200)
                    .passHeader("X-AFKLM-Stack", STACK_NAME)
                    .passJsonData(getProcessorServices().toMap(rb)));

            return (U) dsl.output();
        }

        throw new IOException(String.format("Unknown type: %s", expectedType.getName()));
    }


    @Override
    public SidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return new AlwaysValidSidecarConfiguration();
    }

    static class ResponseBean {
        @Getter @Setter
        String checksum;
        @Getter @Setter
        SidecarInput input;
    }
}
