package com.airfranceklm.amt.sidecar;

import java.security.MessageDigest;
import java.util.Objects;

import static com.airfranceklm.amt.sidecar.SidecarInput.updateNullable;

public class SidecarInputRouting {
    private String httpVerb;
    private String uri;

    public String getHttpVerb() {
        return httpVerb;
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

    public void updateChecksum(MessageDigest md) {
        updateNullable(md, httpVerb);
        updateNullable(md, uri);
    }

    @Override
    public String toString() {
        return "LambdaSidecarInputRouting{" +
                "httpVerb='" + httpVerb + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SidecarInputRouting that = (SidecarInputRouting) o;
        return Objects.equals(httpVerb, that.httpVerb) &&
                Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpVerb, uri);
    }
}
