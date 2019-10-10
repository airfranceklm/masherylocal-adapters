package com.airfranceklm.amt.sidecar.model;

import java.security.MessageDigest;
import java.util.Date;
import java.util.Objects;

import static com.airfranceklm.amt.sidecar.model.SidecarInput.*;

public class SidecarInputToken {
    private String bearerToken;
    private String scope;
    private String userContext;
    private Date expires;
    private String grantType;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getUserContext() {
        return userContext;
    }

    public void setUserContext(String userContext) {
        this.userContext = userContext;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

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
