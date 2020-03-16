package com.airfranceklm.amt.testsupport;

import com.mashery.http.client.HTTPClientRequest;
import com.mashery.http.io.ContentSource;
import com.mashery.http.server.HTTPServerRequest;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import static com.airfranceklm.amt.testsupport.MasheryEndpointModel.masheryEndpointModel;
import static org.junit.Assert.*;

public class APIClientRequestModelGETMethodTest extends EasyMockSupport {

    private MasheryEndpointModel model;
    private APIClientRequestModel clientRequest;

    @Before
    public void init() {
        MasheryEndpointModel.MasheryEndpointModelBuilder b = masheryEndpointModel();

        b.endpointName("unit-test")
                .endpointURI("https://api-unittest.afklm.com/an/api")
                .originURI("https://api-unittset.origin.afklmbackend.com/infra/unit-test-api")
                .originQueryParam("rootPath", "/an/api")
                .originQueryParam("x-unittest", "true");

        model = b.build();

        APIClientRequestModel.APIClientRequestModelBuilder reqBuilder = APIClientRequestModel.apiClientRequest();
        reqBuilder.remoteAddr("192.168.0.1")
                .version("HTTP/1.1")
                .header("Accept", "application/json")
                .header("Accept-Encoding", "gzip, deflate")
                .httpVerb("GET")
                .queryParam("app", "yes");
        clientRequest = reqBuilder.build();
    }

    @Test
    public void testNullContentSourceOnNoBodySpecified() {
        // TODO: Check in Mashery is this is how it works.
        assertNull(clientRequest.mockContentSource(this));
    }

    @Test
    public void testCreatingAPIClientRequest() {
        MasheryProcessorTestCase tc = MasheryProcessorTestCase.buildProcessorCase()
                .endpoint(model)
                .clientRequest(clientRequest)
                .build();

        TestContext<MasheryProcessorTestCase> ctx = new TestContext<>(this, tc);

        ContentSource cs = clientRequest.mockContentSource(this);
        HTTPServerRequest req = clientRequest.mock(ctx);
        replayAll();

        assertEquals("GET", req.getMethod());
        assertEquals("192.168.0.1", req.getRemoteAddr());
        assertEquals("HTTP/1.1", req.getVersion());
        assertEquals("https://api-unittest.afklm.com/an/api?app=yes", req.getURI());

        assertEquals("application/json", req.getHeaders().get("Accept"));
        assertEquals("application/json", req.getHeaders().get("accept"));

        assertEquals("gzip, deflate", req.getHeaders().get("Accept-Encoding"));
        assertEquals("gzip, deflate", req.getHeaders().get("accept-encoding"));

        assertNull(req.getBody());
        // TODO: Check in Mashery if this is how it really works for get methods.

        // Check for found headers.
        boolean accFound = false;
        boolean encFound = false;
        for (String s: req.getHeaders()) {
            switch (s) {
                case "Accept": accFound = true; break;
                case "Accept-Encoding": encFound = true; break;
                default: fail(String.format("Unexpected header %s in enumeration", s));
            }
        }

        verifyAll();
    }

    @Test
    public void testCreatingAPIClientRequestWithoutQueryString() {
        MasheryProcessorTestCase tc = MasheryProcessorTestCase.buildProcessorCase()
                .endpoint(model)
                .clientRequest(clientRequest.toBuilder().clearQueryParams().build())
                .build();

        TestContext<MasheryProcessorTestCase> ctx = new TestContext<>(this, tc);

        HTTPServerRequest req = tc.getClientRequest().mock(ctx);
        replayAll();

        assertEquals("https://api-unittest.afklm.com/an/api", req.getURI());
        verifyAll();
    }

    @Test
    public void testCreatingAPIClientRequestWithResource() {
        MasheryProcessorTestCase tc = MasheryProcessorTestCase.buildProcessorCase()
                .endpoint(model)
                .clientRequest(clientRequest.toBuilder().clearQueryParams().resource("/a/resource").build())
                .build();

        TestContext<MasheryProcessorTestCase> ctx = new TestContext<>(this, tc);

        HTTPServerRequest req = tc.getClientRequest().mock(ctx);
        replayAll();

        assertEquals("https://api-unittest.afklm.com/an/api/a/resource", req.getURI());
        verifyAll();
    }

    @Test
    public void testCreatingAPIOriginRequest() {
        MasheryProcessorTestCase tc = MasheryProcessorTestCase.buildProcessorCase()
                .endpoint(model)
                .clientRequest(clientRequest)
                .build();

        TestContext<MasheryProcessorTestCase> ctx = new TestContext<>(this, tc);
        HTTPClientRequest mock = clientRequest.mockOriginRequest(ctx);

        replayAll();

        assertEquals("GET", mock.getMethod());
        assertEquals("https://api-unittset.origin.afklmbackend.com/infra/unit-test-api?app=yes&rootPath=%2Fan%2Fapi&x-unittest=true", mock.getURI());

        assertEquals("application/json", mock.getHeaders().get("Accept"));
        assertEquals("gzip, deflate", mock.getHeaders().get("Accept-Encoding"));

        verifyAll();
    }
}
