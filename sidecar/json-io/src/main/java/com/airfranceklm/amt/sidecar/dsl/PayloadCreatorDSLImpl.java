package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.model.json.JsonPayloadCarrier;
import lombok.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.airfranceklm.amt.sidecar.CommonExpressions.allocOrGet;

class PayloadCreatorDSLImpl<T extends JsonPayloadCarrier> implements PayloadCreatorDSL {
    T target;

    public PayloadCreatorDSLImpl(@NonNull T target) {
        this.target = target;
    }

    @Override
    public PayloadCreatorDSL passPayload(String payload, boolean encoded) {
        target.setPayload(payload);
        target.setBase64Encoded(encoded);
        return this;
    }

    @Override
    public PayloadCreatorDSL passJsonData(Map<String, ?> data) {
        target.setJsonPayload(data);
        return this;
    }

    @Override
    public PayloadCreatorDSL passHeader(String s, String v) {
        JsonPayloadCarrier.allocOrGetPassHeaders(target).put(s, v);
        return this;
    }
}
