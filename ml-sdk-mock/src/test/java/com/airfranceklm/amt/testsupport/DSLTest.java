package com.airfranceklm.amt.testsupport;

import org.junit.Test;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildClientRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DSLTest {
    @Test
    public void testBasicDSL() {
        BasicDSL dsl = BasicDSL.make();
        dsl.expr((tc) -> {
            buildClientRequest(tc, (appReq) -> appReq
                    .httpVerb("POST")
                    .header("A", "B")
                    .remoteAddr("127.0.0.1")
                    .payload("PAYLOAD"));
        });

        MasheryProcessorTestCase t = dsl.build();
        final APIClientRequestModel cl = t.getClientRequest();
        assertNotNull(cl);
        assertEquals("POST", cl.getHttpVerb());
        assertEquals("127.0.0.1", cl.getRemoteAddr());
        assertEquals("B", cl.getHeaders().get("A"));
        assertEquals("PAYLOAD", cl.getPayload());

        BasicDSL copy = dsl.duplicate();
        copy.expr((tc) -> buildClientRequest(tc, (appReq) -> appReq.httpVerb("GET")));

        MasheryProcessorTestCase t1 = copy.build();
        final APIClientRequestModel cl1 = t1.getClientRequest();
        assertNotNull(cl1);
        assertEquals("GET", cl1.getHttpVerb());
        assertEquals("127.0.0.1", cl1.getRemoteAddr());
        assertEquals("B", cl1.getHeaders().get("A"));
        assertEquals("PAYLOAD", cl1.getPayload());

    }
}
