package com.airfranceklm.amt.testsupport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mashery.trafficmanager.model.auth.AuthorizationType;
import com.mashery.trafficmanager.model.oauth.AccessToken;
import com.mashery.trafficmanager.model.oauth.OAuthContext;
import com.mashery.trafficmanager.model.oauth.TokenType;
import lombok.*;
import org.easymock.EasyMockSupport;

import java.util.Date;

import static com.airfranceklm.amt.testsupport.Mocks.copyIfNull;
import static org.easymock.EasyMock.expect;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true, builderMethodName = "authorizationContext")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MasheryAuthorizationContextModel extends RequestCaseDatum {
    @Builder.Default
    @Getter @Setter
    String tokenType = "bearer";

    @JsonProperty("scope") @Getter @Setter
    String scope;
    // TODO: add support for JSON

    @JsonProperty("user context") @Getter @Setter
    String userContext;
    // TODO: add suport for JSON

    @JsonProperty("grant type") @Getter @Setter
    String grantType;
    @JsonProperty("expires") @Getter @Setter
    Date expires;

    public MasheryAuthorizationContextModel(@NonNull  MasheryAuthorizationContextModel another) {
        this();
        deepCopyFrom(another);
    }

    public void deepCopyFrom(@NonNull MasheryAuthorizationContextModel another) {
        this.tokenType = another.tokenType;
        this.scope = another.scope;
        this.userContext = another.userContext;
        this.grantType = another.grantType;
        this.expires = another.expires;
    }

    public OAuthContext mock(EasyMockSupport owner) {
        return mock(owner, null);
    }

    public OAuthContext mock(EasyMockSupport owner, MasheryPackageKeyModel pkm) {
        String accessToken = "unit-test-token";

        OAuthContext retVal = owner.createMock(OAuthContext.class);
        expect(retVal.getType()).andReturn(AuthorizationType.OAUTH_2).anyTimes();

        expect(retVal.getTokenType()).andReturn(tokenType).anyTimes();
        expect(retVal.getToken()).andReturn(accessToken).anyTimes();

        AccessToken accessTokenMock = owner.createMock(AccessToken.class);
        expect(accessTokenMock.getAccessToken()).andReturn(accessToken).anyTimes();

        // This is also hard-coded since we aren't using anything els.e
        expect(accessTokenMock.getAccessTokenType()).andReturn(TokenType.BEARER).anyTimes();

        // The expiry date of the token is always set some time in the future.
        expect(accessTokenMock.getExpires()).andReturn(expires).anyTimes();
        expect(accessTokenMock.getScope()).andReturn(scope).anyTimes();
        expect(accessTokenMock.getUserToken()).andReturn(userContext).anyTimes();
        expect(accessTokenMock.getGrantType()).andReturn(grantType).anyTimes();

        String clId = pkm != null ? pkm.getPackageKey() : null;
        expect(accessTokenMock.getClientID()).andReturn(clId).anyTimes();

        expect(retVal.getAccessToken()).andReturn(accessTokenMock).anyTimes();

        return retVal;
    }

    public void acceptVisitor(@NonNull TestModelVisitor v) {
        v.visit(this);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof MasheryAuthorizationContextModel;
    }

    public void inheritFrom(MasheryAuthorizationContextModel another) {
        copyIfNull(this::getScope, another::getScope, this::setScope);
        copyIfNull(this::getUserContext, another::getUserContext, this::setUserContext);
        copyIfNull(this::getGrantType, another::getGrantType, this::setGrantType);
        copyIfNull(this::getExpires, another::getExpires, this::setExpires);
        copyIfNull(this::getTokenType, another::getTokenType, this::setTokenType);
    }
}
