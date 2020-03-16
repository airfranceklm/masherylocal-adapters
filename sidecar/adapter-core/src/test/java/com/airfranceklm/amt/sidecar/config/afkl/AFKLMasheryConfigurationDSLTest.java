package com.airfranceklm.amt.sidecar.config.afkl;

import com.airfranceklm.amt.sidecar.SidecarMockSupport;
import com.airfranceklm.amt.sidecar.SidecarInvocationTestCase;
import com.airfranceklm.amt.sidecar.dsl.SidecarTestDSL;
import com.airfranceklm.amt.sidecar.elements.DataElementFilterIntent;
import com.airfranceklm.amt.sidecar.elements.StringFilterAlgorithms;
import com.airfranceklm.amt.sidecar.model.*;
import com.airfranceklm.amt.testsupport.DSL;
import com.airfranceklm.amtml.payload.PayloadOperations;
import com.mashery.http.io.ContentSource;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.model.core.Endpoint;
import com.mashery.trafficmanager.model.core.Processor;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.airfranceklm.amt.sidecar.model.MaxPayloadSizeExcessAction.BlockSidecarCall;
import static com.airfranceklm.amt.sidecar.model.SidecarSynchronicity.RequestResponse;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;

public class AFKLMasheryConfigurationDSLTest extends SidecarMockSupport {

    @Test
    public void testConfigurationAssignsServiceEndpointIds() {
        DSL<SidecarInvocationTestCase> dsl = SidecarTestDSL.make().identifyEndpoint();

        SidecarInvocationTestCase src = dsl.build();

        PreProcessEvent pre = createPreProcessMock(src);
        PostProcessEvent post = createPostProcessMock(src);
        replayAll();

        AFKLPreProcessor reader = new AFKLPreProcessor();
        PreProcessorSidecarConfiguration preCfg = reader.read(pre);

        assertEquals("aServiceId", preCfg.getServiceId());
        assertEquals("anEndpointId", preCfg.getEndpointId());

        AFKLPostProcessor postReader = new AFKLPostProcessor();
        PostProcessorSidecarConfiguration postCfg = postReader.read(post);
        assertEquals("aServiceId", postCfg.getServiceId());
        assertEquals("anEndpointId", postCfg.getEndpointId());
    }

    @Test
    public void testConfigurationOfNull() {
        PreProcessorSidecarConfiguration cfg = preProcessorConfigFrom(null);

        assertEquals(SidecarSynchronicity.Event, cfg.getSynchronicity());
        assertNotNull(cfg.getMaximumRequestPayloadSize());
        assertEquals(cfg.getMaximumRequestPayloadSize().getLimit(), 51200);
        assertEquals(BlockSidecarCall, cfg.getMaximumRequestPayloadSize().getAction());

        AFKLPostProcessor postReader = new AFKLPostProcessor();
        PostProcessorSidecarConfiguration postCfg = postReader.read((Map<String,String>)null);

        assertEquals(SidecarSynchronicity.Event, postCfg.getSynchronicity());
        final MaxPayloadSizeSetting postProcReqSize = postCfg.getMaximumRequestPayloadSize();
        final MaxPayloadSizeSetting postProcRespSize = postCfg.getMaximumResponsePayloadSize();

        assertNotNull(postProcReqSize);
        assertNotNull(postProcRespSize);

        assertEquals(postProcReqSize.getLimit(), 51200);
        assertEquals(BlockSidecarCall, postProcReqSize.getAction());

        assertEquals(postProcRespSize.getLimit(), 51200);
        assertEquals(BlockSidecarCall, postProcRespSize.getAction());

        AFKLPreflight preflightReader = new AFKLPreflight();
        PreFlightSidecarConfiguration pfCfg = preflightReader.read((Map<String,String>)null);

        assertEquals(RequestResponse, pfCfg.getSynchronicity());
        assertNotNull(pfCfg.getMaximumRequestPayloadSize());
        assertEquals(pfCfg.getMaximumRequestPayloadSize().getLimit(), 51200);
        assertEquals(BlockSidecarCall, pfCfg.getMaximumRequestPayloadSize().getAction());

    }

    @Test
    public void testMaxPayloadSizeSetting() {
        Map<String,String> m = new HashMap<>();
        m.put("max-request-size", "5b,client-error");

        PreProcessorSidecarConfiguration preCfg = preProcessorConfigFrom(m);
        assertNull(preCfg.getElements());
        assertNotNull(preCfg.getMaximumRequestPayloadSize());
        assertEquals(5, preCfg.getMaximumRequestPayloadSize().getLimit());
        assertEquals(BlockSidecarCall, preCfg.getMaximumRequestPayloadSize().getAction());
    }

    @Test
    public void testElementSelection() {
        Map<String,String> m = new HashMap<>();
        m.put("synchronicity", "request-response");
        m.put("elements", "requestPayload,+requestHeaders,messageId,packageKey");
        m.put("max-request-size", "5b,blocking");

        PreProcessorSidecarConfiguration preCfg = preProcessorConfigFrom(m);
        assertNotNull(preCfg.getElements());
        assertEquals(4, preCfg.getElements().size());
    }

    @Test
    public void testReadingFilters() {
        Map<String,String> m = new HashMap<>();
        m.put("elements", "-headers");
        m.put("when-packageKey", "abc,def;ghi|jkl");

        PreProcessorSidecarConfiguration preCfg = preProcessorConfigFrom(m);
        assertNotNull(preCfg.getElements());
        assertEquals(2, preCfg.getElements().size());
        assertTrue(preCfg.demandsElement("packageKey"));

        ElementDemand d = preCfg.allocOrGetElementDemand("packageKey", null);
        assertEquals(1, d.getFilters().size());

        ElementFilterDemand efg = d.getFilters().get(0);
        assertEquals(StringFilterAlgorithms.DslExpression, efg.getAlgorithm());
        assertEquals("abc,def;ghi|jkl", efg.getExpression());
        assertNull(efg.getLabel());
        assertEquals(DataElementFilterIntent.MatchScopes, efg.getIntent());
    }


    @Test
    public void testElementExpansionWithSingleElement() {
        assertNeedsExpansion("tokenScope", "tokenScope");
    }

    @Test
    public void testElementExpansionFromList() {
        assertNeedsExpansion("remoteAddress,verb;grantType|notCreatedYet", "remoteAddress");
        assertNeedsExpansion("remoteAddress,verb;grantType|notCreatedYet", "verb");
        assertNeedsExpansion("remoteAddress,verb;grantType|notCreatedYet", "grantType");
        assertNeedsExpansion("remoteAddress,verb;grantType|notCreatedYet", "notCreatedYet");
    }


    private void assertNeedsExpansion(String token, String expToken) {
        Map<String, String> map = new HashMap<>();
        map.put("elements", token);

        assertNeedsExpansionAtOpsPoints(map, expToken);

        map.clear();
        map.put("preflight-elements", token);

        AFKLPreflight pfReader = new AFKLPreflight();
        PreFlightSidecarConfiguration cfCfg = pfReader.read(map);

        assertTrue(cfCfg.demandsElement(expToken));
    }


    private void assertNeedsExpansionAtOpsPoints(Map<String, String> cfg, String elementName) {
        PreProcessorSidecarConfiguration preConf = preProcessorConfigFrom(cfg);

        assertTrue(preConf.demandsElement(elementName));

        AFKLPostProcessor postReader = new AFKLPostProcessor();
        PostProcessorSidecarConfiguration postConf = postReader.read(cfg);

        assertTrue(postConf.demandsElement(elementName));
    }

    private PreProcessorSidecarConfiguration preProcessorConfigFrom(Map<String, String> cfg) {
        AFKLPreProcessor preReader = new AFKLPreProcessor();
        return preReader.read(cfg);
    }

    private PostProcessorSidecarConfiguration postProcessorConfigFrom(Map<String, String> cfg) {
        AFKLPostProcessor preReader = new AFKLPostProcessor();
        return preReader.read(cfg);
    }

    private PreFlightSidecarConfiguration preflightConfigFrom(Map<String, String> cfg) {
        AFKLPreflight preReader = new AFKLPreflight();
        return preReader.read(cfg);
    }


    @Test
    public void testConfiguringSynchronicity() {
        Map<String, String> map = new HashMap<>();

        assertSynchronicity(map, SidecarSynchronicity.Event);

        map.put("synchronicity", "request-response");
        assertSynchronicity(map, SidecarSynchronicity.RequestResponse);

        map.put("synchronicity", "non-blocking");
        assertSynchronicity(map, SidecarSynchronicity.NonBlockingEvent);

        map.put("synchronicity", "non-existing");
        assertHasErrors(map);
    }


    private void assertSynchronicity(Map<String, String> map, SidecarSynchronicity expSync) {
        PreProcessorSidecarConfiguration preCfg = preProcessorConfigFrom(map);
        assertEquals(preCfg.getSynchronicity(), expSync);

        PostProcessorSidecarConfiguration postCfg = postProcessorConfigFrom(map);
        assertEquals(postCfg.getSynchronicity(), expSync);
    }

    private void assertHasErrors(Map<String, String> map) {
        PreProcessorSidecarConfiguration preCfg = preProcessorConfigFrom(map);
        assertTrue(preCfg.hasErrors());

        PostProcessorSidecarConfiguration postCfg = postProcessorConfigFrom(map);
        assertTrue(postCfg.hasErrors());
    }


    @Test
    public void testConfigurationTypeConversion() {

        Map<String,String> m = new HashMap<>();
        m.put("param-string", "string");
        m.put("preflight-param-pfstring", "string");

        m.put("param-true", "true");
        m.put("preflight-param-pftrue", "true");

        m.put("param-false", "false");
        m.put("preflight-param-pffalse", "false");

        m.put("param-num", "200");
        m.put("preflight-param-pfnum", "200");

        m.put("param-double", "200.43");
        m.put("preflight-param-pfdouble", "200.43");

        m.put("param-null", "null");
        m.put("preflight-param-pfnull", "null");

        InlineAsserter<SidecarConfiguration> asserter = (cfg) -> {
            assertEquals("string", cfg.getSidecarParameter("string"));
            assertEquals(true, cfg.getSidecarParameter("true"));
            assertEquals(false, cfg.getSidecarParameter("false"));
            assertEquals(new Integer(200), new Integer(cfg.<Integer>getSidecarParameter("num")));
            assertEquals(new Double(200.43), new Double(cfg.<Double>getSidecarParameter("double")));

            assertTrue(cfg.getSidecarParams().containsKey("null"));
            assertNull(cfg.getSidecarParameter("null"));
        };

        InlineAsserter<PreFlightSidecarConfiguration> pfAsserter = (cfg) -> {
            assertTrue(cfg.preflightDemanded());

            assertEquals("string", cfg.getSidecarParameter("pfstring"));
            assertEquals(true, cfg.getSidecarParameter("pftrue"));
            assertEquals(false, cfg.getSidecarParameter("pffalse"));
            assertEquals(new Integer(200), new Integer(cfg.<Integer>getSidecarParameter("pfnum")));
            assertEquals(new Double(200.43), new Double(cfg.<Double>getSidecarParameter("pfdouble")));
            assertNull(cfg.getSidecarParameter("pfnull"));
        };

        asserter.doAssert(preProcessorConfigFrom(m));
        asserter.doAssert(postProcessorConfigFrom(m));
        pfAsserter.doAssert(preflightConfigFrom(m));
    }

    @FunctionalInterface
    private interface InlineAsserter<T extends SidecarConfiguration> {
        void doAssert(T config);
    }

    /**
     * Checks that {@link PayloadOperations#getContentOf(ContentSource, Charset)} is reading the data correctly.
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

        String contentOf = PayloadOperations.getContentOf(cs, StandardCharsets.UTF_8);
        assertEquals(value, contentOf);
    }

    // ----------------------------
    // Mocks

    private PreProcessEvent mockPreProcessWith(Map<String,String> cfg) {
        PreProcessEvent event = createMock(PreProcessEvent.class);

        Endpoint endp = createMock(Endpoint.class);
        Processor proc = createMock(Processor.class);
        expect(proc.getPreProcessorParameters()).andReturn(cfg).anyTimes();
        expect(endp.getProcessor()).andReturn(proc).anyTimes();
        expect(event.getEndpoint()).andReturn(endp).anyTimes();

        return event;
    }

    private PostProcessEvent mockPostProcessWith(Map<String,String> cfg) {
        PostProcessEvent event = createMock(PostProcessEvent.class);

        Endpoint endp = createMock(Endpoint.class);
        Processor proc = createMock(Processor.class);
        expect(proc.getPreProcessorParameters()).andReturn(cfg).anyTimes();
        expect(endp.getProcessor()).andReturn(proc).anyTimes();
        expect(event.getEndpoint()).andReturn(endp).anyTimes();

        return event;
    }

}
