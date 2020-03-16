package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.identity.PartyKeyDescriptor;
import org.easymock.EasyMockSupport;
import org.junit.Test;
import software.amazon.awssdk.regions.Region;

import java.util.HashMap;
import java.util.Map;

import static com.airfranceklm.amt.sidecar.stack.AWSLambdaConfiguration.*;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.*;

public class AWSLambdaConfigurationTest extends EasyMockSupport {

    @Test
    public void createNullConfiguration() {
        AWSLambdaConfiguration cfg = new AWSLambdaConfiguration(null, 1000L, null);
        assertNull(cfg.getFunctionARN());
        assertNull(cfg.getAwsRegion());
        assertNull(cfg.getAwsClusterIdentity());
        assertNull(cfg.getAwsKey());
        assertNull(cfg.getAwsSecret());
        assertNull(cfg.getAssumeAwsRole());
        assertNull(cfg.getAssumeAwsRoleExternalId());
        assertNull(cfg.getProxyHost());
        assertEquals(-1, cfg.getProxyPort());
    }

    @Test
    public void testInheritingDefaults() {

        PartyKeyDescriptor pkd = new PartyKeyDescriptor();

        AWSLambdaConfiguration defs = AWSLambdaConfiguration.builder()
                .functionARN("f")
                .awsRegion(Region.EU_WEST_1)
                .awsKey("k")
                .awsSecret("s")
                .assumeAwsRole("r")
                .assumeAwsRoleExternalId("extId")
                .awsClusterIdentity(pkd)
                .proxyHost("h")
                .proxyPort(8080)
                .supportProxyHttp(true)
                .supportProxyHttps(true)
                .build();

        AWSLambdaConfiguration cfg = new AWSLambdaConfiguration(null, 3500L, defs);

        assertEquals("f", cfg.getFunctionARN());
        assertEquals(Region.EU_WEST_1, cfg.getAwsRegion());
        assertEquals("k", cfg.getAwsKey());
        assertEquals("s", cfg.getAwsSecret());
        assertEquals("r", cfg.getAssumeAwsRole());
        assertEquals("extId", cfg.getAssumeAwsRoleExternalId());
        assertSame(pkd, cfg.getAwsClusterIdentity());

        assertEquals("h", cfg.getProxyHost());
        assertEquals(8080, cfg.getProxyPort());
        assertTrue(cfg.isSupportProxyHttp());
        assertTrue(cfg.isSupportProxyHttps());
    }

    @Test
    public void testConfigurationValidity() {
        AWSLambdaConfiguration cfg = new AWSLambdaConfiguration();
        assertFalse(cfg.isValid());
        assertFalse(cfg.specifiesAwsIdentity());
        assertFalse(cfg.assumesRole());

        cfg.setFunctionARN("F");
        assertFalse(cfg.isValid());
        assertFalse(cfg.specifiesAwsIdentity());
        assertFalse(cfg.assumesRole());

        cfg.setAwsKey("k");
        assertFalse(cfg.isValid());
        assertFalse(cfg.specifiesAwsIdentity());
        assertFalse(cfg.assumesRole());

        cfg.setAwsSecret("s");
        assertTrue(cfg.specifiesAwsIdentity());
        assertFalse(cfg.assumesRole());
        assertTrue(cfg.isValid());

        cfg.setAwsKey(null);
        assertFalse(cfg.specifiesAwsIdentity());
        assertFalse(cfg.assumesRole());
        assertFalse(cfg.isValid());

        cfg.setAssumeAwsRole("r");
        assertTrue(cfg.isValid());
        assertTrue(cfg.assumesRole());
        assertFalse(cfg.specifiesAwsIdentity());

        cfg.setAwsKey("k");
        assertFalse(cfg.isValid());

        cfg.setAwsSecret(null);
        assertFalse(cfg.specifiesAwsIdentity());
        assertTrue(cfg.assumesRole());
        assertTrue(cfg.isValid());
    }

    @Test
    public void testConfigurationParsing() {
        Map<String, String> cfgMap = new HashMap<>();

        cfgMap.put(CFG_FUNCTION_ARN, "F");
        cfgMap.put(CFG_AWS_KEY, "k");
        cfgMap.put(CFG_AWS_SECRET, "s");
        cfgMap.put(CFG_AWS_REGION, "eu-west-1");
        cfgMap.put(CFG_ASSUME_ROLE, "r");
        cfgMap.put(CFG_PROXY_HOST, "h");
        cfgMap.put(CFG_PROXY_PORT, "8080");
        cfgMap.put(CFG_HTTP_PROTO, "true");
        cfgMap.put(CFG_HTTPS_PROTO, "true");

        AWSLambdaConfiguration cfg = new AWSLambdaConfiguration(cfgMap, 350L);
        assertEquals("F", cfg.getFunctionARN());
        assertEquals("k", cfg.getAwsKey());
        assertEquals("s", cfg.getAwsSecret());
        assertEquals(Region.EU_WEST_1, cfg.getAwsRegion());
        assertEquals("r", cfg.getAssumeAwsRole());
        assertEquals("h", cfg.getProxyHost());
        assertEquals(8080, cfg.getProxyPort());
        assertTrue(cfg.isSupportProxyHttp());
        assertTrue(cfg.isSupportProxyHttps());

        assertEquals(350L, cfg.getTimeout());
    }

    @Test
    public void testParsingMalformedPort() {
        Map<String, String> cfgMap = new HashMap<>();
        cfgMap.put(CFG_PROXY_PORT, "8080d");

        AWSLambdaConfiguration cfg = partialMockBuilder(AWSLambdaConfiguration.class)
                .addMockedMethod("log")
                .createMock();
        cfg.log("Not a number for proxy port: 8080d");
        expectLastCall().once();

        replayAll();
        assertEquals(0, cfg.getProxyPort());
        cfg.init(cfgMap, 400L, null);

        assertEquals(8080, cfg.getProxyPort());
        assertTrue(cfg.definesProxyPort());
        assertFalse(cfg.requiresProxy());

        verifyAll();
    }

    @Test
    public void testEffectiveProxyPort() {
        AWSLambdaConfiguration cfg = new AWSLambdaConfiguration();
        assertEquals(-1, cfg.getProxyPort());
        assertFalse(cfg.definesProxyPort());
        assertEquals(8080, cfg.effectiveProxyPort());
    }

    @Test
    public void testRequireProxySetting() {
        AWSLambdaConfiguration cfg = new AWSLambdaConfiguration();
        assertFalse(cfg.requiresProxy());

        cfg.setProxyHost("h");
        assertTrue(cfg.requiresProxy());
    }

}
