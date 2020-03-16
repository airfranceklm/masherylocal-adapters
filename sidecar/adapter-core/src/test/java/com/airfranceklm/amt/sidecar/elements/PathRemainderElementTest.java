package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildClientRequest;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildEndpoint;

public class PathRemainderElementTest extends ElementTestCase<String> {

    private static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc) -> {
            buildEndpoint(tc, (cfg) -> {
                cfg.endpointURI("https://unit-tests.airfranceklm.com/travel/endpoint");
            });

            buildClientRequest(tc, (cfg) -> {
                cfg.resource("/path/to/op");
            });
        });
    }

    @Test
    public void testResourcePathExtraction() throws DataElementException {
        ElementDemand cfg = new ElementDemand("resourcePath");
        assertAcceptedForPreAndPost(dsl, cfg, "/path/to/op", (input) -> input.getOperation().getPath());
    }


}
