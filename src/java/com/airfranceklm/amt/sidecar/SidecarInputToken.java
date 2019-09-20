package com.airfranceklm.amt.sidecar;

import java.util.Date;
import java.util.Objects;

public class SidecarInputToken {
    String scope;
    String userContext;
    Date expires;
    String grantType;

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

    @Override
    public String toString() {
        return "LambdaSidecarInputToken{" +
                "scope='" + scope + '\'' +
                ", userContext='" + userContext + '\'' +
                ", expires=" + expires +
                ", grantType='" + grantType + '\'' +
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
                Objects.equals(grantType, that.grantType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope, userContext, expires, grantType);
    }
}
