package com.airfranceklm.amt.sidecar;

import java.util.Objects;

/**
 * Indication of the change to the routing.
 */
public class SidecarOutputRouting {
    private String host;
    private String file;
    private String httpVerb;
    private String uri;
    private Integer port;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }


    /**
     * Returns true if routing seeks ot override host, file, or port of the outgoing URI.
     * @return true if override is full or partial, false if the output URI doesn't need to change.
     */
    boolean outboundURINeedsChanging() {
        return this.host != null || this.file != null || this.port > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SidecarOutputRouting that = (SidecarOutputRouting) o;
        return Objects.equals(host, that.host) &&
                Objects.equals(file, that.file) &&
                Objects.equals(httpVerb, that.httpVerb) &&
                Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, file, httpVerb, uri);
    }

    public boolean containsOnlyNulls() {
        return host == null && file == null && httpVerb == null && uri == null && port == null;
    }
}
