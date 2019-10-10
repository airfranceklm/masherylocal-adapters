package com.airfranceklm.amt.sidecar.config;

import com.airfranceklm.amt.testsupport.RequestMockSupport;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;

public class MasheryConfigSidecarConfigurationBuilderTest extends RequestMockSupport {
    @Test
    public void testSimple() {
        Map<String, String> map = new HashMap<>();
        map.put("synchronicity", "request-response");
        map.put("expand-input", "remoteAddress");
        map.put("require-request-headers", "authorization");
        map.put("sidecar-param-B2C", "false");
        map.put("sidecar-param-B2E", "true");
        map.put("sidecar-param-depts", "KLC|ITDCC");

        SidecarConfiguration cfg = new MasheryConfigSidecarConfigurationBuilder().getSidecarConfiguration(SidecarInputPoint.PreProcessor, map);
        assertFalse(cfg.demandsPreflightHandling());
    }
}
