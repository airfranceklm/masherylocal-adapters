package com.airfranceklm.amt.testsupport;

import com.airfranceklm.amtml.payload.PayloadOperations;
import com.mashery.http.client.HTTPClientRequest;
import com.mashery.http.io.ContentSource;
import com.mashery.http.server.HTTPServerRequest;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.airfranceklm.amt.testsupport.MasheryEndpointModel.masheryEndpointModel;
import static org.junit.Assert.*;

public class APIClientRequestModelPOSTMethodTest extends EasyMockSupport {

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
                .header("Content-Type", "text/plain")
                .header("Accept", "application/json")
                .header("Accept-Encoding", "gzip, deflate")
                .httpVerb("POST")
                .payload("SAMPLE PAYLOAD");
        clientRequest = reqBuilder.build();
    }

    @Test
    public void testCreatingAPIClientRequest() throws IOException {
        MasheryProcessorTestCase tc = MasheryProcessorTestCase.buildProcessorCase()
                .endpoint(model)
                .clientRequest(clientRequest)
                .build();

        TestContext<MasheryProcessorTestCase> ctx = new TestContext<>(this, tc);

        ContentSource cs = clientRequest.mockContentSource(this);
        HTTPServerRequest req = clientRequest.mock(ctx);
        replayAll();

        assertEquals("POST", req.getMethod());
        assertEquals("192.168.0.1", req.getRemoteAddr());
        assertEquals("https://api-unittest.afklm.com/an/api", req.getURI());

        assertEquals("application/json", req.getHeaders().get("Accept"));
        assertEquals("application/json", req.getHeaders().get("accept"));

        assertEquals("text/plain", req.getHeaders().get("Content-Type"));
        assertEquals("text/plain", req.getHeaders().get("content-type"));

        assertEquals("gzip, deflate", req.getHeaders().get("Accept-Encoding"));
        assertEquals("gzip, deflate", req.getHeaders().get("accept-encoding"));

        assertNotNull(req.getBody());
        assertFalse(req.getBody().isRepeatable());
        assertEquals("SAMPLE PAYLOAD", PayloadOperations.getContentOf(req.getBody()));

        try {
            req.getBody().getInputStream();
            fail("Exception must be thrown on non-reproducible streams");
        } catch (IOException ex) {
            // Exception is thrown as expected
        }

        verifyAll();
    }

    @Test
    public void testCreatingAPIClientRequestWithoutQueryString() throws IOException {
        MasheryProcessorTestCase tc = MasheryProcessorTestCase.buildProcessorCase()
                .endpoint(model)
                .clientRequest(clientRequest.toBuilder().clearQueryParams().build())
                .build();

        TestContext<MasheryProcessorTestCase> ctx = new TestContext<>(this, tc);

        HTTPServerRequest req = tc.getClientRequest().mock(ctx);
        replayAll();

        assertEquals("https://api-unittest.afklm.com/an/api", req.getURI());

        assertNotNull(req.getBody());
        assertFalse(req.getBody().isRepeatable());

        assertEquals("SAMPLE PAYLOAD", PayloadOperations.getContentOf(req.getBody()));
        try {
            PayloadOperations.getContentsOf(req.getBody());
            fail("Exception must be thrown");
        } catch (IOException ex) {
            // continue.
        }

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
    public void testCreatingAPIOriginRequest() throws IOException {
        MasheryProcessorTestCase tc = MasheryProcessorTestCase.buildProcessorCase()
                .endpoint(model)
                .clientRequest(clientRequest)
                .build();

        TestContext<MasheryProcessorTestCase> ctx = new TestContext<>(this, tc);
        HTTPClientRequest mock = clientRequest.mockOriginRequest(ctx);

        replayAll();

        assertEquals("POST", mock.getMethod());
        assertEquals("https://api-unittset.origin.afklmbackend.com/infra/unit-test-api?rootPath=%2Fan%2Fapi&x-unittest=true", mock.getURI());

        assertEquals("application/json", mock.getHeaders().get("Accept"));
        assertEquals("gzip, deflate", mock.getHeaders().get("Accept-Encoding"));

        assertEquals("SAMPLE PAYLOAD", PayloadOperations.getContentOf(mock.getBody()));

        verifyAll();
    }
}
