package com.airfranceklm.amt.testsupport;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.Map;

@NoArgsConstructor
public class HttpResponseMessageData extends HttpMessageData {

    @JsonProperty("status code") @Getter
    @Setter
    protected Integer statusCode;

    @JsonProperty("status message") @Getter @Setter
    protected String statusMessage;

    public HttpResponseMessageData(long payloadLength, String payload, String base64BinaryPayload, byte[] binaryPayload, Class<?> payloadOwner, String payloadResource, Map<String, String> headers, boolean reproducible, String contentAccessException, String version, Integer statusCode, String statusMessage) {
        super(payloadLength, payload, base64BinaryPayload, binaryPayload, payloadOwner, payloadResource, headers, reproducible, contentAccessException, version);
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public void deepCopyFrom(@NonNull HttpResponseMessageData another) {
        super.deepCopyFrom(another);

        this.statusCode = another.statusCode;
        this.statusMessage = another.statusMessage;
    }
}
