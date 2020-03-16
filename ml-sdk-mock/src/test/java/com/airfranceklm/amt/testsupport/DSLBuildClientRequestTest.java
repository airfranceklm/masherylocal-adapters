package com.airfranceklm.amt.testsupport;

import com.mashery.http.MutableHTTPHeaders;
import com.mashery.http.ParamGroup;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import org.junit.Test;

import static com.airfranceklm.amt.testsupport.MasheryEndpointModel.UNDEFINED_BACKEND_URL;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildClientRequest;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildEndpoint;
import static org.junit.Assert.*;

public class DSLBuildClientRequestTest extends MasheryProcessorTestSupport<MasheryProcessorTestCase> {

    @Test
    public void testCreateClientRequestWithoutOutboundURI() {
        DSL<MasheryProcessorTestCase> dsl = BasicDSL.make().expr((tc) -> {
            buildEndpoint(tc, (cfg) -> cfg.endpointURI("https://unit-test.airfranceklm.com/an/op"));
            buildClientRequest(tc, (cfg) -> cfg.httpVerb("GET").resource("/boo"));
        });

        PreProcessEvent ppe = createPreProcessMock(dsl.build());
        replayAll();

        assertNotNull(ppe);
        assertNotNull(ppe.getServerRequest());
        assertEquals("GET", ppe.getServerRequest().getMethod());
        assertEquals("https://unit-test.airfranceklm.com/an/op/boo", ppe.getServerRequest().getURI());

        assertNotNull(ppe.getClientRequest());
        assertEquals("GET", ppe.getClientRequest().getMethod());
        assertEquals(String.format("%s/boo", UNDEFINED_BACKEND_URL), ppe.getClientRequest().getURI());
    }

    @Test
    public void testSettingUpTheQueryString() {
        DSL<MasheryProcessorTestCase> dsl = BasicDSL.make().expr((tc) -> {
            buildEndpoint(tc, (cfg) -> {
                cfg.endpointURI("https://unit-tests.airfranceklm.com/travel/endpoint");
            });

            buildClientRequest(tc, (cfg) -> {
                cfg.resource("/path/to/op")
                        .queryParam("q1", "p1")
                        .httpVerb("GET");
            });
        });

        PreProcessEvent ppe = createPreProcessMock(dsl.build());
        replayAll();

        ParamGroup pg = ppe.getCallContext().getRequest().getQueryData();
        assertNotNull(pg);
        assertEquals("q1", pg.iterator().next());
        assertEquals("p1", pg.get("q1"));
    }

    @Test
    public void testWillBuildClientRequestOnRemoteIp() {
        DSL<MasheryProcessorTestCase> dsl = BasicDSL.make().expr((tc) -> {
            buildClientRequest(tc, cfg -> cfg.remoteAddr("1.2.3.4"));
        });

        PreProcessEvent ppe = createPreProcessMock(dsl.build());
        replayAll();

        assertNotNull(ppe);
        assertNotNull(ppe.getCallContext());
        assertNotNull(ppe.getCallContext().getRequest());
        assertNotNull(ppe.getCallContext().getRequest().getHTTPRequest());
        assertEquals("1.2.3.4", ppe.getCallContext().getRequest().getHTTPRequest().getRemoteAddr());


        verifyAll();
    }

    @Test
    public void testAddsAndDropsHeadersCorrectly() {
        DSL<MasheryProcessorTestCase> dsl = BasicDSL.make().expr((tc) -> {
            buildClientRequest(tc, (cfg) -> {
                cfg.header("X-Header", "A")
                        .header("X-Header-2", "A2")
                        .header("X-Z", "A3");
            });

            buildEndpoint(tc, (cfg) -> cfg
                    .masheryAddedHeader("X-Mashery-H", "H1")
                    .masheryDroppedHeader("x-Z"));
        });

        PreProcessEvent ppe = createPreProcessMock(dsl.build());
        replayAll();

        assertNotNull(ppe.getClientRequest());
        final MutableHTTPHeaders h = ppe.getClientRequest().getHeaders();
        assertNotNull(h);

        assertEquals("A", h.get("x-header"));
        assertEquals("A2", h.get("x-header-2"));
        assertNull(h.get("x-z"));    // must be dropped
        assertEquals("H1", h.get("x-Mashery-h"));
    }
}
