package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildClientRequest;

public class RequestHeaderElementTest extends ElementTestCase<String> {

    private static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc) -> buildClientRequest(tc, (cfg) -> cfg.header("X-Header", "A")));
    }

    @Test
    public void testHttpHeaderExtraction() throws DataElementException {
        ElementDemand cfg = new ElementDemand("requestHeader", "x-header");
        assertAcceptedForPreAndPost(dsl, cfg, "A", (input) -> input.getRequest().getHeaders().get("x-header"));
    }

    @Test
    public void testHttpVerbExtractionAtPre() throws DataElementException {
        BasicDSL caseDSL = dsl.duplicate();
        caseDSL.expr((tc) -> buildClientRequest(tc, (cfg) -> cfg.header("X-Mashery-Header", "B1")));

        ElementDemand cfg = new ElementDemand("requestHeader", "x-mashery-header");
        assertAcceptedForPre(caseDSL, cfg, "B1", (input) -> input.getRequest().getHeaders().get("x-mashery-header"));
    }

}
