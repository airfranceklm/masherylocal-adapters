package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.model.SidecarInputHTTPMessage;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.Invoke;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.*;
import static org.junit.Assert.*;

public class ResponsePayloadElementTest extends ElementTestCase<ProcessorEvent> {

    private static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc) -> buildAPIOriginResponse(tc, (cfg) -> cfg
                .header("Content-Type", "text/plain")
                .payload("+== RESPONSE PAYLOAD ==+")));
    }

    @Test
    public void testExtractionWithPlainTypeAtPostProcessor() throws DataElementException {
        ElementDemand cfg = new ElementDemand("responsePayload");
        SidecarInvocationData sid = extractFromPostProcessorEvent(dsl, cfg, Invoke);

        assertPlainTextPayload(sid);
    }

    protected void assertPlainTextPayload(SidecarInvocationData sid) {
        final SidecarInputHTTPMessage res = sid.getInput().getResponse();
        assertNotNull(res);
        assertNull(res.getPayloadLength());
        assertNull(res.getPayloadBase64Encoded());
        assertEquals("+== RESPONSE PAYLOAD ==+", res.getPayload());
    }

    @Test
    public void testExtractionWithEncodedTypeAtPostProcessor() throws DataElementException {
        BasicDSL caseDSL = dsl.duplicate();
        caseDSL.expr((tc) -> buildAPIOriginResponse(tc, (c) -> c.header("Content-Encoding", "custom")));

        ElementDemand cfg = new ElementDemand("responsePayload");
        SidecarInvocationData sid = extractFromPostProcessorEvent(caseDSL, cfg, Invoke);

        assertBase64EncodedPayload(sid);
    }

    private void assertBase64EncodedPayload(SidecarInvocationData sid) {
        final SidecarInputHTTPMessage res = sid.getInput().getResponse();
        assertNotNull(res);
        assertEquals(new Long(24), res.getPayloadLength());
        assertTrue(res.getPayloadBase64Encoded());
        assertEquals("Kz09IFJFU1BPTlNFIFBBWUxPQUQgPT0r", res.getPayload());
    }
}
