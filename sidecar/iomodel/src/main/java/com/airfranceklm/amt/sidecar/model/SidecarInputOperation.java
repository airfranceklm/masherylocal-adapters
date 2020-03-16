package com.airfranceklm.amt.sidecar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.security.MessageDigest;
import java.util.Map;
import java.util.Objects;

import static com.airfranceklm.amt.sidecar.model.SidecarInput.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true, builderMethodName = "buildOperation")
public class SidecarInputOperation {
    @Getter @Setter
    private String httpVerb;
    @Getter @Setter
    private String path;
    @Getter @Setter
    private Map<String,String> query;
    @Getter @Setter
    private String uri;

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
                equalNullOrEmptyMap(query, that.query) &&
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
