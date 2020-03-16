package com.airfranceklm.amt.sidecar.model.json;

import com.airfranceklm.amt.sidecar.JsonHelper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.airfranceklm.amt.sidecar.CommonExpressions.allocOrGet;

/**
 * Abstract sidecar output implementation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonAbstractSidecarOutput {

    @Getter @Setter
    protected Date unchangedUntil;
    @Getter @Setter
    protected Integer unchangedFor;

    @Getter @Setter
    protected JsonSidecarOutputTerminationCommand terminate;

    public JsonAbstractSidecarOutput terminate(Consumer<JsonSidecarOutputTerminationCommand.JsonSidecarOutputTerminationCommandBuilder> cfg) {
        JsonSidecarOutputTerminationCommand.JsonSidecarOutputTerminationCommandBuilder builder =
                terminate != null ? terminate.asTerminateBuilder() : JsonSidecarOutputTerminationCommand.buildTerminate();
        cfg.accept(builder);
        terminate = builder.build();

        return this;
    }

    @SuppressWarnings("unchecked")
    static Map<String, ?> jsonToMap(JsonNode json) {
        if (json == null) {
            return null;
        }
        return (Map<String, ?>) JsonHelper.convertValue(json, Map.class);
    }

    /**
     * Converts the JSON node to string
     * @param json JSON node to convert
     * @return String representation of this JSON object.
     */
    static String jsonToString(JsonNode json) {
        if (json == null) {
            return null;
        }
        try {
            return JsonHelper.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    //------------------------------------------------------
    // Getters and setters

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonAbstractSidecarOutput that = (JsonAbstractSidecarOutput) o;
        return Objects.equals(unchangedUntil, that.unchangedUntil) &&
                Objects.equals(terminate, that.terminate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unchangedUntil, terminate);
    }

    public static JsonSidecarOutputTerminationCommand allocOrGetTerminate(JsonAbstractSidecarOutput t) {
        return allocOrGet(t::getTerminate, t::setTerminate, JsonSidecarOutputTerminationCommand::new);
    }
}
