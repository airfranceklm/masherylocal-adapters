package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.testsupport.RequestCase;

import static org.junit.Assert.fail;

public class SidecarRequestCase extends RequestCase {

    String sidecarException;
    SidecarInput sidecarInput;
    SidecarOutput sidecarOutput;

    String preflightException;
    SidecarInput preflightInput;
    SidecarOutput preflightOutput;

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

    public SidecarOutputImpl getOrCreateSidecarOutput() {
        if (sidecarOutput == null) {
            this.sidecarOutput = new SidecarOutputImpl();
        }
        return (SidecarOutputImpl)this.sidecarOutput;
    }

    public SidecarOutputImpl getOrCreatePreflightOutput() {
        if (preflightOutput == null) {
            this.preflightOutput = new SidecarOutputImpl();
        }
        return (SidecarOutputImpl)this.preflightOutput;
    }
}
