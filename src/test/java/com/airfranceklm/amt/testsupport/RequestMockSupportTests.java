package com.airfranceklm.amt.testsupport;

import com.airfranceklm.amt.sidecar.AFKLMSidecarProcessor;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.model.auth.AuthorizationType;
import com.mashery.trafficmanager.model.oauth.OAuthContext;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class RequestMockSupportTests extends RequestCaseYAMLReader<RequestCase> {

    public RequestMockSupportTests() {
        super("./validation.yaml");
    }

    @Override
    protected RequestCase createRequestCase(String name) {
        return new RequestCase(name);
    }

    /*

    @Test
    public void testTrafficManagerExpectations() {
        RequestCase rc = getRequestCase("Basic Parsing", "can parse traffic manager actions correctly");
        PreProcessEvent ppe = setupPreProcessorEvent(rc);

        assertNotNull(ppe.getCallContext());
        assertNotNull(ppe.getCallContext().getResponse());

        ppe.getCallContext().getResponse().getHTTPResponse().setStatusCode(403);
        ppe.getCallContext().getResponse().getHTTPResponse().setStatusMessage("Unit test validation");
        ppe.getCallContext().getResponse().getHTTPResponse().setBody(AFKLMSidecarProcessor.produceFromString("AAAA-FFFF"));

        ppe.getCallContext().getResponse().setComplete();
        verifyAll();
    }

    @Test
    public void testCanParseAPIClientRequest() throws IOException {
        RequestCase rc = getRequestCase("Basic Parsing", "can parse client request correctly");
        basalHTTPServerValidation(rc);
    }

    @Test
    public void testCanParseAPIClientRequestWithReference() throws IOException {
        RequestCase rc = getRequestCase("Basic Parsing", "can parse client reference to another case");
        basalHTTPServerValidation(rc);
    }

    @Test
    public void testCanParseKeyInformation() throws IOException {
        RequestCase rc = getRequestCase("Basic Parsing", "can parse key information correctly");
        assertNotNull(rc);

        PreProcessEvent ppe = setupPreProcessorEvent(rc);
        assertBasalHTTPServerValidation(ppe);
        assertBasalPackageKey(ppe);
    }

    @Test
    public void testCanParseAuthorizationContext() throws IOException {
        RequestCase rc = getRequestCase("Basic Parsing", "can parse authorization context correctly");
        assertNotNull(rc);

        PreProcessEvent ppe = setupPreProcessorEvent(rc);
        assertBasalHTTPServerValidation(ppe);
        assertBasalPackageKey(ppe);
        assertAuthorizationContext(ppe);
    }

    @Test
    public void testCanParseAPIOriginRequest() throws IOException {
        RequestCase rc = getRequestCase("Basic Parsing", "can parse request to api origin correctly");
        assertNotNull(rc);

        PreProcessEvent ppe = setupPreProcessorEvent(rc);
        assertBasalHTTPServerValidation(ppe);
        assertBasalPackageKey(ppe);
        assertAuthorizationContext(ppe);

        assertNotNull(ppe.getClientRequest());
        assertEquals("https://docker.kml/backend/url?myQuery=ffff", ppe.getClientRequest().getURI());
        assertEquals("GET", ppe.getClientRequest().getMethod());
        assertEquals("B", ppe.getClientRequest().getHeaders().get("A"));

        // Test the missing header that was removed.
        assertFalse(ppe.getClientRequest().getHeaders().contains("C"));
        assertNull(ppe.getClientRequest().getHeaders().get("C"));

        assertEquals("F", ppe.getClientRequest().getHeaders().get("E"));
        assertFalse(ppe.getClientRequest().getHeaders().contains("D"));
    }

    private void assertAuthorizationContext(PreProcessEvent ppe) {
        assertNotNull(ppe.getAuthorizationContext());
        assertEquals(AuthorizationType.OAUTH_2, ppe.getAuthorizationContext().getType());
        OAuthContext ctx = (OAuthContext)ppe.getAuthorizationContext();
        assertEquals("unit-test-token", ctx.getToken());
        assertEquals("CC", ctx.getAccessToken().getGrantType());
        assertEquals("12345", ctx.getAccessToken().getScope());
        assertEquals("456", ctx.getAccessToken().getUserToken());
    }

    //--------------------------------------------------------------------------------------------
    // Private methods.

    private void assertBasalPackageKey(PreProcessEvent ppe) {
        assertNotNull(ppe.getKey());
        assertEquals("dfgf", ppe.getKey().getExternalID());
        assertNotNull(ppe.getKey().getApplication());
        assertEquals("the-app", ppe.getKey().getApplication().getName());
        assertNotNull(ppe.getKey().getApplication().getExtendedAttributes());

        assertEquals("b", ppe.getKey().getApplication().getExtendedAttributes().getValue("a"));
        assertEquals("d", ppe.getKey().getApplication().getExtendedAttributes().getValue("c"));
        assertNull(ppe.getKey().getApplication().getExtendedAttributes().getValue("non-existing-eav"));
    }

    private void basalHTTPServerValidation(RequestCase rc) throws IOException {
        PreProcessEvent ppe = setupPreProcessorEvent(rc);

        assertBasalHTTPServerValidation(ppe);
    }

    private PreProcessEvent setupPreProcessorEvent(RequestCase rc) {
        TestContext tc = createTestContextFrom(rc);

        PreProcessEvent ppe = createPreProcessorMock(tc);
        replayAll();
        return ppe;
    }

    private void assertBasalHTTPServerValidation(PreProcessEvent ppe) throws IOException {
        assertNotNull(ppe.getServerRequest());
        assertEquals("127.0.0.2", ppe.getServerRequest().getRemoteAddr());
        assertEquals("GET", ppe.getServerRequest().getMethod());
        assertEquals("https://api-unitttest.airfranceklm.com/fff?myQuery=123", ppe.getServerRequest().getURI());
        assertEquals("AAAA", AFKLMSidecarProcessor.getContentOf(ppe.getServerRequest().getBody()));
    }
    */
}
