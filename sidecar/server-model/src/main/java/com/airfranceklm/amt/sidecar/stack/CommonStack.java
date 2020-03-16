package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.ProcessorServices;
import com.airfranceklm.amt.sidecar.identity.ProcessorKeySet;
import lombok.Getter;

/**
 * A base implementation for a stack that performs inputs/outputs. This class should be subclassed by stacks writing
 * to the local machine or trusted network where the application of ALCP algorithm won't be required.
 * <p/>
 * Production-grade stacks should subclass {@link ALCPEnabledStack} to get the benefit of the ALCP wrapping and
 * unwrapping.
 */
public abstract class CommonStack implements SidecarStack {
    @Getter
    private ProcessorServices processorServices;

    @Getter
    private ProcessorKeySet alcpIdentities;

    @Override
    public void useProcessorServices(ProcessorServices ps) {
        this.processorServices = ps;
    }

    public void useAlcpIdentities(ProcessorKeySet ci) {
        this.alcpIdentities = ci;
    }

}
