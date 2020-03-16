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

public class RequestHeaderSkippingElementTest extends ElementTestCase<HTTPHeaders> {

    private static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc) -> {
            buildClientRequest(tc, (cfg) -> {
                cfg.header("X-Header", "A")
                        .header("X-Header-2", "A2")
                        .header("X-Z", "A3");
            });

            buildEndpoint(tc, (cfg) -> cfg
                    .masheryAddedHeader("X-Mashery-H", "H1")
                    .masheryDroppedHeader("X-Z")
                    .passMessageId(false));
        });
    }

    @Test
    public void testAllHeadersRequestAtPre() throws DataElementException {
        ElementDemand cfg = new ElementDemand("-requestHeaders", "x-header");
        SidecarInvocationData sid = extractFromPreProcessorEvent(dsl, cfg, Invoke);

        assertEquals(2, sid.getInput().getRequest().getHeaders().size());
        assertEquals("A2", sid.getInput().getRequest().getHeaders().get("x-header-2"));
        assertEquals("H1", sid.getInput().getRequest().getHeaders().get("x-mashery-h"));
    }

    @Test
    public void testAllHeadersRequestAtPost() throws DataElementException {
        ElementDemand cfg = new ElementDemand("-requestHeaders", "x-header|X-Header-2");
        SidecarInvocationData sid = extractFromPostProcessorEvent(dsl, cfg, Invoke);

        assertEquals(1, sid.getInput().getRequest().getHeaders().size());

        assertEquals("A3", sid.getInput().getRequest().getHeaders().get("x-z"));
    }

}
