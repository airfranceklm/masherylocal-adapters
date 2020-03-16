package com.airfranceklm.amt.testsupport;

import org.junit.Test;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.*;
import static org.junit.Assert.*;

public class MasheryProcessorTestCaseAccessorTest {

    @Test
    public void testLocatePackageKey() {
        MasheryProcessorTestCase tc = new MasheryProcessorTestCase();
        assertNull(locatePackageKey(tc));

        assertNotNull(allocOrGetAPIClientRequest(tc));
        assertNull(locatePackageKey(tc));

        final MasheryPackageKeyModel pk = allocOrGetPackageKey(tc);
        assertNotNull(pk);
        assertSame(pk, locatePackageKey(tc));
    }

    @Test
    public void testLocateApplicationModel() {
        MasheryProcessorTestCase tc = new MasheryProcessorTestCase();
        assertNull(locateApplicationModel(tc));

        assertNotNull(allocOrGetAPIClientRequest(tc));
        assertNull(locateApplicationModel(tc));

        final MasheryApplicationModel pk = allocOrGetApplication(tc);
        assertNotNull(pk);
        assertSame(pk, locateApplicationModel(tc));
    }

    @Test
    public void testLocateAuthorizationContext() {
        MasheryProcessorTestCase tc = new MasheryProcessorTestCase();
        assertNull(locateAuthorizationContext(tc));

        assertNotNull(allocOrGetAPIClientRequest(tc));
        assertNull(locateAuthorizationContext(tc));

        final MasheryAuthorizationContextModel pk = allocOrGetAuthorizationContext(tc);
        assertNotNull(pk);
        assertSame(pk, locateAuthorizationContext(tc));
    }

    @Test
    public void testLocateAPIOriginRequestModification() {
        MasheryProcessorTestCase tc = new MasheryProcessorTestCase();
        assertNull(locateAPIOriginRequestModification(tc));

        assertNotNull(allocOrGetApiOriginInteraction(tc));
        assertNull(locateAPIOriginRequestModification(tc));

        final APIOriginRequestModificationModel pk = allocOrGetAPIOriginRequestModification(tc);
        assertNotNull(pk);
        assertSame(pk, locateAPIOriginRequestModification(tc));
    }

    @Test
    public void testLocateAPIOriginResponse() {
        MasheryProcessorTestCase tc = new MasheryProcessorTestCase();
        assertNull(locateAPIOriginResponse(tc));

        assertNotNull(allocOrGetApiOriginInteraction(tc));
        assertNull(locateAPIOriginResponse(tc));

        final APIOriginResponseModel pk = allocOrGetAPIOriginResponse(tc);
        assertNotNull(pk);
        assertSame(pk, locateAPIOriginResponse(tc));
    }

    @Test
    public void testSimpleAllocs() {
        MasheryProcessorTestCase tc = new MasheryProcessorTestCase();
        assertNotNull(allocOrGetMasheryDebugContextInteraction(tc));
        assertNotNull(allocOrGetCacheInteraction(tc));
        assertNotNull(allocOrGetApiClientResponse(tc));
    }

    // ---------------------------------------------
    // DSL-stype methods tests
    // Quick checks that the objects are created in the expected places.
    // Extensive checks on individual methods is not done, since this is a standard Lombok functionality
    // and doesn't need to be tested specifically.

    @Test
    public void testBuildEndpoint() {
        MasheryProcessorTestCase tc = new MasheryProcessorTestCase();
        buildEndpoint(tc, (cfg) -> cfg.serviceId("pass"));
        assertNotNull(tc.getEndpoint());
        assertEquals("pass", tc.getEndpoint().getServiceId());
    }

    @Test
    public void testBuildClientRequest() {
        MasheryProcessorTestCase tc = new MasheryProcessorTestCase();
        buildClientRequest(tc, (cfg) -> cfg.httpVerb("UNITTEST"));

        assertNotNull(tc.getClientRequest());
        assertEquals("UNITTEST", tc.getClientRequest().getHttpVerb());
    }

    @Test
    public void testBuildAuthorizationContext() {
        MasheryProcessorTestCase tc = new MasheryProcessorTestCase();
        buildAuthorizationContext(tc, (cfg) -> cfg.scope("abc"));

        assertNotNull(tc.getClientRequest());
        assertNotNull(tc.getClientRequest().getAuthorizationContext());
        assertEquals("abc", tc.getClientRequest().getAuthorizationContext().getScope());
    }

    @Test
    public void testBuildApplication() {
        MasheryProcessorTestCase tc = new MasheryProcessorTestCase();
        buildApplication(tc, (cfg) -> cfg.name("abc"));

        assertNotNull(tc.getClientRequest());
        assertNotNull(tc.getClientRequest().getApplication());
        assertEquals("abc", tc.getClientRequest().getApplication().getName());
    }

    @Test
    public void testBuildPackageKey() {
        MasheryProcessorTestCase tc = new MasheryProcessorTestCase();
        buildPackageKey(tc, (cfg) -> cfg.packageKey("abc"));

        assertNotNull(tc.getClientRequest());
        assertNotNull(tc.getClientRequest().getApplication());
        assertNotNull(tc.getClientRequest().getApplication().getPackageKeyModel());
        assertEquals("abc", tc.getClientRequest().getApplication().getPackageKeyModel().getPackageKey());
    }

    @Test
    public void testBuildAPIOriginInteractions() {
        MasheryProcessorTestCase tc = new MasheryProcessorTestCase();
        buildAPIOriginInteraction(tc, (cfg) -> cfg.responseException("abc"));

        assertNotNull(tc.getOriginInteraction());
        assertEquals("abc", tc.getOriginInteraction().getResponseException());
    }

    @Test
    public void testBuildAPIOriginRequestModification() {
        MasheryProcessorTestCase tc = new MasheryProcessorTestCase();
        buildAPIOriginRequestModification(tc, (cfg) -> cfg.expectedDroppedHeader("abc"));

        assertNotNull(tc.getOriginInteraction());
        assertNotNull(tc.getOriginInteraction().getRequestModification());
        assertEquals("abc", tc.getOriginInteraction().getRequestModification().getExpectedDroppedHeaders().get(0));
    }

    @Test
    public void testBuildAPIOriginRequestResponse() {
        MasheryProcessorTestCase tc = new MasheryProcessorTestCase();
        buildAPIOriginResponse(tc, (cfg) -> cfg.header("abc", "def"));

        assertNotNull(tc.getOriginInteraction());
        assertNotNull(tc.getOriginInteraction().getResponse());
        assertEquals("def", tc.getOriginInteraction().getResponse().getHeaders().get("abc"));
    }

    @Test
    public void testBuildMasheryResponse() {
        MasheryProcessorTestCase tc = new MasheryProcessorTestCase();
        buildMasheryResponse(tc, (cfg) -> cfg.header("abc", "def"));

        assertNotNull(tc.getMasheryResponse());
        assertEquals("def", tc.getMasheryResponse().getHeaders().get("abc"));
    }

    @Test
    public void testBuildDebugContext() {
        MasheryProcessorTestCase tc = new MasheryProcessorTestCase();
        buildDebugContext(tc, (cfg) -> cfg.expectRemove("a"));

        assertNotNull(tc.getDebugContextInteraction());
        assertTrue(tc.getDebugContextInteraction().getExpectRemoves().contains("a"));
    }

    @Test
    public void testBuildCache() {
        MasheryProcessorTestCase tc = new MasheryProcessorTestCase();
        buildCache(tc, (cfg) -> cfg.expectedGet("a"));

        assertNotNull(tc.getCacheInteraction());
        assertTrue(tc.getCacheInteraction().getExpectedGets().contains("a"));
    }
}
