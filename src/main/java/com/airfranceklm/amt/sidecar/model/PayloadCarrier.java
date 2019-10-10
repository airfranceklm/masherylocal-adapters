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
     * @return
     */
    Map<String,?> getJSONPayload();
}
