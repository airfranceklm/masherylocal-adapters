package com.airfranceklm.amt.sidecar;

import java.security.MessageDigest;
import java.util.Map;
import java.util.Objects;

import static com.airfranceklm.amt.sidecar.SidecarInput.updateChecksumOfMap;
import static com.airfranceklm.amt.sidecar.SidecarInput.updateNullable;

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

    void updateChecksum(MessageDigest md) {
        updateNullable(md, httpVerb);
        updateNullable(md, uri);
        updateNullable(md, path);

        updateChecksumOfMap(md, "query", query);
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
