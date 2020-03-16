package com.airfranceklm.amt.testsupport;

import com.mashery.trafficmanager.model.oauth.OAuthContext;
import com.mashery.trafficmanager.model.oauth.TokenType;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static com.airfranceklm.amt.testsupport.MasheryAuthorizationContextModel.authorizationContext;
import static com.airfranceklm.amt.testsupport.MasheryPackageKeyModel.packageKey;
import static com.airfranceklm.amt.testsupport.Mocks.assertAllEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class MasheryAuthorizationContextModelTest extends EasyMockSupport {
    Date expires;
    MasheryAuthorizationContextModel mdl;

    @Before
    public void setup() {
        expires = new Date();

        mdl = authorizationContext()
                .expires(expires)
                .grantType("password")
                .scope("a b c")
                .userContext("user-context+permission")
                .tokenType("bearer")
                .build();
    }

    @Test
    public void testCreatingMock() {
        final Date expires = new Date();

        MasheryAuthorizationContextModel mdl = authorizationContext()
                .expires(expires)
                .grantType("password")
                .scope("a b c")
                .userContext("user-context+permission")
                .tokenType("bearer")
                .build();

        MasheryPackageKeyModel pkm = packageKey()
                .packageKey("r98jdmdk44gfs4")
                .build();

        OAuthContext ctx = mdl.mock(this, pkm);
        replayAll();
        assertNotNull(ctx);

        assertEquals("unit-test-token", ctx.getToken());
        assertEquals("bearer", ctx.getTokenType());
        // TODO Is this the correct value -> check in Mashery.

        assertNotNull(ctx.getAccessToken());
        assertAllEquals(expires, ctx.getAccessToken().getExpires(), mdl.getExpires());
        assertAllEquals("password", ctx.getAccessToken().getGrantType(), mdl.getGrantType());
        assertAllEquals("a b c", ctx.getAccessToken().getScope(), mdl.getScope());
        assertAllEquals("a b c", ctx.getAccessToken().getScope(), mdl.getScope());
        assertAllEquals("user-context+permission", ctx.getAccessToken().getUserToken(), mdl.getUserContext());


        assertEquals("r98jdmdk44gfs4", ctx.getAccessToken().getClientID());
        assertEquals(TokenType.BEARER, ctx.getAccessToken().getAccessTokenType());

        verifyAll();
    }

    @Test
    public void testDeepCopy() {
        MasheryAuthorizationContextModel n1 = new MasheryAuthorizationContextModel();
        assertThat(n1, is(not(mdl)));

        MasheryAuthorizationContextModel n2 = new MasheryAuthorizationContextModel(mdl);
        assertEquals(n2, mdl);
    }

}
