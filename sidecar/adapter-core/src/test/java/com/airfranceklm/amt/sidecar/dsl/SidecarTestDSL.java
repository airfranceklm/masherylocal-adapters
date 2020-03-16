package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.SidecarInvocationTestCase;
import com.airfranceklm.amt.testsupport.DSL;

public class SidecarTestDSL extends DSL<SidecarInvocationTestCase> {

    @Override
    protected SidecarInvocationTestCase create() {
        return new SidecarInvocationTestCase();
    }

    public SidecarTestDSL duplicate() {
        SidecarTestDSL retVal = new SidecarTestDSL();
        retVal.copy(this);

        return retVal;
    }

    /**
     * Creates an instnace of this DSL.
     * @return empty instance of DSL class.
     */
    public static SidecarTestDSL make() {
        return new SidecarTestDSL();
    }
}
