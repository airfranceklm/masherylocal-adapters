package com.airfranceklm.amt.sidecar.model;

public interface RequestRoutingChangeBean {
    Integer getPort();

    String getHttpVerb();

    String getHost();

    String getFile();

    String getUri();

    String getFileBase();

    String getProtocol();

    boolean outboundURINeedsChanging();

    boolean outboundHostChanged();
}
