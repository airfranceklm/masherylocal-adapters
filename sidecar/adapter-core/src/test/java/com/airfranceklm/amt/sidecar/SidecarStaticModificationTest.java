package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.dsl.SidecarTestDSL;
import com.airfranceklm.amt.sidecar.model.json.JsonRequestModificationCommand;
import com.airfranceklm.amt.sidecar.model.json.JsonRequestRoutingChangeBean;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.*;

public class SidecarStaticModificationTest extends SidecarMockSupport {
    static SidecarTestDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = SidecarTestDSL.make();
        dsl.identifyEndpoint();

        dsl.expr((tc) -> {
            buildEndpoint(tc, (cfg) -> {
                cfg.endpointURI("https://apiunittest.airfranceklm.com/localOverrideEndpoint")
                .originURI("https://backend.klm.com/path/to/op");
            });

            buildClientRequest(tc, (cfg) -> cfg.resource("/clOp"));

            buildAPIOriginRequestModification(tc, (cfg) -> cfg.expectSetUri("http://localhost:4001/respond/clOp"));
        });
    }

    @Test
    public void testStaticRouting() throws IOException {
        PreProcessEvent ppe = createPreProcessMock(dsl.build());
        replayAll();

        JsonSidecarPreProcessorOutput output = new JsonSidecarPreProcessorOutput();
        output.setModify(new JsonRequestModificationCommand());

        final JsonRequestRoutingChangeBean changeRoute = new JsonRequestRoutingChangeBean();
        changeRoute.setProtocol("http");
        changeRoute.setHost("localhost");
        changeRoute.setPort(4001);
        changeRoute.setFileBase("/respond");

        output.getModify().setChangeRoute(changeRoute);

        SidecarProcessor p = new SidecarProcessor();
        SidecarProcessor.SidecarResponseDecision retVal = p.applySidecarOutput(ppe, output);
        assertTrue(retVal.continuesWithChangeRoute());

        verifyAll();

    }
}
