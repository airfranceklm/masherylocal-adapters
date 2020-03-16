package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.dsl.SidecarTestDSL;
import com.airfranceklm.amt.sidecar.model.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.model.SidecarSynchronicity;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.sidecar.SidecarInvocationTestCaseAccessor.*;
import static com.airfranceklm.amt.sidecar.SidecarProcessor.MSG_SERVICE_CANNOT_BE_PROVIDED;
import static com.airfranceklm.amt.sidecar.SidecarProcessor.formatCurtailedMessage;
import static com.airfranceklm.amt.sidecar.model.json.JsonRequestRoutingChangeBean.routeToHost;
import static com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput.preOut;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.*;

/**
 * Tests of the pre-flight processing checks.
 */
public class PreFlightChecksTests extends SidecarMockSupport {

    private static SidecarTestDSL baseDSL;

    @BeforeClass
    public static void createDSL() {

        baseDSL = new SidecarTestDSL();
        baseDSL.identifyEndpoint().expr((tc) -> {
            buildEndpoint(tc, (cfg) -> {
                cfg.endpointURI("https://api-unitttest.airfranceklm.com/travel/")
                        .originURI("https://internal-hosting.host.klm/pathto/provivider")
                        .preProcessParam("synchronicity", "event")
                        .preProcessParam("request-headers", "a|x-preflight")
                        .preProcessParam("preflight-enabled", "true");
            });

            buildClientRequest(tc, (cfg) -> {
                cfg.remoteAddr("1.2.3.4")
                        .httpVerb("GET")
                        .header("a", "B")
                        .resource("/unitTest")
                        .queryParam("myQuery", "123");
            });

            buildPackageKey(tc, (cfg) -> cfg.packageKey("pk12345"));
            buildApplication(tc, (cfg) -> cfg.name("Boo").eav("a", "B"));

            buildPreflightInput(tc, (cfg) -> {
                cfg.synchronicity(SidecarSynchronicity.RequestResponse)
                        .point(SidecarInputPoint.Preflight)
                        .serviceId("aServiceId")
                        .endpointId("anEndpointId");
            });
        });
    }

    public PreFlightChecksTests() {
        super();
    }

    @Test
    public void testPreflightCanCancelProcessing() {
        SidecarTestDSL dsl = baseDSL.duplicate();
        dsl.expr((tc) -> {
            final JsonSidecarPreProcessorOutput v = preOut();
            v.terminate((cfg) -> cfg.statusCode(403));

            tc.setPreflightOutput(v);
        });

        dsl.expectCurtailingWith(403, formatCurtailedMessage(MSG_SERVICE_CANNOT_BE_PROVIDED));
        // Is this the right message?

        verifyPreProcessorCase(dsl.build());
    }

    @Test
    public void testPreflightChangesRouting() {
        SidecarTestDSL dsl = baseDSL.duplicate();

        dsl.expr((tc) -> {
            final JsonSidecarPreProcessorOutput v = preOut();
            v.modify((cfg) -> cfg.changeRoute(routeToHost("internal-hosting-updated.host.klm")));
            tc.setPreflightOutput(v);

            buildAPIOriginRequestModification(tc, (cfg) -> {
                cfg.expectSetUri("https://internal-hosting-updated.host.klm/pathto/provivider/unitTest?myQuery=123");
            });

            buildAPIOriginResponse(tc, (cfg) -> cfg.header("x-resp", "boo")
                    .payload("payload")
                    .statusCode(201));

            buildMasheryResponse(tc, (cfg) -> cfg.statusCode(201)
                    .statusMessage("Code 201")
                    .header("x-resp", "boo")
                    .payload("payload")
                    .expectSetComplete(true));
        });


        SidecarInvocationTestCase src = dsl.build();
        verifyPreProcessorCase(src);
    }

    @Test
    public void testPreflightAddsHeaders() {
        SidecarTestDSL dsl = baseDSL.duplicate();
        dsl.expr((tc) -> {

            buildPreflightOutput(tc, (preOut) -> {
                preOut.modify((cfg) -> cfg.passHeader("x-preflight", "preflight-passing"));
            });

            buildPreProcessorInput(tc, (c) -> {
                c.synchronicity(SidecarSynchronicity.Event)
                        .point(SidecarInputPoint.PreProcessor)
                        .serviceId("aServiceId")
                        .endpointId("anEndpointId");

                c.request(sidecarHTTPInput((h) -> h
                        .header("a", "B")
                        .header("x-preflight", "preflight-passing")));
            });


            buildAPIOriginRequestModification(tc, (cfg) -> {
                cfg.expectedAddedHeader("x-preflight", "preflight-passing");
            });
        });

        SidecarInvocationTestCase src = dsl.build();
        src.dump();
        verifyPreProcessorCase(src);
    }
}
