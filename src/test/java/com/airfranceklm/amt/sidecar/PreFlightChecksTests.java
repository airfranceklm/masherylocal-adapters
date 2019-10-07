package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.config.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.config.SidecarSynchronicity;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests of the pre-flight processing checks.
 */
public class PreFlightChecksTests extends AFKLMSidecarMockSupport {

    private static SidecarTestDSL baseDSL;

    @BeforeClass
    public static void createDSL() {
        baseDSL = new SidecarTestDSL();
        baseDSL.configureEndpointData((endp) -> {
            endp.identifyAs("fServiceId", "fEndpointId", "ut-endpoint")
                    .enpointUri("https://api-unitttest.airfranceklm.com/travel/");

            endp.preProcessor()
                    .param("synchronicity", "event")
                    .param("include-request-headers", "a|x-preflight")
                    .param("preflight-enabled", "true");
        });

        baseDSL.configureAPIClientRequest((cl) -> cl.from("1.2.3.4")
                .withVerb("GET")
                .withUri("https://api-unitttest.airfranceklm.com/travel/unitTest?myQuery=123")
                .withHeader("a", "B")
                .withoutPayload());

        baseDSL.configurePackageKey((cl) -> {
            cl.key("pk12345");
            cl.application("Boo")
                    .withEAV("a", "B");
        });

        baseDSL.configureAPIOriginRequest((cl) -> {
            cl.withOriginalURI("https://internal-hosting.host.klm/pathto/provivider/unitTest?myQuery=123");
        });

        baseDSL.configurePreflightInput((cl) -> {
            cl.withUnitTestMessageId()
                    .synchronicity(SidecarSynchronicity.RequestResponse)
                    .point(SidecarInputPoint.Preflight)
                    .serviceId("fServiceId")
                    .endpointId("fEndpointId")
                    .packageKey("pk12345");
        });

    }

    public PreFlightChecksTests() {
        super();
    }

    @Test
    public void testPreflightCanCancelProcessing() {
        SidecarTestDSL dsl = baseDSL.duplicate();
        dsl.configurePreflightOutput(cl -> {
            cl.withCode(403);
        });
        dsl.expectCurtailingWith(403, AFKLMSidecarProcessor.MSG_SERVICE_CANNOT_BE_PROVIDED);
        // Is this the right message?

        SidecarRequestCase src = dsl.build();
        verifyPreProcessorRequestCase(src);
    }

    @Test
    public void testPreflightChangesRouting() {
        SidecarTestDSL dsl = baseDSL.duplicate();
        dsl.configurePreflightOutput(cl -> {
            cl.changeRouting()
                    .toHost("internal-hosting-updated.host.klm");
        });

        dsl.configureAPIOriginRequest((cl) -> {
            cl.expectSetURI("https://internal-hosting-updated.host.klm/pathto/provivider/unitTest?myQuery=123");
        });

        dsl.configureSidecarInput(cl -> {
            cl.withUnitTestMessageId()
                    .synchronicity(SidecarSynchronicity.Event)
                    .point(SidecarInputPoint.PreProcessor)
                    .serviceId("fServiceId")
                    .endpointId("fEndpointId")
                    .packageKey("pk12345");
            cl.request()
                    .withHeader("a", "B");
        });

        SidecarRequestCase src = dsl.build();
        verifyPreProcessorRequestCase(src);
    }

    @Test
    public void testPreflightAddsHeaders() {
        SidecarTestDSL dsl = baseDSL.duplicate();
        dsl.configurePreflightOutput(cl -> cl.addHeader("x-preflight", "preflight-passing"));

        dsl.configureAPIOriginRequest((c) -> c.expectAddHeader("x-preflight", "preflight-passing"));

        dsl.configureSidecarInput((c) -> {
            c.withUnitTestMessageId()
                    .synchronicity(SidecarSynchronicity.Event)
                    .point(SidecarInputPoint.PreProcessor)
                    .serviceId("fServiceId")
                    .endpointId("fEndpointId")
                    .packageKey("pk12345");

            c.request()
                    .withHeader("a", "B")
                    .withHeader("x-preflight", "preflight-passing");
        });

        SidecarRequestCase src = dsl.build();
        verifyPreProcessorRequestCase(src);
    }
}
