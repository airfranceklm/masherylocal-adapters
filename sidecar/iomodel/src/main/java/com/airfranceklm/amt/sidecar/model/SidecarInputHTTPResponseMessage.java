package com.airfranceklm.amt.sidecar.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;

import java.security.MessageDigest;
import java.util.Map;
import java.util.Objects;

import static com.airfranceklm.amt.sidecar.model.SidecarInput.stdOf;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SidecarInputHTTPResponseMessage extends SidecarInputHTTPMessage {

    @Getter @Setter
    private Integer statusCode;

    public SidecarInputHTTPResponseMessage() {
        super();
    }

    @Builder(builderMethodName = "buildHTTPResponseMessage")
    public SidecarInputHTTPResponseMessage(@Singular  Map<String, String> headers
            , @Singular Map<String, Object> payloadFragments
            , Long payloadLength
            , String payload
            , Boolean payloadBase64Encoded
            , Integer statusCode) {
        super(headers, payloadFragments, payloadLength, payload, payloadBase64Encoded);
        this.statusCode = statusCode;
    }

    public SidecarInputHTTPResponseMessage deepClone() {
        final SidecarInputHTTPResponseMessageBuilder b = SidecarInputHTTPResponseMessage
                .buildHTTPResponseMessage()
                .payload(getPayload())
                .payloadLength(getPayloadLength())
                .payloadBase64Encoded(getPayloadBase64Encoded())
                .statusCode(getStatusCode());

        if (getHeaders() != null) b.headers(getHeaders());
        if (getPayloadFragments() != null) b.payloadFragments(getPayloadFragments());

        return b.build();
    }

    SidecarInputHTTPResponseMessage(int code) {
        super();
        this.statusCode = code;
    }

    /**
     * Backward compatiblity with Mashery's Phase-1 specification.
     * @deprecated
     * @param code code
     */
    @Deprecated
    public void setResponseCode(int code) {
        setStatusCode(code);
    }

    void updateChecksum(MessageDigest md) {
        super.updateChecksum(md);
        md.update(stdOf(String.valueOf(statusCode)));
    }

    @Override
    public String toString() {
        return "SidecarResponseHTTPMessage{" +
                "code=" + statusCode +
                ", headers=" + SidecarInput.mapToString(getHeaders()) +
                ", payloadLength=" + getPayloadLength() +
                ", payload='" + getPayload() + '\'' +
                ", base64Encoded='" + getPayloadBase64Encoded() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SidecarInputHTTPResponseMessage that = (SidecarInputHTTPResponseMessage) o;

        return super.equals(o) &&
            Objects.equals(statusCode, that.statusCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), statusCode);
    }
}
