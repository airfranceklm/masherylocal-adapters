package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.mashery.http.HTTPHeaders;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.Invoke;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildClientRequest;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildEndpoint;
import static junit.framework.Assert.assertEquals;

public class AllRequestHeaderElementTest extends ElementTestCase<HTTPHeaders> {

    private static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc) -> {

            buildEndpoint(tc, (cfg) -> cfg.masheryDroppedHeader("X-Z")
                    .masheryAddedHeader("X-Mashery-H", "H1")
                    .passMessageId(false));

            buildClientRequest(tc, (cfg) -> {
                cfg.header("X-Header", "A")
                        .header("X-Header-2", "A2")
                        .header("X-Z", "A3");
            });
        });
    }

    @Test
    public void testAllHeadersRequestAtPre() throws DataElementException {
        ElementDemand cfg = new ElementDemand("+requestHeaders");
        SidecarInvocationData sid = extractFromPreProcessorEvent(dsl, cfg, Invoke);

        assertEquals(4, sid.getInput().getRequest().getHeaders().size());
        assertEquals("A", sid.getInput().getRequest().getHeaders().get("x-header"));
        assertEquals("A2", sid.getInput().getRequest().getHeaders().get("x-header-2"));
        assertEquals("A3", sid.getInput().getRequest().getHeaders().get("x-z"));
        assertEquals("H1", sid.getInput().getRequest().getHeaders().get("x-mashery-h"));
    }

    @Test
    public void testAllHeadersRequestAtPost() throws DataElementException {
        ElementDemand cfg = new ElementDemand("+requestHeaders");
        SidecarInvocationData sid = extractFromPostProcessorEvent(dsl, cfg, Invoke);

        assertEquals("A", sid.getInput().getRequest().getHeaders().get("x-header"));
        assertEquals("A2", sid.getInput().getRequest().getHeaders().get("x-header-2"));
        assertEquals("A3", sid.getInput().getRequest().getHeaders().get("x-z"));
    }

}
