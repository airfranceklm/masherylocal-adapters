package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.model.SidecarInputRouting;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.Invoke;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildClientRequest;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildEndpoint;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class RoutingElementTest extends ElementTestCase<SidecarInputRouting> {

    private static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc)-> {
            buildEndpoint(tc, (cfg) -> cfg
                    .endpointURI("http://api-unittest.afklm.com/travel/")
                    .originURI("https://some.backend.klm.com/api-mgr/travel"));

            buildClientRequest(tc, (cfg) -> cfg
                    .httpVerb("GET")
                    .resource("/op"));
        });

    }

    @Test
    public void testAllHeadersRequestAtPre() throws DataElementException {
        ElementDemand cfg = new ElementDemand("routing");
        SidecarInvocationData sid = extractFromPreProcessorEvent(dsl, cfg, Invoke);

        final SidecarInputRouting routing = sid.getInput().getRouting();
        assertNotNull(routing);

        assertEquals("https://some.backend.klm.com/api-mgr/travel/op", routing.getUri());
        assertEquals("get", routing.getHttpVerb());
    }
}
