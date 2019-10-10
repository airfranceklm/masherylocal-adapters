package com.airfranceklm.amt.testsupport;

import com.airfranceklm.amt.sidecar.model.SidecarInvocationData;
import com.mashery.http.MutableHTTPHeaders;
import com.mashery.http.ParamGroup;
import com.mashery.http.ParamIterator;
import com.mashery.http.URI;
import com.mashery.http.client.HTTPClientRequest;
import com.mashery.http.client.HTTPClientResponse;
import com.mashery.http.io.ContentProducer;
import com.mashery.http.io.ContentSource;
import com.mashery.http.server.HTTPServerRequest;
import com.mashery.http.server.HTTPServerResponse;
import com.mashery.trafficmanager.cache.Cache;
import com.mashery.trafficmanager.cache.CacheException;
import com.mashery.trafficmanager.debug.DebugContext;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import com.mashery.trafficmanager.model.auth.AuthorizationType;
import com.mashery.trafficmanager.model.core.*;
import com.mashery.trafficmanager.model.oauth.AccessToken;
import com.mashery.trafficmanager.model.oauth.OAuthContext;
import com.mashery.trafficmanager.model.oauth.TokenType;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;

import java.io.IOException;
import java.util.*;

import static junit.framework.Assert.assertNotNull;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.fail;

/**
 * Support for the unit-testing of the Mashery pre- and post-processors.
 */
public abstract class RequestMockSupport<T extends RequestCase> extends EasyMockSupport {

    /**
     * Creates a test context that will slurpScenarioData mocks for the specified request case.
     *
     * @param rc request case.
     * @return value of the test context.
     */
    protected TestContext createTestContextFrom(T rc) {
        return new TestContext(rc);
    }

    protected PreProcessEvent createPreProcessorMock(T rc) {
        return createPreProcessorMock(createTestContextFrom(rc));
    }

    /**
     * Create a mock for the pre-processor
     *
     * @return a mock of the pre-processor event.
     */
    protected PreProcessEvent createPreProcessorMock(TestContext tc) {
        if (tc == null) {
            throw new IllegalArgumentException("Test context must not be null");
        }

        PreProcessEvent ppe = createMock(PreProcessEvent.class);
        expect(ppe.getType()).andReturn(PreProcessEvent.EVENT_TYPE).anyTimes();

        mockCommonEventMethods(tc, ppe);


        // ---------------------------------------------------------------------
        // Specific for the pre-processor event.

        // Set up the request that the client has sent to the API gateway.
        if (tc.rc.apiClientRequest != null) {
            expect(ppe.getServerRequest()).andReturn(tc.createServerRequest()).anyTimes();
        }

        // Mock the request that Mashery will send to the API origin serve.
        expect(ppe.getClientRequest()).andReturn(tc.createClientRequest()).anyTimes();
        expect(ppe.getCache()).andReturn(tc.getCacheMock()).anyTimes();
        expect(ppe.getDebugContext()).andReturn(tc.getDebugContextMock()).anyTimes();
        return ppe;
    }

    protected PostProcessEvent createPostProcessorMock(T rc) {
        return createPostProcessorMock(createTestContextFrom(rc));
    }

    protected PostProcessEvent createPostProcessorMock(TestContext tc) {
        assertNotNull(tc);
        PostProcessEvent ppe = createMock(PostProcessEvent.class);
        expect(ppe.getType()).andReturn(PostProcessEvent.EVENT_TYPE).anyTimes();

        mockCommonEventMethods(tc, ppe);

        // --------------------------------------------------
        // Specific support for the post-processor events.
        expect(ppe.getClientResponse()).andReturn(tc.createClientResponseMock()).anyTimes();
        if (tc.getRequestCase().httpServerResponseData != null) {
            expect(ppe.getServerResponse()).andReturn(tc.createHTTPServerResponseMock()).anyTimes();
        }

        expect(ppe.getCache()).andReturn(tc.createCacheMock()).anyTimes();
        expect(ppe.getDebugContext()).andReturn(tc.createDebugContextMock()).anyTimes();
        return ppe;
    }

    private void mockCommonEventMethods(TestContext tc, ProcessorEvent ppe) {
        // Assemble the call requests.
        expect(ppe.getCallContext()).andReturn(tc.createAPICallContextMock()).anyTimes();

        //------------------------
        // Set up the endpoint mock.
        expect(ppe.getEndpoint()).andReturn(tc.createEndpointMock()).anyTimes();

        // -------------------------------------------------------------------------------------------
        // Mashery has authenticated it and has identified the key.
        // Creating the key and the application
        expect(ppe.getKey()).andReturn(tc.createKeyMock()).anyTimes();
        expect(ppe.getAuthorizationContext()).andReturn(tc.createOAuthAuthorizationContext()).anyTimes();
    }





    /**
     * Checks that the content producer will yield the expected string, exactly as specified.
     *
     * @param str expected string
     * @return mock stub.
     */
    public static ContentProducer contentProducerYielding(String str) {
        reportMatcher(new ContentProducerArgumentsMatcher(str));
        return null;
    }

    class ParamsIteratorImpl implements ParamIterator {

        Iterator<String> backingIterator;
        String value = null;

        ParamsIteratorImpl(Set<String> backingSet) {
            this.backingIterator = backingSet.iterator();
        }

        @Override
        public void remove() throws IllegalStateException, UnsupportedOperationException {
            throw new UnsupportedOperationException("Should not be called.");
        }

        @Override
        public String value() throws IllegalStateException {
            return value;
        }

        @Override
        public boolean hasNext() {
            return backingIterator.hasNext();
        }

        @Override
        public String next() {
            if (backingIterator.hasNext()) {
                value = backingIterator.next();
            } else {
                value = null;
            }
            return value;
        }
    }


    protected class TestContext {
        private T rc;
        ContentSource sourceMock;
        ContentSource responseMock;
        HTTPServerRequest serverRequestMock;
        HTTPClientRequest clientRequestMock;

        DebugContext debugContextMock;
        Cache cacheMock;

        public T getRequestCase() {
            return rc;
        }

        public T getRequestContext() {
            return rc;
        }

        public DebugContext getDebugContextMock() {
            return debugContextMock;
        }

        public Cache getCacheMock() {
            return cacheMock;
        }

        private Cache createCacheMock() {
            Cache retVal = createMock(Cache.class);

            // TODO: check on the context if any specific values are required.
            try {
                expect(retVal.get(anyObject(), anyString())).andReturn(null).anyTimes();
            } catch (CacheException e) {
                fail(String.format("Could not setup cache mock: %s", e.getMessage()));
            }
            return retVal;
        }

        private DebugContext createDebugContextMock() {
            DebugContext retVal = createMock(DebugContext.class);

            // TODO: fill the context.

            expect(retVal.getEntry(anyString())).andReturn(null).anyTimes();
            return retVal;
        }

        TestContext(T rc) {
            this.rc = rc;

            if (rc.hasClientRequestBody()) {
                // The source will be available only if there is data; otherwise it will be null.
                // Hence the classes also need to incorporate the necessary checks.
                sourceMock = createMock(ContentSource.class);
                expect(sourceMock.getContentLength()).andAnswer(rc.getAPIClientContentLength()).anyTimes();
                try {
                    if (rc.hasClientRequestBody()) {
                        expect(sourceMock.getInputStream()).andAnswer(rc.getAPIClientContentStream()).anyTimes();
                    } else {
                        // Question: is this how Mashery is really working?
                        expect(sourceMock.getInputStream()).andThrow(new IOException("No content")).anyTimes();
                    }
                } catch (IOException ex) {
                    throw new IllegalStateException("I/OException must never be thrown during the setup of a mock");
                }
            }

            if (rc.hasClientResponseBody()) {
                // The source will be available only if there is data; otherwise it will be null.
                // Hence the classes also need to incorporate the necessary checks.
                responseMock = createMock(ContentSource.class);
                expect(responseMock.getContentLength()).andAnswer(rc.getAPIOriginResponseContentLength()).anyTimes();
                try {
                    if (rc.hasClientResponseBody()) {
                        expect(responseMock.getInputStream()).andAnswer(rc.getAPIOriginResponseContentStream()).anyTimes();
                    } else {
                        // Question: is this how Mashery is really working?
                        expect(responseMock.getInputStream()).andThrow(new IOException("No content")).anyTimes();
                    }
                } catch (IOException ex) {
                    throw new IllegalStateException("I/OException must never be thrown during the setup of a mock");
                }
            }

            this.debugContextMock = createDebugContextMock();
            this.cacheMock = createCacheMock();
        }

        HTTPClientResponse createClientResponseMock() {
            if (rc.apiOriginResponse != null) {
                HTTPClientResponse retVal = createMock(HTTPClientResponse.class);
                expect(retVal.getStatusCode()).andReturn(rc.apiOriginResponse.code);
                expect(retVal.getBody()).andReturn(responseMock).anyTimes();

                HTTPHeadersHelper httpHeadersHelper = new HTTPHeadersHelper(RequestMockSupport.this, rc.apiOriginResponse.headers);
                expect(retVal.getHeaders()).andReturn(httpHeadersHelper.createReadonlyHeaders()).anyTimes();

                return retVal;
            }

            return null;
        }

        APICall createAPICallContextMock() {
            APICall callMock = createMock(APICall.class);
            ApplicationRequest appReqMock = createMock(ApplicationRequest.class);

            // If the configuration doesn't supply any information about the client request,
            // this means that the intention of the code is not calling this at all.
            if (rc.apiClientRequest != null) {

                // Mocking the URI, it's a tough cookie since the code is using the
                // toString() method. The way to mock it is to create a mock and wrap it
                // into another object.

                if (rc.apiClientRequest.uri != null) {
                    URI usedUriValue = URI.parse(rc.apiClientRequest.uri);
                    expect(appReqMock.getURI()).andReturn(usedUriValue).anyTimes();
                }

                expect(appReqMock.getHTTPRequest()).andReturn(createServerRequest()).anyTimes();
                expect(appReqMock.getBody()).andReturn(sourceMock).anyTimes();

                final String endpURI = rc.getEndpointData().endpointURI;
                if (endpURI != null) {
                    String allRemain = rc.apiClientRequest.uri.substring(endpURI.length());
                    int queryIndex = allRemain.indexOf("?");
                    if (queryIndex > 0) {
                        expect(appReqMock.getPathRemainder()).andReturn(allRemain.substring(0, queryIndex)).anyTimes();
                        String[] params = allRemain.substring(queryIndex + 1).split("[&]");

                        final Set<String> tokens = new HashSet<>();
                        ParamGroup pg = createMock(ParamGroup.class);
                        for (String s : params) {
                            String[] pv = s.split("[=]");
                            expect(pg.contains(pv[0])).andReturn(true).anyTimes();
                            if (pv.length == 1) {
                                expect(pg.get(pv[0])).andReturn(null).anyTimes();
                            } else {
                                expect(pg.get(pv[0])).andReturn(pv[1]).anyTimes();
                            }
                            tokens.add(pv[0]);
                        }

                        expect(pg.iterator()).andAnswer(new IAnswer<ParamIterator>() {
                            @Override
                            public ParamIterator answer() throws Throwable {
                                return new ParamsIteratorImpl(tokens);
                            }
                        }).anyTimes();

                        expect(pg.contains(anyString())).andReturn(false).anyTimes();
                        expect(pg.get(anyString())).andReturn(null).anyTimes();

                        expect(appReqMock.getQueryData()).andReturn(pg).anyTimes();
                    } else {
                        expect(appReqMock.getPathRemainder()).andReturn(allRemain).anyTimes();
                        expect(appReqMock.getQueryData()).andReturn(null).anyTimes();
                    }
                }

            }
            expect(callMock.getRequest()).andReturn(appReqMock).anyTimes();

            TrafficManagerResponse mockTrafficMgrResponse = createMock(TrafficManagerResponse.class);
            HTTPServerResponse respMock = createMock(HTTPServerResponse.class);
            MutableHTTPHeaders tmHeadersMock = createMock(MutableHTTPHeaders.class);

            expect(tmHeadersMock.get("X-Mashery-Message-ID")).andReturn("unit-test-call-uuid").anyTimes();

            // Traffic manager interactions -- if expected.
            if (rc.trafficManagerResponse != null) {
                if (rc.trafficManagerResponse.expectSetComplete != null) {
                    mockTrafficMgrResponse.setComplete();
                    expectLastCall().once();
                }
                if (rc.trafficManagerResponse.expectSetCompleteWithMessage != null) {
                    mockTrafficMgrResponse.setComplete(rc.trafficManagerResponse.expectSetCompleteWithMessage);
                    expectLastCall().once();
                }

                if (rc.trafficManagerResponse.expectSetFailedWithMessage != null) {
                    mockTrafficMgrResponse.setFailed(rc.trafficManagerResponse.expectSetFailedWithMessage);
                    expectLastCall().once();
                }

                if (rc.trafficManagerResponse.modifiesHTTPServerResponse()) {

                    if (rc.trafficManagerResponse.expectStatusCode != null) {
                        respMock.setStatusCode(rc.trafficManagerResponse.expectStatusCode.intValue());
                        expectLastCall().once();
                    }
                    if (rc.trafficManagerResponse.expectStatusMessage != null) {
                        respMock.setStatusMessage(rc.trafficManagerResponse.expectStatusMessage);
                        expectLastCall().once();
                    }
                    if (rc.trafficManagerResponse.expectResponseBody != null) {
                        respMock.setBody(contentProducerYielding(rc.trafficManagerResponse.expectResponseBody));
                        expectLastCall().once();
                    }

                    if (rc.trafficManagerResponse.responseHeaders != null) {
                        rc.trafficManagerResponse.responseHeaders.forEach((key, value) -> {
                            tmHeadersMock.set(key, value);
                            expectLastCall().once();
                        });
                    }
                }
            }

            expect(respMock.getHeaders()).andReturn(tmHeadersMock).anyTimes();
            expect(mockTrafficMgrResponse.getHTTPResponse()).andReturn(respMock).anyTimes();
            expect(callMock.getResponse()).andReturn(mockTrafficMgrResponse).anyTimes();

            return callMock;

        }

        ExtendedAttributes extendedAttributesFrom(Map<String, String> params) {
            ExtendedAttributes retVal = createMock(ExtendedAttributes.class);
            if (params != null) {
                params.forEach((key, value) -> {
                    expect(retVal.getValue(key)).andReturn(value).anyTimes();
                });
            }

            // Attempt to read a non-existing EAV will return null.
            expect(retVal.getValue(anyString())).andReturn(null).anyTimes();

            return retVal;
        }

        Endpoint createEndpointMock() {
            Endpoint mockEndpoint = createMock(Endpoint.class);
            expect(mockEndpoint.getExternalID()).andReturn(rc.getEndpointData().endpointId).anyTimes();
            expect(mockEndpoint.isPCIEnabled()).andReturn(false).anyTimes();
            expect(mockEndpoint.isSSLEnabled()).andReturn(true).anyTimes();


            Processor processorCfg = createMock(Processor.class);
            expect(processorCfg.getProcessorName()).andReturn("unittest-lambda-processor").anyTimes();
            expect(processorCfg.getPreProcessorParameters()).andReturn(rc.getEndpointData().preProcessorConfiguration).anyTimes();
            expect(processorCfg.getPostProcessorParameters()).andReturn(rc.getEndpointData().postProcessorConfiguration).anyTimes();

            expect(mockEndpoint.getProcessor()).andReturn(processorCfg).anyTimes();

            API apiMock = createMock(API.class);
            expect(apiMock.getExternalID()).andReturn(rc.getEndpointData().serviceId).anyTimes();

            expect(mockEndpoint.getAPI()).andReturn(apiMock).anyTimes();
            return mockEndpoint;
        }

        Key createKeyMock() {
            if (rc.packageKeyData != null) {
                Application appMock = createMock(Application.class);
                expect(appMock.getName()).andReturn(rc.packageKeyData.application.name).anyTimes();
                expect(appMock.getExternalID()).andReturn("unitTestApp-dkjfahkfda").anyTimes();

                ExtendedAttributes appEavs = extendedAttributesFrom(rc.packageKeyData.application.extendedAttributes);
                expect(appMock.getExtendedAttributes()).andReturn(appEavs).anyTimes();

                Key keyMock = createMock(Key.class);
                expect(keyMock.getExtendedAttributes()).andReturn(extendedAttributesFrom(rc.packageKeyData.packageKeyEAVs)).anyTimes();
                expect(keyMock.getApplication()).andReturn(appMock).anyTimes();
                expect(keyMock.getExternalID()).andReturn(rc.packageKeyData.packageKey).anyTimes();
                return keyMock;
            } else {
                return null;
            }
        }

        OAuthContext createOAuthAuthorizationContext() {
            if (rc.authorizationContext != null) {
                String accessToken = "unit-test-token";

                OAuthContext retVal = createMock(OAuthContext.class);
                expect(retVal.getType()).andReturn(AuthorizationType.OAUTH_2).anyTimes();

                // This is hard-coded for AFKLM context since we aren't using anything
                // else.
                expect(retVal.getTokenType()).andReturn("bearer").anyTimes();
                expect(retVal.getToken()).andReturn(accessToken).anyTimes();

                AccessToken accessTokenMock = createMock(AccessToken.class);
                expect(accessTokenMock.getAccessToken()).andReturn(accessToken).anyTimes();

                // This is also hard-coded since we aren't using anything els.e
                expect(accessTokenMock.getAccessTokenType()).andReturn(TokenType.BEARER).anyTimes();

                // The expiry date of the token is always set some time in the future.
                expect(accessTokenMock.getExpires()).andReturn(rc.authorizationContext.expires).anyTimes();
                expect(accessTokenMock.getScope()).andReturn(rc.authorizationContext.scope).anyTimes();
                expect(accessTokenMock.getUserToken()).andReturn(rc.authorizationContext.userContext).anyTimes();
                expect(accessTokenMock.getGrantType()).andReturn(rc.authorizationContext.grantType).anyTimes();

                expect(retVal.getAccessToken()).andReturn(accessTokenMock).anyTimes();

                return retVal;
            } else {
                return null;
            }
        }

        /**
         * Request that Mashery would be about to send.
         *
         * @return
         */
        HTTPClientRequest createClientRequest() {
            if (clientRequestMock == null) {
                doCreateClientRequest();
            }

            return clientRequestMock;
        }

        private void doCreateClientRequest() {
            if (rc.apiOriginRequest != null) {
                clientRequestMock = createMock(HTTPClientRequest.class);

                expect(clientRequestMock.getMethod()).andReturn(rc.apiClientRequest.httpVerb).anyTimes();
                expect(clientRequestMock.getURI()).andReturn(rc.apiOriginRequest.uri).anyTimes();

                // Computation of the headers that should be sent by Mashery.
                Map<String, String> mashReqHeaders = new HashMap<>();
                if (rc.apiClientRequest.headers != null) {
                    mashReqHeaders.putAll(rc.apiClientRequest.headers);
                }

                if (rc.apiOriginRequest.masheryDroppedHeaders != null) {
                    List<String> h = rc.apiOriginRequest.masheryDroppedHeaders;
                    for (String s : h) {
                        mashReqHeaders.remove(s);
                    }
                }

                if (rc.apiOriginRequest.masheryAddedHeaders != null) {
                    mashReqHeaders.putAll(rc.apiOriginRequest.masheryAddedHeaders);
                }

                // ---------------------------------------------------------------------------
                // Headers.

                HTTPHeadersHelper headersHelper = new HTTPHeadersHelper(RequestMockSupport.this, mashReqHeaders);
                headersHelper.initAccumulatingMock();


                if (rc.apiOriginRequest.expectedSidecarDroppedHeaders != null) {
                    for (String hdr : rc.apiOriginRequest.expectedSidecarDroppedHeaders) {
                        headersHelper.dropOnce(hdr);
                    }
                }

                if (rc.apiOriginRequest.expectedSidecarAddedHeaders != null) {
                    rc.apiOriginRequest.expectedSidecarAddedHeaders.forEach((key, value) -> {
                        headersHelper.accumulateOnce(key, value.toString());
                    });
                }
                expect(clientRequestMock.getHeaders()).andReturn(headersHelper.getAccumulatedMock()).anyTimes();

                expectAPIOriginRequestChanges();
            }
        }

        /**
         * If the lambda output is such that the input to the API needs to be modified,
         * then these need to be captured, too
         */
        private void expectAPIOriginRequestChanges() {
            if (rc.apiOriginRequest.expectSetVerb != null) {
                clientRequestMock.setMethod(rc.apiOriginRequest.expectSetVerb);
                expectLastCall().once();
            }
            if (rc.apiOriginRequest.expectSetUri != null) {
                clientRequestMock.setURI(rc.apiOriginRequest.expectSetUri);
                expectLastCall().once();
            }
            if (rc.apiOriginRequest.expectOverrideBody) {
                // The body needs to be overridden with the response that was
                // returned from the lambda function.
                clientRequestMock.setBody(contentProducerYielding(rc.apiOriginRequest.expectOverridingBodyValue));
                expectLastCall().once();
            }
        }

        /**
         * Mock the request that is sent from the client to the server
         *
         * @return created mock
         */
        HTTPServerRequest createServerRequest() {
            if (serverRequestMock == null) {
                doCreateServerRequest();
            }

            return serverRequestMock;
        }

        private void doCreateServerRequest() {
            serverRequestMock = createMock(HTTPServerRequest.class);

            expect(serverRequestMock.getRemoteAddr()).andReturn(rc.apiClientRequest.remoteAddr).anyTimes();
            expect(serverRequestMock.getMethod()).andReturn(rc.apiClientRequest.httpVerb).anyTimes();
            expect(serverRequestMock.getURI()).andReturn(rc.apiClientRequest.uri).anyTimes();
            expect(serverRequestMock.getTimestamp()).andReturn(System.currentTimeMillis()).anyTimes();

            expect(serverRequestMock.isSecure()).andReturn(rc.apiClientRequest.overSSL).anyTimes();
            expect(serverRequestMock.getVersion()).andReturn(rc.apiClientRequest.version).anyTimes();

            HTTPHeadersHelper headersHelper = new HTTPHeadersHelper(RequestMockSupport.this, rc.apiClientRequest.headers);
            expect(serverRequestMock.getHeaders()).andReturn(headersHelper.createReadonlyHeaders()).anyTimes();

            expect(serverRequestMock.getBody()).andReturn(sourceMock).anyTimes();

            // Expect the body.
        }

        HTTPServerResponse createHTTPServerResponseMock() {
            if (rc.httpServerResponseData != null) {
                HTTPServerResponse respMock = createMock(HTTPServerResponse.class);
                if (rc.httpServerResponseData.getCode() != null) {
                    respMock.setStatusCode(rc.httpServerResponseData.getCode().intValue());
                    expectLastCall().once();
                }
                if (rc.httpServerResponseData.getStatusMessage() != null) {
                    respMock.setStatusMessage(rc.httpServerResponseData.getStatusMessage());
                    expectLastCall().once();
                }
                if (rc.httpServerResponseData.getPayload() != null) {
                    respMock.setBody(contentProducerYielding(rc.httpServerResponseData.getPayload()));
                    expectLastCall().once();
                }
                if (rc.httpServerResponseData.getHeaders() != null) {
                    MutableHTTPHeaders writeHeaderMock = createMock(MutableHTTPHeaders.class);
                    rc.httpServerResponseData.getHeaders().forEach((key, value) -> {
                        writeHeaderMock.set(key, value);
                        expectLastCall().once();
                    });
                }
                return respMock;
            } else {
                return null;
            }
        }
    }


}
