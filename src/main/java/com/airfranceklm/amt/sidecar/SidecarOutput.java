package com.airfranceklm.amt.sidecar;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SidecarOutput extends Serializable  {
    List<String> getDropHeaders();

    Map<String, String> getAddHeaders();

    String getPayload();

    Map<String,?> getJSONPayload();

    Integer getCode();

    Date getUnchangedUntil();

    SidecarOutputRouting getChangeRoute();

    boolean addsContentType();

    String getMessage();

    Map<String, ?> getRelayParams();

    HashMap<String, Object> createSerializeableRelayParameters();

    boolean relaysMessageToPostprocessor();
}
