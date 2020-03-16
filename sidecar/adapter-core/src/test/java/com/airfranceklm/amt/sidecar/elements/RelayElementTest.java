package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import org.junit.BeforeClass;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildAPIOriginResponse;

public class RelayElementTest {

    static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc) -> {
            buildAPIOriginResponse(tc, (cfg) -> cfg.header("Content-Type", "text/plain").payload("+== RESPONSE PAYLOAD ==+"));
        });
        // TODO.
    }
}
