package com.airfranceklm.amt.testsupport;

import com.airfranceklm.amt.testsupport.mocks.HTTPClientRequestImpl;
import com.airfranceklm.amt.testsupport.mocks.HTTPHeadersHelper;
import com.airfranceklm.amt.testsupport.mocks.MutableHTTPHeadersImpl;
import com.airfranceklm.amt.testsupport.mocks.ReadonlyParamGroupImpl;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mashery.http.MutableHTTPHeaders;
import com.mashery.http.URI;
import com.mashery.http.client.HTTPClientRequest;
import com.mashery.http.io.ContentSource;
import com.mashery.http.server.HTTPServerRequest;
import com.mashery.trafficmanager.model.core.ApplicationRequest;
import lombok.*;
import org.easymock.EasyMockSupport;

import java.util.HashMap;
import java.util.Map;

import static com.airfranceklm.amt.testsupport.Mocks.*;
import static com.airfranceklm.amtml.payload.PayloadOperations.produceFromBinary;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class APIClientRequestModel extends HttpMessageData {

    @JsonProperty("over ssl")
    @Getter
    @Setter
    protected Boolean overSSL = true;
    @JsonProperty("remote address")
    @Getter
    @Setter
    protected String remoteAddr;
    @JsonProperty("http verb")
    @Getter
    @Setter
    protected String httpVerb;

    @JsonProperty("resource")
    @Getter
    @Setter
    protected String resource;

    @Getter
    @Setter
    @JsonProperty("query params")
    protected Map<String, String> queryParams;

    @Getter
    @Setter
    protected Map<String, String> pathVariables;

    @JsonProperty("authorization context")
    @Getter
    @Setter
    protected MasheryAuthorizationContextModel authorizationContext;

    @Getter
    @Setter
    protected MasheryApplicationModel application;

    @Builder(toBuilder = true, builderMethodName = "apiClientRequest")
    public APIClientRequestModel(@Singular Map<String, String> headers
            , @Singular Map<String, String> queryParams
            , @Singular Map<String, String> pathVariables
            , long payloadLength
            , String payload
            , String base64BinaryPayload
            , byte[] binaryPayload
            , Class<?> payloadOwner
            , String payloadResource
            , String version
            , boolean overSSL
            , String remoteAddr
            , String httpVerb
            , String resource
            , boolean reproducible
            , String contentAccessException) {

        super(payloadLength
                , payload
                , base64BinaryPayload
                , binaryPayload
                , payloadOwner
                , payloadResource
                , headers
                , reproducible
                , contentAccessException
                , version);

        this.queryParams = queryParams;
        this.pathVariables = pathVariables;

        this.version = version;
        this.overSSL = overSSL;
        this.remoteAddr = remoteAddr;
        this.httpVerb = httpVerb;
        this.resource = resource;
    }

    public APIClientRequestModel deepCopyFrom(@NonNull APIClientRequestModel another) {
        super.deepCopyFrom(another);

        Mocks.cloneNullableMap(another::getQueryParams, this::setQueryParams, HashMap::new);
        Mocks.cloneNullableMap(another::getPathVariables, this::setPathVariables, HashMap::new);

        this.version = another.version;
        this.overSSL = another.overSSL;
        this.remoteAddr = another.remoteAddr;
        this.httpVerb = another.httpVerb;
        this.resource = another.resource;

        return this;
    }

    public APIClientRequestModel inheritFrom(@NonNull APIClientRequestModel another) {
        inheritClientHTTPRequestFrom(another);

        copyIfNull(this::getAuthorizationContext, another::getAuthorizationContext, (c) -> this.setAuthorizationContext(new MasheryAuthorizationContextModel(c)));
        copyIfNull(this::getApplication, another::getApplication, (app) -> this.setApplication(new MasheryApplicationModel(app)));

        return this;
    }

    public void inheritClientHTTPRequestFrom(@NonNull APIClientRequestModel another) {
        super.inheritFrom(another);

        copyIfNull(this::getOverSSL, another::getOverSSL, this::setOverSSL);
        copyIfNull(this::getRemoteAddr, another::getRemoteAddr, this::setRemoteAddr);
        copyIfNull(this::getHttpVerb, another::getHttpVerb, this::setHttpVerb);
        copyIfNull(this::getResource, another::getResource, this::setResource);

        copyIfNullMap(this::getQueryParams, another::getQueryParams, this::setQueryParams, HashMap::new);
        copyIfNullMap(this::getPathVariables, another::getPathVariables, this::setPathVariables, HashMap::new);
    }


    public <T extends MasheryProcessorTestCase> HTTPClientRequest mockOriginRequest(@NonNull TestContext<T> context) {

        EasyMockSupport owner = context.getOwner();
        assertNotNull(owner);

        MasheryEndpointModel mdl = nonNullOf(context.getTestCase().getEndpoint(), MasheryEndpointModel.FALLBACK_ENDPOINT);

        APIOriginInteractionModel interactionModel = context.getTestCase().getOriginInteraction();

        HTTPClientRequestImpl.HTTPClientRequestImplBuilder builder = HTTPClientRequestImpl.builder();
        builder.URI(computeOriginURI(mdl))
                .method(httpVerb)
                .version(version);

        syncPayloadLength();
        if (getPayloadLength() > 0) {
            builder.body(produceFromBinary(resolvePayload()));
        }

        MutableHTTPHeadersImpl headersDelegate = new MutableHTTPHeadersImpl(mdl.computeHeadersForOrigin(getHeaders()));
        builder.headers(headersDelegate);

        // If interaction model specifies the interaction, a mock will be setup to facade the interaction
        // with the lenient delegate
        if (interactionModel != null) {
            final APIOriginRequestModificationModel reqMod = interactionModel.getRequestModification();

            if (reqMod != null) {
                MutableHTTPHeaders mock = owner.createMock(MutableHTTPHeaders.class);
                expect(mock.iterator()).andDelegateTo(headersDelegate).anyTimes();
                expect(mock.get(anyString())).andDelegateTo(headersDelegate).anyTimes();

                reqMod.requireExpectedHeaderInteraction(mock, headersDelegate);
                builder.headers(mock);
            }
        }

        HTTPClientRequestImpl delegate = builder.build();

        if (interactionModel == null) {
            return delegate;
        } else {
            HTTPClientRequest req = owner.createMock(HTTPClientRequest.class);
            delegateGetters(req, delegate);

            interactionModel.applyExpectedResponseInteractions(context, req);
            if (interactionModel.getRequestModification() != null) {
                interactionModel.getRequestModification().requireExpectedRequestInteraction(req, delegate);
            }
            return req;
        }
    }

    private String computeOriginURI(MasheryEndpointModel mdl) {
        return Mocks.computeRequestURI(nonNullOf(mdl.getOriginURI(), MasheryEndpointModel.UNDEFINED_BACKEND_URL)
                , getResource()
                , getPathVariables()
                , Mocks.joinNullableMaps(getQueryParams(), mdl.getOriginQueryParams()));
    }

    private void delegateGetters(HTTPClientRequest resp, HTTPClientRequestImpl delegate) {
        expect(resp.getMethod()).andDelegateTo(delegate).anyTimes();
        expect(resp.getURI()).andDelegateTo(delegate).anyTimes();
        expect(resp.getHeaders()).andDelegateTo(delegate).anyTimes();
        expect(resp.getBody()).andDelegateTo(delegate).anyTimes();
    }

    public <T extends MasheryProcessorTestCase> HTTPServerRequest mock(@NonNull TestContext<T> context) {
        EasyMockSupport owner = context.getOwner();
        assertNotNull(owner);

        MasheryEndpointModel mdl = nonNullOf(context.getTestCase().getEndpoint(), MasheryEndpointModel.FALLBACK_ENDPOINT);
        assertNotNull("You need an endpoint to build an HTTP Server Request", mdl);

        ContentSource cs = context.allocOrGetClientContentSource(() -> mockContentSource(owner));

        return mockAPIClientRequestToMashery(owner, mdl, cs);
    }

    HTTPServerRequest mockAPIClientRequestToMashery(EasyMockSupport owner, MasheryEndpointModel mdl, ContentSource cs) {
        HTTPServerRequest serverRequestMock = owner.createMock(HTTPServerRequest.class);

        expect(serverRequestMock.getRemoteAddr()).andReturn(getRemoteAddr()).anyTimes();
        expect(serverRequestMock.getMethod()).andReturn(getHttpVerb()).anyTimes();

        String uri = Mocks.computeRequestURI(nonNullOf(mdl.getEndpointURI(), MasheryEndpointModel.UNDEFINED_REQUEST_URL), getResource(), getPathVariables(), getQueryParams());

        expect(serverRequestMock.getURI()).andReturn(uri).anyTimes();
        expect(serverRequestMock.getTimestamp()).andReturn(System.currentTimeMillis()).anyTimes();

        expect(serverRequestMock.isSecure()).andReturn(getOverSSL() == null ? false : getOverSSL()).anyTimes();
        expect(serverRequestMock.getVersion()).andReturn(getVersion()).anyTimes();

        HTTPHeadersHelper headersHelper = new HTTPHeadersHelper(owner, getHeaders());
        expect(serverRequestMock.getHeaders()).andReturn(headersHelper.createReadonlyHeaders()).anyTimes();

        expect(serverRequestMock.getBody()).andReturn(cs).anyTimes();

        return serverRequestMock;
    }


    public void acceptVisitor(TestModelVisitor v) {
        v.visit(this);

        if (authorizationContext != null) {
            authorizationContext.acceptVisitor(v);
        }

        if (application != null) {
            application.acceptVisitor(v);
        }
    }

    public <T extends MasheryProcessorTestCase> ApplicationRequest mockApplicationRequest(TestContext<T> ctx) {
        ApplicationRequest retVal = ctx.getOwner().createMock(ApplicationRequest.class);
        expect(retVal.getPathRemainder()).andReturn(resource).anyTimes();

        final HTTPServerRequest serverReq = ctx.allocOrGetServerRequest(() -> mock(ctx));
        expect(retVal.getHTTPRequest()).andReturn(serverReq).anyTimes();

        expect(retVal.getQueryData()).andReturn(new ReadonlyParamGroupImpl(queryParams)).anyTimes();
        expect(retVal.getBody())
                .andReturn(ctx.allocOrGetClientContentSource(() -> mockContentSource(ctx.getOwner())))
                .anyTimes();

        if (ctx.getTestCase().getEndpoint() != null) {
            expect(retVal.getURI()).andReturn(URI.parse(computeOriginURI(ctx.getTestCase().getEndpoint()))).anyTimes();
        }

        return retVal;
    }
}

