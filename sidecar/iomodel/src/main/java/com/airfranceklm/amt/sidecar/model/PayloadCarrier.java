package com.airfranceklm.amt.sidecar.model;

import java.util.Map;

public interface PayloadCarrier {
    /**
     * Specifies the payload to use instead of the communicated.
     * @return String containing the payload
     */
    String getPayload();

    /**
     * Indication if the {@link #getPayload()} is Base64-encoded, and thus if the payload needs to be
     * Base-64 decoded before being transmitted ot the destinations
     * @return set value; null if was omitte din the original response.
     */
    Boolean getBase64Encoded();

    /**
     * Returns a JSON-type payload
     * @return JSON map, if supplied, otherwise returns <code>null</code>.
     */
    Map<String,?> getJsonPayload();

    /**
     * Specifies the headers to add
     * @return Map with headers, or null if not required.
     */
    Map<String,String> getPassHeaders();

    /**
     * Checks whether the command adds the content type.
     * @return true if content-type is passed, false otherwise
     */
    default boolean addsContentType() {
        return getPassHeaders() != null
                && (getPassHeaders().containsKey("content-type")
                || getPassHeaders().containsKey("Content-Type"));
                // TODO: Default implementation is case-sensitive after Jackson
                // unmarshalling. To be investigated and fixed.
    }
}
