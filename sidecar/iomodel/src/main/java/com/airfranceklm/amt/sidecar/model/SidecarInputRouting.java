package com.airfranceklm.amt.sidecar.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.security.MessageDigest;
import java.util.Objects;

import static com.airfranceklm.amt.sidecar.model.SidecarInput.updateNullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true, builderMethodName = "buildInputRouting")
public class SidecarInputRouting {
    @Getter @Setter
    private String httpVerb;
    @Getter @Setter
    private String uri;

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
