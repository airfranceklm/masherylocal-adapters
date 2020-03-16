package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.model.SidecarInputRouting;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.Invoke;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildClientRequest;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildEndpoint;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class RoutingHostElementTest extends ElementTestCase<SidecarInputRouting> {

    private static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc) -> {
            buildEndpoint(tc, (cfg) -> cfg
                    .endpointURI("http://api-unittest.afklm.com/travel/")
                    .originURI("https://some.backend.klm.com/api-mgr/travel"));

            buildClientRequest(tc, (cfg) -> cfg.httpVerb("GET"));
        });
    }

    @Test
    public void testAllHeadersRequestAtPre() throws DataElementException {
        ElementDemand cfg = new ElementDemand("routingHost");
        SidecarInvocationData sid = extractFromPreProcessorEvent(dsl, cfg, Invoke);

        final Map<String,Object> params = sid.getInput().getParams();
        assertNotNull(params);
        
        assertEquals(1, params.size());
        assertEquals("some.backend.klm.com", params.get("routingHost"));
    }
}
