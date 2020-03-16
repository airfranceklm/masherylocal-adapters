package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.DSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.Invoke;
import static junit.framework.Assert.assertEquals;

public class EndpointIdentificationElementTest extends ElementTestCase<String> {

    private static DSL<MasheryProcessorTestCase> dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make().identifyEndpoint();
    }

    @Test
    public void testAllHeadersRequestAtPre() throws DataElementException {
        ElementDemand cfg = new ElementDemand("endpointIdentification");
        SidecarInvocationData sid = extractFromPreProcessorEvent(dsl, cfg, Invoke);

        assertEquals("aServiceId", sid.getInput().getServiceId());
        assertEquals("anEndpointId", sid.getInput().getEndpointId());
    }

    @Test
    public void testAllHeadersRequestAtPost() throws DataElementException {
        ElementDemand cfg = new ElementDemand("endpointIdentification");
        SidecarInvocationData sid = extractFromPostProcessorEvent(dsl, cfg, Invoke);

        assertEquals("aServiceId", sid.getInput().getServiceId());
        assertEquals("anEndpointId", sid.getInput().getEndpointId());
    }


}
