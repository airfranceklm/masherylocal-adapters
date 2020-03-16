package com.airfranceklm.amt.testsupport;

import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.*;
import static org.junit.Assert.*;

public class MasheryPreProcessorTestSupportTest extends MasheryProcessorTestSupport<MasheryProcessorTestCase> {

    static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();

        dsl.expr((tc) -> {

            buildEndpoint(tc, (cfg) -> {
                cfg.serviceId("unitTestServiceId").endpointURI("unitTestEndpointId")
                        .endpointURI("http://api-unitttest.airfranceklm.com/a/unittest/api")
                        .originURI("http://origin-api-unittest.klm.salesforce.com/apex/infra")
                        .masheryAddedHeader("X-ConsumerId", "EZK")
                        .masheryDroppedHeader("Authorization")
                        .masheryDroppedHeader("X-ConsumerId");
            });

            buildClientRequest(tc, (cfg) -> {
                cfg.httpVerb("GET")
                        .remoteAddr("192.168.0.1")
                        .resource("/an/op")
                        .header("Accept", "application/json")
                        .header("Authorization", "Bearer fake-access-token");
            });

            buildApplication(tc, (cfg) -> cfg.name("Unit Test App").eav("UT","Yes"));

            buildPackageKey(tc, (cfg) -> cfg.packageKey("af5d").eav("UTK", "NO"));

            buildAuthorizationContext(tc, (cfg) -> cfg.grantType("password"));
        });
    }


    @Test
    public void testCreateAndDoNothing() {
        PreProcessEvent ppe = createPreProcessMock(dsl.build());
        replayAll();

        assertNotNull(ppe);
        verifyAll();
    }

    @Test
    public void testCreateAndCheckBasicOptions() {
        PreProcessEvent ppe = createPreProcessMock(dsl.build());
        replayAll();

        assertNotNull(ppe);
        assertNotNull(ppe.getServerRequest());
        assertNotNull(ppe.getClientRequest());

        assertEquals(PreProcessEvent.EVENT_TYPE, ppe.getType());

        assertNotNull(ppe.getAuthorizationContext());
        assertNotNull(ppe.getCache());
        assertNotNull(ppe.getCallContext());
        assertNotNull(ppe.getCallContext().getRequest());
        assertSame(ppe.getServerRequest(), ppe.getCallContext().getRequest().getHTTPRequest());

        assertNotNull(ppe.getCallContext().getResponse());

        assertNotNull(ppe.getDebugContext());
        assertNotNull(ppe.getEndpoint());
        assertNotNull(ppe.getKey());
        verifyAll();
    }
}
