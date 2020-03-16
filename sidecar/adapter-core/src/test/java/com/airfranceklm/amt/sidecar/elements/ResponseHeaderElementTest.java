package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildAPIOriginResponse;

public class ResponseHeaderElementTest extends ElementTestCase<String> {

    private static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc) -> buildAPIOriginResponse(tc, (c) -> c.header("X-Header", "A1")));
    }

    @Test
    public void testHttpResponseHeaderExtraction() throws DataElementException {
        ElementDemand cfg = new ElementDemand("responseHeader", "x-header");
        assertAcceptedForPost(dsl, cfg, "A1", (input) -> input.getResponse().getHeaders().get("x-header"));
    }

}
