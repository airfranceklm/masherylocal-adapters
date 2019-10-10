package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.impl.model.SidecarPostProcessorOutputImpl;
import com.airfranceklm.amt.sidecar.impl.model.SidecarPreProcessorOutputImpl;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarOutput;
import com.airfranceklm.amt.sidecar.model.SidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import com.airfranceklm.amt.testsupport.RequestCase;

import static org.junit.Assert.fail;

public class SidecarRequestCase extends RequestCase {

    String sidecarException;
    SidecarInput sidecarInput;

    SidecarPreProcessorOutputImpl preProcessorOutput;
    SidecarPostProcessorOutputImpl postProcessorOutput;

    String preflightException;
    SidecarInput preflightInput;
    SidecarPreProcessorOutputImpl preflightOutput;

    SidecarRequestCase() {
        super();
    }

    SidecarRequestCase(String name) {
        super(name);
    }

    @Override
    protected void secondPass() {
        super.secondPass();
        // TODO
//        APIOriginRequest mashReq = getApiOriginRequest();
//
//        if (mashReq != null && mashReq.isExpectOverrideBody()) {
//            mashReq.setExpectOverridingBodyValue(lambdaOutput, );
//        }
    }

    public SidecarInput getOrCreateSidecarInput() {
        if (sidecarInput == null) {
            this.sidecarInput = new SidecarInput();
        }
        return this.sidecarInput;
    }

    public SidecarInput getOrCreatePreflightInput() {
        if (preflightInput == null) {
            this.preflightInput = new SidecarInput();
        }
        return this.preflightInput;
    }

    public SidecarPreProcessorOutputImpl getOrCreateSidecarPreProcessorOutput() {
        if (preProcessorOutput == null) {
            this.preProcessorOutput = new SidecarPreProcessorOutputImpl();
        }
        return this.preProcessorOutput;
    }

    public SidecarPostProcessorOutputImpl getOrCreateSidecarPostProcessorOutput() {
        if (postProcessorOutput == null) {
            this.postProcessorOutput = new SidecarPostProcessorOutputImpl();
        }
        return this.postProcessorOutput;
    }

    public SidecarPreProcessorOutputImpl getOrCreatePreflightOutput() {
        if (preflightOutput == null) {
            this.preflightOutput = new SidecarPreProcessorOutputImpl();
        }
        return this.preflightOutput;
    }
}
