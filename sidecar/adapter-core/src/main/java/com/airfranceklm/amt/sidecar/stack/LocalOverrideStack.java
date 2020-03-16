package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.*;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarOutputTerminationCommand;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;

import java.io.IOException;

/**
 * Local override stack. The stack will be specified in SaaS configuration and will always trigger failures unless
 * the deployer will supply a replacement local configuration.
 */
public class LocalOverrideStack extends CommonStack {

    public static final String STACK_NAME = "local-override";

    private static SidecarStackConfiguration STACK_CFG = new AlwaysValidSidecarConfiguration();

    private static JsonSidecarPreProcessorOutput preOutput;
    private static JsonSidecarPostProcessorOutput postOutput;

    static {
        preOutput = new JsonSidecarPreProcessorOutput();
        preOutput.setTerminate(new JsonSidecarOutputTerminationCommand(597, "Service requires host-specific configuration"));

        postOutput = new JsonSidecarPostProcessorOutput();
        postOutput.setTerminate(new JsonSidecarOutputTerminationCommand(597, "Service requires host-specific configuration"));
    }

    @Override
    public String getStackName() {
        return STACK_NAME;
    }

    @Override @SuppressWarnings("unchecked")
    public <T extends CallModificationCommand, U extends SidecarOutput<T>> U invoke(SidecarStackConfiguration cfg, SidecarInvocationData cmd, Class<U> expectedType) throws IOException {
        if (expectedType.isInstance(preOutput)) {
            return (U)preOutput;
        } else if (expectedType.isInstance(postOutput)) {
            return (U)postOutput;
        } else {
            throw new IllegalArgumentException(String.format("Unsupported return type: %s", expectedType.getName()));
        }
    }

    @Override
    public SidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return STACK_CFG;
    }
}
