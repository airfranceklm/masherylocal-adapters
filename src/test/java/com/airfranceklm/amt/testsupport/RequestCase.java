package com.airfranceklm.amt.testsupport;

import org.easymock.IAnswer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Consolidated information about the request case, for both pre-and post-processing.
 */
public class RequestCase {
    private TestScenario testScenario;
    private EndpointData endpointData;

    private String name;
    boolean preProcessorCase = true;
    //String inheritFrom; // TODO

    APIClientRequest apiClientRequest;
    PackageKeyData packageKeyData;
    AuthorizationContextData authorizationContext;
    APIOriginRequest apiOriginRequest;

    APIOriginResponse apiOriginResponse;

    TrafficManagerResponseData trafficManagerResponse;
    HTTPServerResponseData httpServerResponseData;

    public RequestCase() {
        // Anonymous case.
    }

    public RequestCase(String name) {
        this.name = name;
    }

    IAnswer<Long> getAPIClientContentLength() {
        return new IAnswer<Long>() {
            @Override
            public Long answer() throws Throwable {
                if (apiClientRequest == null) return 0L;
                if (apiClientRequest.payload != null) {
                    return (long) apiClientRequest.payload.length();
                } else if (apiClientRequest.payloadOwner != null && apiClientRequest.payloadResource != null) {

                    return getStreamLength(apiClientRequest.payloadOwner, apiClientRequest.payloadResource);
                } else {
                    return 0L;
                }
            }
        };
    }

    private Long getStreamLength(Class resourceOwner, String resourceName) throws IOException {
        long length = 0;
        try (InputStream is = resourceOwner.getResourceAsStream(resourceName)) {
            if (is != null) {
                byte[] buf = new byte[10240];
                int k = 0;
                while ((k = is.read(buf)) > 0) {
                    length += k;
                }
            }

            return length;
        }
    }

    IAnswer<Long> getAPIOriginResponseContentLength() {
        return new IAnswer<Long>() {
            @Override
            public Long answer() throws Throwable {
                if (apiOriginResponse == null) return 0L;
                else if (apiOriginResponse.payload != null) {
                    return apiOriginResponse.payloadLength;
                } else if (apiOriginResponse.payloadOwner != null && apiOriginResponse.payloadResource != null) {
                    return getStreamLength(apiOriginResponse.payloadOwner, apiOriginResponse.payloadResource);
                } else {
                    return 0L;
                }
            }
        };
    }

    IAnswer<InputStream> getAPIClientContentStream() {
        return new IAnswer<InputStream>() {
            @Override
            public InputStream answer() throws Throwable {
                if (apiClientRequest == null) {
                    return null;
                } else if (apiClientRequest.payload != null) {
                    return new ByteArrayInputStream(apiClientRequest.payload.getBytes(StandardCharsets.UTF_8));
                } else if (apiClientRequest.payloadOwner != null && apiClientRequest.payloadResource != null) {
                    return apiClientRequest.payloadOwner.getResourceAsStream(apiClientRequest.payloadResource);
                } else {
                    return null;
                }
            }
        };
    }

    IAnswer<InputStream> getAPIOriginResponseContentStream() {
        return new IAnswer<InputStream>() {
            @Override
            public InputStream answer() throws Throwable {
                if (apiOriginResponse == null) {
                    return null;
                } else if (apiOriginResponse.payload != null) {
                    return new ByteArrayInputStream(apiOriginResponse.payload.getBytes(StandardCharsets.UTF_8));
                } else if (apiOriginResponse.payloadOwner != null && apiOriginResponse.payloadResource != null) {
                    return apiOriginResponse.payloadOwner.getResourceAsStream(apiOriginResponse.payloadResource);
                } else {
                    return null;
                }
            }
        };
    }

    boolean hasClientRequestBody() {
        return (apiClientRequest != null && (apiClientRequest.payload != null
                || (apiClientRequest.payloadOwner != null && apiClientRequest.payloadResource != null)));
    }

    boolean hasClientResponseBody() {
        return apiOriginResponse != null &&
                (apiOriginResponse.payload != null || (apiOriginResponse.payloadOwner != null && apiOriginResponse.payloadResource != null));
    }

    public TestScenario getTestScenario() {
        return testScenario;
    }

    public boolean isPreProcessorCase() {
        return preProcessorCase;
    }

    /**
     * Computation of the cross-links between parameters and filling in the cross-referenced
     * values, also between the parameters.
     */
    protected void secondPass() {
        // API Client Request
        if (apiClientRequest != null && apiClientRequest.needsCopyFromAnotherCase()) {
            RequestCase anotherCase = dereference("API client", apiClientRequest.getCaseToCopyFrom());
            if (anotherCase.apiClientRequest == null) {
                throw new IllegalStateException(String.format("API client of case %s needs to copy data from case %s, but this case doesn't supply the client request.",
                        name,
                        apiClientRequest.getCaseToCopyFrom()));
            }

            apiClientRequest.copyFrom(anotherCase.apiClientRequest);
        }

        // Package Key data
        if (packageKeyData != null && packageKeyData.needsCopyFromAnotherCase()) {
            RequestCase keyCase = dereference("Package key data", packageKeyData.getCaseToCopyFrom());
            if (keyCase.packageKeyData != null) {
                packageKeyData.copyFrom(keyCase.packageKeyData);
            } else {
                throw new IllegalStateException(String.format("Package key information of case `%s` needs to copy data from case `%s`, but this case doesn't supply the package key information.",
                        name,
                        packageKeyData.getCaseToCopyFrom()));
            }
        }

        // Authorization context.
        if (authorizationContext != null && authorizationContext.needsCopyFromAnotherCase()) {
            RequestCase authCtxCase = dereference("Authorization context data", authorizationContext.getCaseToCopyFrom());
            if (authCtxCase.authorizationContext != null) {
                authorizationContext.copyFrom(authCtxCase.authorizationContext);
            } else {
                throw new IllegalStateException(String.format("Authorization context information of case `%s` needs to copy data from case `%s`, but this case doesn't supply the package key information.",
                        name,
                        authorizationContext.getCaseToCopyFrom()));
            }
        }

        // Traffic manager expectations.
        if (trafficManagerResponse != null && trafficManagerResponse.needsCopyFromAnotherCase()) {
            RequestCase tmCase = dereference("Traffic Manager Data", trafficManagerResponse.getCaseToCopyFrom());
            if (tmCase.trafficManagerResponse != null) {
                trafficManagerResponse.copyFrom(tmCase.trafficManagerResponse);
            } else {
                throw new IllegalStateException(String.format("Traffic manager expectations of case `%s` needs to copy data from case `%s`, but this case doesn't supply the traffic manager expectations.",
                        name,
                        trafficManagerResponse.getCaseToCopyFrom()));
            }
        }

        if (apiOriginRequest != null && apiOriginRequest.needsCopyFromAnotherCase()) {
            RequestCase tmCase = dereference("API Origin Request Data", apiOriginRequest.getCaseToCopyFrom());
            if (tmCase.apiOriginRequest != null) {
                apiOriginRequest.copyFrom(tmCase.apiOriginRequest);
            } else {
                throw new IllegalStateException(String.format("API Origin request expectations of case `%s` needs to copy data from case `%s`, but this case doesn't supply the traffic manager expectations.",
                        name,
                        apiOriginRequest.getCaseToCopyFrom()));
            }
        }
    }

    /**
     * De-references a case.
     *
     * @param elem        data structure element
     * @param anotherName name of another case.
     * @return instnace of a request case. If not found, then {@link IllegalStateException} is thrown to indicate
     * a broken reference.
     */
    private RequestCase dereference(String elem, String anotherName) {
        RequestCase anotherCase = testScenario.getRequestCase(anotherName);

        if (anotherCase == null) {
            throw new IllegalStateException(String.format("%s of case %s needs to copy data from case %s, which doesn't exist.",
                    elem,
                    name,
                    anotherName));
        }
        return anotherCase;
    }

    public APIOriginRequest getApiOriginRequest() {
        return apiOriginRequest;
    }

    void buildAPIClientRequestFromYaml(Map<String, Object> yaml) {
        this.apiClientRequest = new APIClientRequest(yaml);
    }

    void buildAuthorizationContextFromYaml(Map<String, Object> yaml) {
        this.authorizationContext = new AuthorizationContextData(yaml);
    }

    void buildAPIOriginResponseFromYaml(Map<String, Object> yaml) {
        this.apiOriginResponse = new APIOriginResponse(yaml);
    }

    void buildPackageKeyFromYaml(Map<String, Object> yaml) {
        this.packageKeyData = new PackageKeyData(yaml);
    }

    void buildTrafficManagerExpectationFromYaml(Map<String, Object> yaml) {
        this.trafficManagerResponse = new TrafficManagerResponseData(yaml);
    }

    void buildExpectedHTTPResponseFromYaml(Map<String, Object> yaml) {
        this.httpServerResponseData = new HTTPServerResponseData(yaml);
    }

    // ----------------------------------------------------------------------
    // DSL supporting methods.
    public void withAPIClientRequest(Consumer<APIClientRequest> c) {
        this.apiClientRequest = new APIClientRequest();
        c.accept(this.apiClientRequest);
    }

    public EndpointData getEndpointData() {
        if (endpointData != null) {
            return endpointData;
        } else if (testScenario != null) {
            return testScenario.getEndpointData();
        } else {
            return null;
        }
    }

    public void setEndpointData(EndpointData endpointData) {
        this.endpointData = endpointData;
    }

    void setTestScenario(TestScenario testScenario) {
        this.testScenario = testScenario;
    }

    public APIClientRequest getApiClientRequest() {
        return apiClientRequest;
    }

    public PackageKeyData getPackageKeyData() {
        return packageKeyData;
    }

    public AuthorizationContextData getAuthorizationContext() {
        return authorizationContext;
    }

    public APIOriginResponse getApiOriginResponse() {
        return apiOriginResponse;
    }

    public TrafficManagerResponseData getTrafficManagerResponse() {
        return trafficManagerResponse;
    }

    public HTTPServerResponseData getHttpServerResponseData() {
        return httpServerResponseData;
    }

    // --------------------------------------------------------------------
    // DSL Methods.
    public void withEndpointData(Consumer<EndpointData> c) {
        if (c == null) {
            return;
        }

        c.accept(getOrCreateEndpointData());
    }

    public EndpointData getOrCreateEndpointData() {
        if (this.endpointData == null) {
            this.endpointData = new EndpointData();
        }
        return this.endpointData;
    }

    public APIClientRequest getOrCreateAPIClientRequestData() {
        if (this.apiClientRequest == null) {
            this.apiClientRequest = new APIClientRequest();
        }
        return this.apiClientRequest;
    }

    public PackageKeyData getOrCreatePackageKeyData() {
        if (this.packageKeyData == null) {
            this.packageKeyData = new PackageKeyData();
        }
        return this.packageKeyData;
    }

    public AuthorizationContextData getOrCreateAuthorizationContextData() {
        if (this.authorizationContext == null) {
            this.authorizationContext = new AuthorizationContextData();
        }
        return this.authorizationContext;
    }

    public APIOriginRequest getOrCreateAPIOriginRequestData() {
        if (this.apiOriginRequest == null) {
            this.apiOriginRequest = new APIOriginRequest();
        }
        return this.apiOriginRequest;
    }

    public APIOriginResponse getOrCreateAPIOriginResponseData() {
        if (this.apiOriginResponse == null) {
            this.apiOriginResponse = new APIOriginResponse();
        }
        return this.apiOriginResponse;
    }

    public TrafficManagerResponseData getOrCreateTrafficManagerResponseData() {
        if (this.trafficManagerResponse == null) {
            this.trafficManagerResponse = new TrafficManagerResponseData();
        }
        return this.trafficManagerResponse;
    }

    public void setPreProcessorCase(boolean setting) {
        this.preProcessorCase = setting;
    }
}
