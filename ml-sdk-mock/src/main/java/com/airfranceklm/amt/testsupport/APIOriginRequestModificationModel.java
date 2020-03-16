package com.airfranceklm.amt.testsupport;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mashery.http.MutableHTTPHeaders;
import com.mashery.http.client.HTTPClientRequest;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.airfranceklm.amt.testsupport.Mocks.*;
import static org.easymock.EasyMock.expectLastCall;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true, builderMethodName = "originRequestModification")
public class APIOriginRequestModificationModel extends RequestCaseDatum {

    @JsonProperty("drop headers") @Getter
    @Setter
    @Singular
    List<String> expectedDroppedHeaders;

    @JsonProperty("add headers") @Getter @Setter @Singular
    Map<String, String> expectedAddedHeaders;

    @JsonProperty("set http verb") @Getter @Setter
    String expectSetVerb;

    @JsonProperty("set uri") @Getter @Setter
    String expectSetUri;

    @JsonProperty("replace payload") @Getter @Setter
    PayloadSourceModel payloadModel;

    public APIOriginRequestModificationModel(APIOriginRequestModificationModel other) {
        if (other != null) {
            deepCopyFrom(other);
        }
    }

    public APIOriginRequestModificationModel deepCopyFrom(@NonNull APIOriginRequestModificationModel other) {

        Mocks.cloneNullableCollection(other::getExpectedDroppedHeaders, this::setExpectedDroppedHeaders, ArrayList::new);
        Mocks.cloneNullableMap(other::getExpectedAddedHeaders, this::setExpectedAddedHeaders, HashMap::new);


        this.expectSetVerb = other.expectSetVerb;
        this.expectSetUri = other.expectSetUri;

        this.payloadModel = PayloadSourceModel.deepClone(other.getPayloadModel());

        return this;
    }

    public void requireExpectedHeaderInteraction(MutableHTTPHeaders headersMock, MutableHTTPHeaders headersAccumulator) {
        forEachAddedHeader((h, v) -> {
            headersMock.set(h, v);
            expectLastCall().andDelegateTo(headersAccumulator).once();
        });

        forEachDroppedHeader((h) -> {
            headersMock.remove(h);
            expectLastCall().andDelegateTo(headersAccumulator).once();
        });

    }

    public void requireExpectedRequestInteraction(HTTPClientRequest reqMock, HTTPClientRequest accumulator) {

        if (getExpectSetUri() != null) {
            reqMock.setURI(getExpectSetUri());
            expectLastCall().andDelegateTo(accumulator).once();
        }

        if (getExpectSetVerb() != null) {
            reqMock.setMethod(getExpectSetVerb());
            expectLastCall().andDelegateTo(accumulator).once();
        }
    }

    public void forEachAddedHeader(BiConsumer<String, String> c) {
        if (expectedAddedHeaders != null) {
            expectedAddedHeaders.forEach(c);
        }
    }

    public void forEachDroppedHeader(Consumer<String> c) {
        if (expectedDroppedHeaders != null) {
            expectedDroppedHeaders.forEach(c);
        }
    }

    public void applyExpectations(HTTPClientRequest reqMock) {
        final MutableHTTPHeaders hh = reqMock.getHeaders();

        Mocks.forEachNotNull(expectedDroppedHeaders, (h) -> {
            hh.remove(h);
            expectLastCall().once();
        });

        Mocks.forEachNotNull(expectedAddedHeaders, (k, v) -> {
            hh.set(k, v);
            expectLastCall().once();
        });

        if (expectSetVerb != null) {
            reqMock.setMethod(this.expectSetVerb);
            expectLastCall().once();
        }

        if (expectSetUri != null) {
            reqMock.setURI(expectSetUri);
            expectLastCall().once();
        }
    }

    public static APIOriginRequestModificationModel deepClone(APIOriginRequestModificationModel other) {
        if (other != null) {
            APIOriginRequestModificationModel retVal = new APIOriginRequestModificationModel();
            retVal.deepCopyFrom(other);
            return retVal;
        } else {
            return null;
        }
    }

    public void acceptVisitor(TestModelVisitor v) {
        v.visit(this);
    }

    public APIOriginRequestModificationModel inheritFrom(APIOriginRequestModificationModel other) {
        copyIfNullCollection(this::getExpectedDroppedHeaders, other::getExpectedDroppedHeaders, this::setExpectedDroppedHeaders, ArrayList::new);

        copyIfNullMap(this::getExpectedAddedHeaders, other::getExpectedAddedHeaders, this::setExpectedAddedHeaders, HashMap::new);

        copyIfNull(this::getExpectSetVerb, other::getExpectSetVerb, this::setExpectSetVerb);
        copyIfNull(this::getExpectSetUri, other::getExpectSetUri, this::setExpectSetUri);

        copyIfNull(this::getPayloadModel, other::getPayloadModel, (m) -> this.setPayloadModel(new PayloadSourceModel(m)));

        return this;
    }
}
