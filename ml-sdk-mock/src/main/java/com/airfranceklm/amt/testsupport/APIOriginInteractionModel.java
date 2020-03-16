package com.airfranceklm.amt.testsupport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mashery.http.MutableHTTPHeaders;
import com.mashery.http.client.HTTPClientException;
import com.mashery.http.client.HTTPClientRequest;
import lombok.*;

import static com.airfranceklm.amt.testsupport.Mocks.copyIfNull;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.fail;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true, builderMethodName = "apiOriginInteraction")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class APIOriginInteractionModel extends RequestCaseDatum {

    @JsonProperty("modify origin request")
    @Getter
    @Setter
    private APIOriginRequestModificationModel requestModification;

    @JsonProperty("api origin response")
    @Getter
    @Setter
    private APIOriginResponseModel response;

    @JsonProperty("api origin exception")
    @Getter
    @Setter
    private String responseException;

    public <T extends MasheryProcessorTestCase> void applyExpectedResponseInteractions(TestContext<T> ctx
            , HTTPClientRequest reqMock) {

        // Mock direct send on the message.
        try {
            if (responseException != null) {
                expect(reqMock.send()).andThrow(new HTTPClientException(responseException));
            } else if (response != null) {
                expect(reqMock.send()).andReturn(response.mock(ctx)).anyTimes();
            }
        } catch (HTTPClientException e) {
            fail("Exception thrown during recording of a mock");
        }
    }

    public APIOriginInteractionModel deepCopyFrom(APIOriginInteractionModel other) {
        this.requestModification = APIOriginRequestModificationModel.deepClone(other.getRequestModification());
        this.response = APIOriginResponseModel.deepClone(other.getResponse());

        return this;
    }


    public void acceptVisitor(TestModelVisitor v) {
        v.visit(this);
        if (requestModification != null) {
            requestModification.acceptVisitor(v);
        }

        if (response != null) {
            response.acceptVisitor(v);
        }
    }

    public void inheritFrom(APIOriginInteractionModel other) {
        copyIfNull(this::getResponseException, other::getResponseException, this::setResponseException);

        copyIfNull(this::getRequestModification, other::getRequestModification, (md) -> this.setRequestModification(new APIOriginRequestModificationModel(md)));
        copyIfNull(this::getResponse, other::getResponse, (r) -> this.setResponse(new APIOriginResponseModel(r)));
    }
}