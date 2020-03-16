package com.airfranceklm.amt.sidecar.dsl;

import java.util.Map;

public interface PayloadCreatorDSL {

    PayloadCreatorDSL passPayload(String payload, boolean encoded);

    PayloadCreatorDSL passJsonData(Map<String, ?> data);

    PayloadCreatorDSL passHeader(String s, String v);
}
