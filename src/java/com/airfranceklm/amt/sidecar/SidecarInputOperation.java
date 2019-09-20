package com.airfranceklm.amt.sidecar;

import java.util.Map;
import java.util.Objects;

public class SidecarInputOperation {
    private String httpVerb;
    private String path;
    private Map<String,String> query;
    private String uri;

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getQuery() {
        return query;
    }

    public void setQuery(Map<String, String> query) {
        this.query = query;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SidecarInputOperation that = (SidecarInputOperation) o;
        return Objects.equals(httpVerb, that.httpVerb) &&
                Objects.equals(path, that.path) &&
                Objects.equals(query, that.query) &&
                Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpVerb, path, query, uri);
    }

    @Override
    public String toString() {
        return String.format("{httpVerb=%s, path=%s, query=%s, uri=%s}",
                httpVerb,
                path,
                SidecarInput.mapToString(query),
                uri);
    }
}
