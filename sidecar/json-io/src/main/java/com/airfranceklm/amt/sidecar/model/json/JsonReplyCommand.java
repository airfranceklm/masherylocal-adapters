package com.airfranceklm.amt.sidecar.model.json;

import com.airfranceklm.amt.sidecar.model.ReplyCommand;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class JsonReplyCommand extends JsonPayloadCarrier implements ReplyCommand {

    @Getter @Setter
    protected Integer statusCode;

    @Builder(builderMethodName = "buildReply")
    public JsonReplyCommand(@Singular Map<String, String> passHeaders
            , Map<String, ?> jsonPayload
            , String payload
            , JsonNode json
            , Boolean base64Encoded
            , Integer statusCode) {
        super(asTreeMap(passHeaders), jsonPayload, payload, json, base64Encoded);
        this.statusCode = statusCode;
    }

    public JsonReplyCommand.JsonReplyCommandBuilder asReplyBuilder() {
        JsonReplyCommand.JsonReplyCommandBuilder b = buildReply();

        b.statusCode(statusCode)
                .passHeaders(getPassHeaders())
                .jsonPayload(getJsonPayload())
                .payload(getPayload())
                .json(getJson())
                .base64Encoded(getBase64Encoded());

        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        JsonReplyCommand that = (JsonReplyCommand) o;
        return Objects.equals(this.getStatusCode(), that.getStatusCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getStatusCode());
    }

    /**
     * Used as a setter for backward compatibilty with Mashery Phase-1 dialect.
     * Use {@link #setStatusCode(Integer)} instead.
     * @param code code to use.
     */
    @Deprecated
    public void setCode(int code) {
        setStatusCode(code);
    }
}
