package com.airfranceklm.amt.sidecar;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SidecarOutput extends Serializable  {
    List<String> getDropHeaders();

    Map<String, String> getAddHeaders();

    String getPayload();

    JsonNode getJson();

    Integer getCode();

    Date getUnchangedUntil();

    SidecarOutputRouting getChangeRoute();

    boolean addsContentType();

    String getMessage();

    Map<String, Object> getRelayParameters();

    HashMap<String, Object> createSerializeableRelayParameters();

    boolean relaysMessageToPostprocessor();
}
