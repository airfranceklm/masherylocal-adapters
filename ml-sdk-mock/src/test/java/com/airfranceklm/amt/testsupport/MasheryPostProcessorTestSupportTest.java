package com.airfranceklm.amt.testsupport;

import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.*;
import static org.junit.Assert.*;

public class MasheryPostProcessorTestSupportTest extends MasheryProcessorTestSupport<MasheryProcessorTestCase> {

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

            buildAPIOriginResponse(tc, (cfg) -> {
                cfg.header("Content-Type", "text/plain")
                        .payload("Sample")
                        .statusCode(200)
                        ;
            });
        });
    }


    @Test
    public void testCreateAndDoNothing() {
        PostProcessEvent ppe = createPostProcessMock(dsl.build());
        replayAll();

        assertNotNull(ppe);
        verifyAll();
    }

    @Test
    public void testCreateAndCheckBasicOptions() {
        PostProcessEvent ppe = createPostProcessMock(dsl.build());
        replayAll();

        assertNotNull(ppe);
        assertNotNull(ppe.getClientResponse());
        assertNotNull(ppe.getServerResponse());

        assertEquals(PostProcessEvent.EVENT_TYPE, ppe.getType());

        assertNotNull(ppe.getAuthorizationContext());
        assertNotNull(ppe.getCache());
        assertNotNull(ppe.getCallContext());
        assertNotNull(ppe.getCallContext().getRequest());

        assertNotNull(ppe.getCallContext().getResponse());

        assertNotNull(ppe.getDebugContext());
        assertNotNull(ppe.getEndpoint());
        assertNotNull(ppe.getKey());

        verifyAll();
    }
}
