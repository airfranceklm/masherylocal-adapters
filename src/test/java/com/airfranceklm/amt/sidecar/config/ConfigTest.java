package com.airfranceklm.amt.sidecar.config;

import com.airfranceklm.amt.sidecar.AFKLMSidecarProcessor;
import com.mashery.http.io.ContentSource;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ConfigTest {

    @Test
    public void testExpandTokenScope() {
        assertNeedsExpansion("tokenScope", InputScopeExpansion.TokenScope);
    }

    @Test
    public void testExpandRemoteAddress() {
        assertNeedsExpansion("remoteAddress", InputScopeExpansion.RemoteAddress);
    }

    @Test
    public void testExpandHttpVerb() {
        assertNeedsExpansion("verb", InputScopeExpansion.RequestVerb);
    }

    @Test
    public void testExpandTokenGrantType() {
        assertNeedsExpansion("grantType", InputScopeExpansion.GrantType);
    }

    @Test
    public void testExpandOperation() {
        Map<String,String> map = new HashMap<>();
        map.put("expand-input", "operation");
        assertNeedsExpansionAtOpsPoints(map, InputScopeExpansion.Operation);

        map = new HashMap<>();
        map.put("expand-preflight", "operation");

        SidecarConfiguration preflightCfg = new MasheryConfigSidecarConfigurationBuilder().getSidecarConfiguration(SidecarInputPoint.Preflight, map);
        assertFalse(preflightCfg.needsPreflightExpansionOf(InputScopeExpansion.Operation));
        assertTrue(preflightCfg.hasErrors());
    }

    private void assertNeedsExpansion(String token, InputScopeExpansion req) {
        MasheryConfigSidecarConfigurationBuilder builder = new MasheryConfigSidecarConfigurationBuilder();

        Map<String,String> map = new HashMap<>();
        map.put("expand-input", token);

        assertNeedsExpansionAtOpsPoints(map, req);

        map.clear();
        map.put("expand-preflight", token);
        SidecarConfiguration cfg = builder.getSidecarConfiguration(SidecarInputPoint.Preflight, map);
        assertTrue(cfg.needsPreflightExpansionOf(req));
    }

    private void assertNeedsExpansionAtOpsPoints(Map<String,String> cfg, InputScopeExpansion req) {
        MasheryConfigSidecarConfigurationBuilder builder = new MasheryConfigSidecarConfigurationBuilder();

        for (SidecarInputPoint sip: SidecarInputPoint.getOperationalPoints()) {
            SidecarConfiguration sCfg = builder.getSidecarConfiguration(sip, cfg);
            assertTrue(sCfg.needsExpansionOf(req));
        }
    }

    @Test public void testConfigureMaxSize() {
        MasheryConfigSidecarConfigurationBuilder builder = new MasheryConfigSidecarConfigurationBuilder();

        Map<String,String> map = new HashMap<>();
        map.put("expand-input", "RequestPayload");
        SidecarConfiguration cfg = builder.getSidecarConfiguration(SidecarInputPoint.PreProcessor, map);
        assertEquals(cfg.getMaxSize().getMaxSize(), 51200);
        assertEquals(MaxSizeComplianceRequirement.Blocking, cfg.getMaxSize().getCompliance());


//        map.put("expand-input", "RequestPayload");

    }

    @Test
    public void testConfiguringSynchronicity() {
        MasheryConfigSidecarConfigurationBuilder builder = new MasheryConfigSidecarConfigurationBuilder();

        Map<String,String> map = new HashMap<>();

        assertSynchronicity(builder, map, SidecarSynchronicity.Event);

        map.put("synchronicity", "request-response");
        assertSynchronicity(builder, map, SidecarSynchronicity.RequestResponse);

        map.put("synchronicity", "non-blocking");
        assertSynchronicity(builder, map, SidecarSynchronicity.NonBlockingEvent);

        map.put("synchronicity", "non-existing");
        SidecarConfiguration cfg = builder.getSidecarConfiguration(SidecarInputPoint.PreProcessor, map);
        assertTrue(cfg.hasErrors());
    }

    protected void assertSynchronicity(MasheryConfigSidecarConfigurationBuilder builder, Map<String, String> map, SidecarSynchronicity expSync) {
        SidecarConfiguration cfg = builder.getSidecarConfiguration(SidecarInputPoint.PreProcessor, map);
        assertEquals(cfg.getSynchronicity(), expSync);

        cfg = builder.getSidecarConfiguration(SidecarInputPoint.PostProcessor, map);
        assertEquals(cfg.getSynchronicity(), expSync);
    }

    @Test
    public void testConfigurationTypeConversoin() {


        Map<String,String> m = new HashMap<>();
        m.put("sidecar-param-string", "string");
        m.put("sidecar-param-true", "true");
        m.put("sidecar-param-false", "false");
        m.put("sidecar-param-num", "200");
        m.put("sidecar-param-double", "200.43");
        m.put("sidecar-param-null", "null");

        SidecarConfiguration cfg = new MasheryConfigSidecarConfigurationBuilder().getSidecarConfiguration(SidecarInputPoint.PreProcessor, m);

        assertEquals("string", cfg.getSidecarParameter("string"));
        assertEquals(true, cfg.getSidecarParameter("true"));
        assertEquals(false, cfg.getSidecarParameter("false"));
        assertEquals(new Integer(200), new Integer(cfg.<Integer>getSidecarParameter("num")));
        assertEquals(new Double(200.43), new Double(cfg.<Double>getSidecarParameter("double")));
        assertNull(cfg.getSidecarParameter("null"));
    }

    /**
     * Checks that {@link AFKLMSidecarProcessor#getContentOf(ContentSource)} is reading the data correctly.
     */
    @Test
    public void testContentSourceExtraction() throws IOException {
        String value = "ACBDEFGH";

        ContentSource cs = new ContentSource() {
            @Override
            public long getContentLength() {
                return value.length();
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public boolean isRepeatable() {
                return true;
            }
        };

        String contentOf = AFKLMSidecarProcessor.getContentOf(cs);
        assertEquals(value, contentOf);
    }

}
