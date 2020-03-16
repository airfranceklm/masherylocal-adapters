package com.airfranceklm.amt.testsupport;

import com.airfranceklm.amt.testsupport.mocks.HTTPServerResponseImpl;
import com.airfranceklm.amt.testsupport.mocks.MutableHTTPHeadersImpl;
import com.airfranceklm.amt.testsupport.mocks.TrafficManagerResponseImpl;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mashery.http.MutableHTTPHeaders;
import com.mashery.http.server.HTTPServerResponse;
import com.mashery.trafficmanager.model.core.TrafficManagerResponse;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.locateAPIOriginResponse;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseFeature.MASHERY_MESSAGE_ID;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseFeature.MASHERY_MESSAGE_ID_HEADER;
import static com.airfranceklm.amt.testsupport.Mocks.*;
import static org.easymock.EasyMock.*;

@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class APIClientResponseModel extends HttpResponseMessageData {

    @JsonProperty("drop headers")
    @Getter @Setter @Singular
    private List<String> dropHeaders;

    @JsonProperty("complete")
    @Getter @Setter
    private Boolean expectSetComplete;

    @JsonProperty("complete with")
    @Getter @Setter
    private String expectSetCompleteWithMessage;

    @JsonProperty("failed with")
    @Getter @Setter
    private String expectSetFailedWithMessage;

    @Builder(toBuilder = true, builderMethodName = "masheryResponse")
    public APIClientResponseModel(long payloadLength
            , String payload
            , String base64BinaryPayload
            , byte[] binaryPayload
            , Class<?> payloadOwner
            , String payloadResource
            , @Singular Map<String, String> headers
            , boolean reproducible
            , String contentAccessException
            , String version
            , Integer statusCode
            , String statusMessage
            , @Singular List<String> dropHeaders
            , Boolean expectSetComplete
            , String expectSetCompleteWithMessage
            , String expectSetFailedWithMessage) {
        super(payloadLength, payload, base64BinaryPayload, binaryPayload, payloadOwner, payloadResource, headers, reproducible, contentAccessException, version, statusCode, statusMessage);
        this.dropHeaders = dropHeaders;
        this.expectSetComplete = expectSetComplete;
        this.expectSetCompleteWithMessage = expectSetCompleteWithMessage;
        this.expectSetFailedWithMessage = expectSetFailedWithMessage;
    }

    public APIClientResponseModel inheritFrom(APIClientResponseModel other) {
        super.inheritFrom(other);

        copyIfNullCollection(this::getDropHeaders, other::getDropHeaders, this::setDropHeaders, ArrayList::new);

        copyIfNull(this::getExpectSetComplete, other::getExpectSetComplete, this::setExpectSetComplete);
        copyIfNull(this::getExpectSetCompleteWithMessage, other::getExpectSetCompleteWithMessage, this::setExpectSetCompleteWithMessage);
        copyIfNull(this::getExpectSetFailedWithMessage, other::getExpectSetFailedWithMessage, this::setExpectSetFailedWithMessage);


        return this;
    }

    public <T extends MasheryProcessorTestCase> TrafficManagerResponse mockMasheryResponse(TestContext<T> ctx) {

        TrafficManagerResponseImpl tmDelegate = new TrafficManagerResponseImpl();
        final APIOriginResponseModel originResp = locateAPIOriginResponse(ctx.getTestCase());

        HTTPServerResponseImpl.HTTPServerResponseImplBuilder respBuilder = HTTPServerResponseImpl.builder();
        MutableHTTPHeadersImpl headers;

        Map<String,String> uHeaders = new HashMap<>();
        if (ctx.getTestCase().getEndpoint() != null && ctx.getTestCase().getEndpoint().isPassMessageId()) {
            uHeaders.put(MASHERY_MESSAGE_ID_HEADER, MASHERY_MESSAGE_ID);
        }

        // Inherit the content from the API origin response
        if (originResp != null) {
            originResp.syncPayloadLength();

            if (originResp.getHeaders() != null) {
                uHeaders.putAll(originResp.getHeaders());
            }
            headers = new MutableHTTPHeadersImpl(uHeaders);

            respBuilder
                    .headers(headers)
                    .statusCode(originResp.effectiveResponseCode())
                    .statusMessage(originResp.getStatusMessage())
                    .body(originResp.producer());
        } else {
            // Create headers with null contents.
            headers = new MutableHTTPHeadersImpl(uHeaders);

            respBuilder
                    .headers(headers)
                    .statusCode(200)
                    .statusMessage("Ok");
        }

        HTTPServerResponseImpl responseDelegate = respBuilder.build();
        ctx.setHttpServerResponse(responseDelegate);

        tmDelegate.setHTTPResponse(responseDelegate);

        if (!isLenient()) {

            // If the model specifies the interaction, we'll replace the object with the mock
            // that will control the behaviour.

            if (getHeaders() != null || getDropHeaders() != null) {
                MutableHTTPHeaders headersMock = ctx.getOwner().createMock(MutableHTTPHeaders.class);
                expect(headersMock.get(anyString())).andDelegateTo(headers).anyTimes();
                expect(headersMock.contains(anyString())).andDelegateTo(headers).anyTimes();
                expect(headersMock.iterator()).andDelegateTo(headers).anyTimes();

                if (getHeaders() != null) {
                    getHeaders().forEach((k, v) -> {
                        headersMock.set(k, v);
                        expectLastCall().andDelegateTo(headers).once();
                    });
                }

                if (getDropHeaders() != null) {
                    getDropHeaders().forEach((h) -> {
                        headersMock.remove(h);
                        expectLastCall().andDelegateTo(headers).once();
                    });
                }

                responseDelegate.setHeaders(headersMock);
            }

            if (modifiesMessage()) {
                HTTPServerResponse respMock = ctx.getOwner().createMock(HTTPServerResponse.class);

                expect(respMock.getStatusCode()).andDelegateTo(responseDelegate).anyTimes();
                expect(respMock.getStatusMessage()).andDelegateTo(responseDelegate).anyTimes();
                expect(respMock.getHeaders()).andDelegateTo(responseDelegate).anyTimes();
                expect(respMock.getBody()).andDelegateTo(responseDelegate).anyTimes();
                expect(respMock.getVersion()).andDelegateTo(responseDelegate).anyTimes();

                if (getStatusCode() != null) {
                    respMock.setStatusCode(getStatusCode());
                    expectLastCall().andDelegateTo(responseDelegate).once();
                }

                if (getStatusMessage() != null) {
                    respMock.setStatusMessage(getStatusMessage());
                    expectLastCall().andDelegateTo(respBuilder).once();
                }

                if (specifiesPayload()) {
                    respMock.setBody(contentProducerYielding(resolvePayload()));
                    expectLastCall().andDelegateTo(responseDelegate).once();
                }

                tmDelegate.setHTTPResponse(respMock);
                ctx.setHttpServerResponse(respMock);
            }
        }

        if (isCompleting()) {
            TrafficManagerResponse tmMock = ctx.getOwner().createMock(TrafficManagerResponse.class);
            expect(tmMock.getHTTPResponse()).andDelegateTo(tmDelegate).anyTimes();

            if (expectSetComplete != null && expectSetComplete) {
                tmMock.setComplete();
                expectLastCall().andDelegateTo(tmDelegate).once();
            }

            if (expectSetCompleteWithMessage != null) {
                tmMock.setComplete(expectSetCompleteWithMessage);
                expectLastCall().andDelegateTo(tmDelegate).once();
            }

            if (expectSetFailedWithMessage != null) {
                tmMock.setFailed(expectSetFailedWithMessage);
                expectLastCall().andDelegateTo(tmDelegate).once();
            }

            return tmMock;
        } else {
            return tmDelegate;
        }

    }

    private boolean isCompleting() {
        return expectSetComplete != null || expectSetCompleteWithMessage != null || expectSetFailedWithMessage != null;
    }

    private boolean isLenient() {
        return getHeaders() == null
                && getDropHeaders() == null
                && getStatusCode() == null
                && getStatusMessage() == null
                && !specifiesPayload();
    }

    private boolean modifiesMessage() {
        return getStatusCode() == null
                && getStatusMessage() == null
                && !specifiesPayload();
    }


    public void deepCopyFrom(APIClientResponseModel other) {
        super.deepCopyFrom(other);

        this.expectSetComplete = other.expectSetComplete;
        this.expectSetCompleteWithMessage = other.expectSetCompleteWithMessage;
        this.expectSetFailedWithMessage = other.expectSetFailedWithMessage;
    }

    public void acceptVisitor(TestModelVisitor v) {
        v.visit(this);
    }
}
