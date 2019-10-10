package com.airfranceklm.amt.sidecar.impl.model;

import com.airfranceklm.amt.sidecar.JsonHelper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Date;
import java.util.Map;

/**
 * Abstract sidecar output implementation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbstractSidecarOutputImpl {

    private Date unchangedUntil;
    private SidecarOutputTerminationCommandImpl terminate;

    @SuppressWarnings("unchecked")
    static Map<String, ?> jsonToMap(JsonNode json) {
        if (json == null) {
            return null;
        }
        return (Map<String, ?>) JsonHelper.objectMapper.convertValue(json, Map.class);
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
            return JsonHelper.objectMapper.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    //------------------------------------------------------
    // Getters and setters

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    public Date getUnchangedUntil() {
        return unchangedUntil;
    }

    public void setUnchangedUntil(Date unchangedUntil) {
        this.unchangedUntil = unchangedUntil;
    }

    public SidecarOutputTerminationCommandImpl getTerminate() {
        return terminate;
    }

    public void setTerminate(SidecarOutputTerminationCommandImpl terminate) {
        this.terminate = terminate;
    }

    public boolean terminates() {
        return false;
    }
}
