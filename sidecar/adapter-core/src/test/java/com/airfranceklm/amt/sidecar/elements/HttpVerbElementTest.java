package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildClientRequest;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildEndpoint;
import static junit.framework.Assert.assertNull;

public class HttpVerbElementTest extends ElementTestCase<String> {

    private static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc) -> {
            buildEndpoint(tc, (cfg) -> cfg.endpointURI("https://unit-test.airfranceklm.com/an/op"));
            buildClientRequest(tc, (cfg) -> cfg.httpVerb("GET").resource("/boo"));
        });
    }

    @Test
    public void testHttpVerbExtraction() throws DataElementException {
        ElementDemand cfg = new ElementDemand("httpVerb");
        assertAcceptedForPreAndPost(dsl, cfg, "get", (input) -> input.getOperation().getHttpVerb());
    }


}
