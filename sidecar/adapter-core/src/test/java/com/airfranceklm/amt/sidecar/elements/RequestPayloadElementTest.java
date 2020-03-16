package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.model.SidecarInputHTTPMessage;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.Invoke;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildClientRequest;
import static org.junit.Assert.*;

public class RequestPayloadElementTest extends ElementTestCase<ProcessorEvent> {

    private static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc) -> {
            buildClientRequest(tc, (cfg) -> cfg
                    .httpVerb("POST")
                    .header("Content-Type", "text/plain")
                    .payload("+== PAYLOAD ==+"));
        });
    }

    @Test
    public void testExtractionWithPlainType() throws DataElementException {
        ElementDemand cfg = new ElementDemand("requestPayload");
        SidecarInvocationData sid = extractFromPreProcessorEvent(dsl, cfg, Invoke);

        assertPlainTextPayload(sid);
    }

    @Test
    public void testExtractionWithPlainTypeAtPostProcessor() throws DataElementException {
        ElementDemand cfg = new ElementDemand("requestPayload");
        SidecarInvocationData sid = extractFromPostProcessorEvent(dsl, cfg, Invoke);

        assertPlainTextPayload(sid);
    }

    protected void assertPlainTextPayload(SidecarInvocationData sid) {
        final SidecarInputHTTPMessage req = sid.getInput().getRequest();
        assertNotNull(req);
        assertNull(req.getPayloadLength());
        assertNull(req.getPayloadBase64Encoded());
        assertEquals("+== PAYLOAD ==+", req.getPayload());
    }

    @Test
    public void testExtractionWithEncodedType() throws DataElementException {
        BasicDSL caseDSL = dsl.duplicate();
        caseDSL.expr((tc) -> buildClientRequest(tc, (c) -> c.header("Content-Encoding", "custom")));

        ElementDemand cfg = new ElementDemand("requestPayload");
        SidecarInvocationData sid = extractFromPreProcessorEvent(caseDSL, cfg, Invoke);

        assertBase64EncodedPayload(sid);
    }

    @Test
    public void testExtractionWithEncodedTypeAtPostProcessor() throws DataElementException {
        BasicDSL caseDSL = dsl.duplicate();
        caseDSL.expr((tc) -> buildClientRequest(tc, (c) -> c.header("Content-Encoding", "custom")));

        ElementDemand cfg = new ElementDemand("requestPayload");
        SidecarInvocationData sid = extractFromPostProcessorEvent(caseDSL, cfg, Invoke);

        assertBase64EncodedPayload(sid);
    }

    private void assertBase64EncodedPayload(SidecarInvocationData sid) {
        final SidecarInputHTTPMessage req = sid.getInput().getRequest();
        assertNotNull(req);
        assertEquals(new Long(15), req.getPayloadLength());
        assertTrue(req.getPayloadBase64Encoded());
        assertEquals("Kz09IFBBWUxPQUQgPT0r", req.getPayload());
    }
}
