package com.airfranceklm.amt.sidecar.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.security.MessageDigest;
import java.util.Date;
import java.util.Objects;

import static com.airfranceklm.amt.sidecar.model.SidecarInput.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder=true, builderMethodName = "buildToken")
public class SidecarInputToken {
    @Getter @Setter
    private String bearerToken;
    @Getter @Setter
    private String scope;
    @Getter @Setter
    private String userContext;
    @Getter @Setter
    private Date expires;
    @Getter @Setter
    private String grantType;

    void updateChecksum(MessageDigest md) {
        updateNullable(md, grantType);
        updateUTF8Nullable(md, scope);
        updateUTF8Nullable(md, userContext);
        if (expires != null) {
            md.update(stdOf(String.valueOf(expires.getTime())));
            updateRedirect(md);
        }
    }

    @Override
    public String toString() {
        return "LambdaSidecarInputToken{" +
                "scope='" + scope + '\'' +
                ", userContext='" + userContext + '\'' +
                ", expires=" + expires +
                ", grantType='" + grantType + '\'' +
                ", bearer='" + bearerToken + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SidecarInputToken that = (SidecarInputToken) o;
        return Objects.equals(scope, that.scope) &&
                Objects.equals(userContext, that.userContext) &&
                Objects.equals(expires, that.expires) &&
                Objects.equals(grantType, that.grantType) &&
                Objects.equals(bearerToken, that.bearerToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope, userContext, expires, grantType, bearerToken);
    }
}
