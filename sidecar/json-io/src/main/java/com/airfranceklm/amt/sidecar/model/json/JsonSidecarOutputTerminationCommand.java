package com.airfranceklm.amt.sidecar.model.json;

import com.airfranceklm.amt.sidecar.model.TerminateCommand;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class JsonSidecarOutputTerminationCommand extends JsonReplyCommand implements TerminateCommand {

    @Getter
    @Setter
    protected String message;

    @Builder(builderMethodName = "buildTerminate")
    public JsonSidecarOutputTerminationCommand(@Singular Map<String, String> passHeaders
            , Map<String, ?> jsonPayload
            , String payload
            , JsonNode json
            , Boolean base64Encoded
            , Integer statusCode
            , String message) {

        super(asTreeMap(passHeaders), jsonPayload, payload, json, base64Encoded, statusCode);
        this.message = message;
    }

    public JsonSidecarOutputTerminationCommand.JsonSidecarOutputTerminationCommandBuilder asTerminateBuilder() {
        JsonSidecarOutputTerminationCommand.JsonSidecarOutputTerminationCommandBuilder b = buildTerminate();

        b.message(message)
                .statusCode(getStatusCode())
                .passHeaders(getPassHeaders())
                .jsonPayload(getJsonPayload())
                .payload(getPayload())
                .json(getJson())
                .base64Encoded(getBase64Encoded());

        return b;
    }

    public JsonSidecarOutputTerminationCommand(Integer statusCode, String message) {
        this.message = message;
        this.setStatusCode(statusCode);
    }

    public JsonSidecarOutputTerminationCommand with(Consumer<JsonSidecarOutputTerminationCommand> c) {
        c.accept(this);
        return this;
    }

    public <T> JsonSidecarOutputTerminationCommand with(T value, BiConsumer<JsonSidecarOutputTerminationCommand, T> c) {
        c.accept(this, value);
        return this;
    }

    public boolean containsOnlyNulls() {
        return super.containsOnlyNulls() &&
                this.message == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonSidecarOutputTerminationCommand that = (JsonSidecarOutputTerminationCommand) o;
        return super.equals(o) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), message);
    }
}
