package com.airfranceklm.amt.testsupport.dsl;

import com.airfranceklm.amt.testsupport.AuthorizationContextData;

import java.util.Date;

public class AuthorizationContextDataDSL {
    private AuthorizationContextData data;

    public AuthorizationContextDataDSL(AuthorizationContextData data) {
        this.data = data;
    }

    public AuthorizationContextDataDSL withScope(String scope) {
        this.data.setScope(scope);
        return this;
    }

    public AuthorizationContextDataDSL withUserContext(String userContext) {
        this.data.setUserContext(userContext);
        return this;
    }

    public AuthorizationContextDataDSL withGrantType(String grantType) {
        this.data.setGrantType(grantType);
        return this;
    }

    public AuthorizationContextDataDSL withExpiryTime(Date expiry) {
        this.data.setExpires(expiry);
        return this;
    }
}
