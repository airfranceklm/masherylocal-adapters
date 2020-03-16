package com.airfranceklm.amt.testsupport;

import com.mashery.trafficmanager.model.core.Endpoint;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.airfranceklm.amt.testsupport.MasheryEndpointModel.masheryEndpointModel;
import static org.junit.Assert.*;

public class MasheryEndpointModelTest extends EasyMockSupport {

    private MasheryEndpointModel model;

    @Before
    public void init() {
        MasheryEndpointModel.MasheryEndpointModelBuilder b = masheryEndpointModel();

        b.endpointName("unit-test")
                .endpointURI("https://api-unittest.afklm.com/an/api")
                .originURI("https://api-unittset.origin.afklmbackend.com/infra/unit-test-api")
                .originQueryParam("rootPath", "/an/api")
                .originQueryParam("x-unittest", "true")
                .preProcessParam("a", "b")
                .postProcessParam("c", "d");

        model = b.build();
    }

    @Test
    public void testPassesOnNoAction() {
        Endpoint endp = model.mock(this);
        replayAll();
        verifyAll();
    }

    @Test
    public void testCanGetAllValues() {
        Endpoint endp = model.mock(this);
        replayAll();

        assertEquals(MasheryEndpointModel.DEFAULT_ENDPOINT_ID, endp.getExternalID());
        assertNotNull(endp.getAPI());

        assertEquals(MasheryEndpointModel.DEFAULT_SERVICE_ID, endp.getAPI().getExternalID());

        assertNotNull(endp.getProcessor());
        assertNotNull(endp.getProcessor().getPreProcessorParameters());
        assertEquals("b", endp.getProcessor().getPreProcessorParameters().get("a"));

        assertNotNull(endp.getProcessor().getPostProcessorParameters());
        assertEquals("d", endp.getProcessor().getPostProcessorParameters().get("c"));

        verifyAll();
    }

    @Test
    public void testComputeHeadersForOrigin() {
        APIClientRequestModel rm = APIClientRequestModel.apiClientRequest()
                .header("A", "B")
                .header("C", "D")
                .build();

        MasheryEndpointModel mem = MasheryEndpointModel.masheryEndpointModel()
                .masheryAddedHeader("X-A", "xa")
                .masheryDroppedHeader("c")
        .build();

        Map<String,String> mm = mem.computeHeadersForOrigin(rm.getHeaders());
        assertTrue(mm.containsKey("A"));
        assertTrue(mm.containsKey("X-A"));
        assertFalse(mm.containsKey("C"));
    }
}
