package com.airfranceklm.amt.testsupport;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Basic DSL
 */
public class BasicDSL extends DSL<MasheryProcessorTestCase> {

    private static final BasicDSL emptyDSL = new BasicDSL();

    @Override
    protected MasheryProcessorTestCase create() {
        return new MasheryProcessorTestCase();
    }

    public BasicDSL duplicate() {
        BasicDSL retVal = new BasicDSL();
        retVal.copy(this);

        return retVal;
    }

    public static BasicDSL make() {
        return new BasicDSL();
    }

    public static BasicDSL emptyDSL() {
        return emptyDSL;
    }
}
