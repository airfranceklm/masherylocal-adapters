package com.airfranceklm.amt.sidecar.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.airfranceklm.amt.sidecar.model.SidecarInput.*;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SidecarInputHTTPMessage {
    @Getter @Setter
    private Map<String, String> headers;
    @Getter @Setter
    private Map<String, Object> payloadFragments;
    @Getter @Setter
    private Long payloadLength;
    @Getter @Setter
    private String payload;
    @Getter @Setter
    private Boolean payloadBase64Encoded;

    public SidecarInputHTTPMessage() {
    }

    public SidecarInputHTTPMessage deepClone() {
        final SidecarInputHTTPMessageBuilder b = SidecarInputHTTPMessage.buildHTTPInputMessage()
                .payloadLength(payloadLength)
                .payloadBase64Encoded(payloadBase64Encoded);

        if (headers != null) b.headers(headers);
        if (payloadFragments != null) b.payloadFragments(payloadFragments);

        return b.build();
    }

    @Builder(builderMethodName = "buildHTTPInputMessage")
    public SidecarInputHTTPMessage(@Singular Map<String, String> headers
            , @Singular Map<String, Object> payloadFragments
            , Long payloadLength
            , String payload
            , Boolean payloadBase64Encoded) {
        this.headers = headers;
        this.payloadFragments = payloadFragments;
        this.payloadLength = payloadLength;
        this.payload = payload;
        this.payloadBase64Encoded = payloadBase64Encoded;
    }

    boolean containsOnlyNulls() {
        return (headers == null || headers.size() == 0)
                && payload == null;
    }

    void updateChecksum(MessageDigest md) {
        updateChecksumOfMap(md, "reqHeaders", headers);
        if (payload != null) {
            updateRedirect(md);
            md.update(utf8Of(payload));
        }
    }

    public void addHeader(String h, String val) {
        if (this.headers == null) {
            this.headers = new HashMap<>();
        }
        this.headers.put(h, val);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SidecarInputHTTPMessage that = (SidecarInputHTTPMessage) o;
        return Objects.equals(payloadLength, that.payloadLength) &&
                equalNullOrEmptyMap(headers, that.headers) &&
                Objects.equals(payload, that.payload) &&
                equalNullOrEmptyMap(payloadFragments, that.payloadFragments) &&
                Objects.equals(payloadBase64Encoded, that.payloadBase64Encoded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers, payloadFragments, payloadLength, payload, payloadBase64Encoded);
    }

    @Override
    public String toString() {
        return "SidecarInputHTTPMessage{" +
                "headers=" + mapToString(headers) +
                ", payloadLength=" + payloadLength +
                ", payload='" + payload + '\'' +
                ", payloadFragments='" + mapToString(payloadFragments) + '\'' +
                ", base64Encoded='" + payloadBase64Encoded + '\'' +
                '}';
    }
}
