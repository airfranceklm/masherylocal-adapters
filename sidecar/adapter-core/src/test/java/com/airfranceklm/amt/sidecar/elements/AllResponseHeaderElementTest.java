package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import com.mashery.http.HTTPHeaders;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.Invoke;
import static junit.framework.Assert.assertEquals;

public class AllResponseHeaderElementTest extends ElementTestCase<HTTPHeaders> {

    private static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc) -> {
            MasheryProcessorTestCaseAccessor.buildAPIOriginResponse(tc, (cfg) -> {
                cfg.header("X-Header-A1", "A1")
                        .header("X-Header-A2", "A2");
            });
        });
    }

    @Test
    public void testAllHeadersRequestAtPre() throws DataElementException {
        ElementDemand cfg = new ElementDemand("+responseHeaders");
        SidecarInvocationData sid = extractFromPostProcessorEvent(dsl, cfg, Invoke);

        assertEquals(2, sid.getInput().getResponse().getHeaders().size());
        assertEquals("A1", sid.getInput().getResponse().getHeaders().get("x-header-a1"));
        assertEquals("A2", sid.getInput().getResponse().getHeaders().get("x-header-a2"));
    }


}
