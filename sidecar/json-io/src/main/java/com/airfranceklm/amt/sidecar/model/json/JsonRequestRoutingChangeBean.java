package com.airfranceklm.amt.sidecar.model.json;

import com.airfranceklm.amt.sidecar.model.RequestRoutingChangeBean;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Objects;

/**
 * Indication of the change to the routing.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true, builderMethodName = "buildChangeRoute")
public class JsonRequestRoutingChangeBean implements RequestRoutingChangeBean {
    @Getter @Setter
    protected String protocol;
    @Getter @Setter
    protected String host;
    @Getter @Setter
    protected String file;
    @Getter @Setter
    protected String fileBase;
    @Getter @Setter
    protected String httpVerb;
    @Getter @Setter
    protected String uri;
    @Getter @Setter
    protected Integer port;

    public static JsonRequestRoutingChangeBean routeToHost(String host) {
        JsonRequestRoutingChangeBean retVal = new JsonRequestRoutingChangeBean();
        retVal.setHost(host);
        return retVal;
    }

    /**
     * Returns true if routing seeks ot override host, file, or port of the outgoing URI.
     * @return true if override is full or partial, false if the output URI doesn't need to change.
     */
    public boolean outboundURINeedsChanging() {
        return this.protocol != null || this.host != null || this.file != null || this.port > 0;
    }

    @Override
    public boolean outboundHostChanged() {
        return host != null || port != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRequestRoutingChangeBean that = (JsonRequestRoutingChangeBean) o;
        return Objects.equals(protocol, that.protocol) &&
                Objects.equals(host, that.host) &&
                Objects.equals(file, that.file) &&
                Objects.equals(httpVerb, that.httpVerb) &&
                Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, file, httpVerb, uri, protocol);
    }

    public boolean containsOnlyNulls() {
        return host == null && file == null && httpVerb == null && uri == null && port == null && protocol == null;
    }
}
