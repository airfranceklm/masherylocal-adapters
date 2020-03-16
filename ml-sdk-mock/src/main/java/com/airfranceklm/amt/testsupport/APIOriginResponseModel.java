package com.airfranceklm.amt.testsupport;

import com.airfranceklm.amt.testsupport.mocks.HTTPHeadersHelper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mashery.http.client.HTTPClientResponse;
import com.mashery.http.io.ContentSource;
import lombok.*;
import org.easymock.EasyMockSupport;

import java.util.Map;

import static com.airfranceklm.amt.testsupport.Mocks.copyIfNull;
import static com.airfranceklm.amt.testsupport.Mocks.copyIfNullMap;
import static org.easymock.EasyMock.expect;

@NoArgsConstructor
public class APIOriginResponseModel extends HttpResponseMessageData {


    public APIOriginResponseModel(APIOriginResponseModel other) {
        deepCopyFrom(other);
    }

    @Builder(toBuilder = true, builderMethodName = "apiOriginResponse")
    public APIOriginResponseModel(long payloadLength, String payload, String base64BinaryPayload
            , byte[] binaryPayload, Class<?> payloadOwner, String payloadResource
            , @Singular Map<String, String> headers, boolean reproducible
            , String contentAccessException, Integer statusCode
            , String statusMessage, String version) {

        super(payloadLength, payload, base64BinaryPayload, binaryPayload
                , payloadOwner, payloadResource, headers, reproducible, contentAccessException, version, statusCode, statusMessage);

    }

    public APIOriginResponseModel inheritFrom(APIOriginResponseModel other) {
        super.inheritFrom(other);

        copyIfNull(this::getStatusCode, other::getStatusCode, this::setStatusCode);
        copyIfNull(this::getStatusMessage, other::getStatusMessage, this::setStatusMessage);

        return this;
    }

    public APIOriginResponseModel deepCopyFrom(APIOriginResponseModel other) {
        super.deepCopyFrom(other);
        return this;
    }

    public int effectiveResponseCode() {
        return getStatusCode() != null ? getStatusCode() : 200;
    }

    public <T extends MasheryProcessorTestCase> HTTPClientResponse mock(TestContext<T> context) {
        final EasyMockSupport owner = context.getOwner();

        HTTPClientResponse resp = owner.createMock(HTTPClientResponse.class);

        expect(resp.getStatusMessage()).andReturn(getStatusMessage()).anyTimes();
        expect(resp.getStatusCode()).andReturn(effectiveResponseCode()).anyTimes();
        expect(resp.getVersion()).andReturn(version).anyTimes();

        final ContentSource contentSource = context.allocOrGetOriginContentSource(() -> mockContentSource(owner));
        expect(resp.getBody()).andReturn(contentSource).anyTimes();

        HTTPHeadersHelper helper = new HTTPHeadersHelper(owner, getHeaders());
        expect(resp.getHeaders()).andReturn(helper.createReadonlyHeaders()).anyTimes();

        return resp;
    }

    public static APIOriginResponseModel deepClone(APIOriginResponseModel other) {
        if (other != null) {
            return new APIOriginResponseModel().deepCopyFrom(other);
        } else {
            return null;
        }
    }

    public void acceptVisitor(TestModelVisitor v) {
        v.visit(this);
    }
}
