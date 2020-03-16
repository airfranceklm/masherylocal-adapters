package com.airfranceklm.amt.sidecar;
/*
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.config.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.config.SidecarSynchronicity;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.stack.AFKLMSidecarStack;
import com.airfranceklm.amt.sidecar.stack.HTTPSidecarStack;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

@Ignore
 */
public class HTTPStackIntegrationTest {
/*
    @Test
    public void testHttpStackIntegration() throws IOException {
        HTTPSidecarStack httpStack = new HTTPSidecarStack();
        SidecarConfiguration cfg = new SidecarConfiguration(SidecarInputPoint.PreProcessor);
        cfg.addStackParameter("uri", "http://localhost:4001/");
        cfg.addStackParameter("compression", "false");

        AFKLMSidecarStack.AFKLMSidecarStackConfiguration sCfg = httpStack.configureFrom(cfg);

        SidecarInput input = new SidecarInput();
        input.setPoint(SidecarInputPoint.PreProcessor);
        input.setSynchronicity(SidecarSynchronicity.RequestResponse);
        input.setPackageKey("packageKey когда лето заканчивается");
        input.setServiceId("serviceId");
        input.setEndpointId("endpointId");
        input.addParam("foo", "bar");

        input.getOrCreateRequest().addHeader("x-a", "header-value");

        httpStack.invoke(sCfg, input);
    }

    @Test
    public void testHttpStackIntegrationWithCompression() throws IOException {
        HTTPSidecarStack httpStack = new HTTPSidecarStack();
        SidecarConfiguration cfg = new SidecarConfiguration(SidecarInputPoint.PreProcessor);
        cfg.addStackParameter("uri", "http://localhost:4001/");


        AFKLMSidecarStack.AFKLMSidecarStackConfiguration sCfg = httpStack.configureFrom(cfg);

        SidecarInput input = new SidecarInput();
        input.setPoint(SidecarInputPoint.PreProcessor);
        input.setSynchronicity(SidecarSynchronicity.RequestResponse);
        input.setPackageKey("packageKey");
        input.setServiceId("serviceId");
        input.setEndpointId("endpointId");
        input.addParam("foo", "bar");
        input.addApplicationEAV("PublicKey", "keyValue");

        input.getOrCreateRequest().addHeader("x-a", "header-value");

        httpStack.invoke(sCfg, input);
    }

 */
}
