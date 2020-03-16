package com.airfranceklm.amt.sidecar.elements.impl;

import com.airfranceklm.amt.sidecar.elements.*;
import com.airfranceklm.amt.sidecar.model.SidecarInputToken;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import com.mashery.trafficmanager.model.oauth.OAuthContext;

import java.util.function.Function;

import static com.airfranceklm.amt.sidecar.elements.DataElementSalience.FAIL_FAST;
import static com.airfranceklm.amt.sidecar.model.SidecarInput.Accessor.allocOrGetToken;

public class TokenElementsFactory {

    static final Function<ProcessorEvent, String> userContextLocator = (ppe) -> fromOAuth2Context(ppe, (ctx) -> ctx.getAccessToken().getUserToken());

    private static DataElement<ProcessorEvent, String> createGrantType(String param) {
        SimpleDataElement<ProcessorEvent, String> retVal = new SimpleDataElement<>(StringElements.TokenGrantType, FAIL_FAST);

        retVal.extractWith((ppe) -> fromOAuth2Context(ppe, (ctx) -> ctx.getAccessToken().getGrantType()));
        retVal.convertUsing((grantType, sid) -> allocOrGetToken(sid.getInput()).setGrantType(grantType));

        return retVal;
    }

    private static DataElement<ProcessorEvent, String> createScope(String param) {
        SimpleDataElement<ProcessorEvent, String> retVal = new SimpleDataElement<>(StringElements.TokenScope, FAIL_FAST);

        retVal.extractWith((ppe) -> fromOAuth2Context(ppe, (ctx) -> ctx.getAccessToken().getScope()));
        retVal.convertUsing((tokenScope, sid) -> {
            if (tokenScope != null) {
                allocOrGetToken(sid.getInput()).setScope(tokenScope);
            }
        });

        return retVal;
    }

    private static DataElement<ProcessorEvent, String> createUserContext(String param) {
        SimpleDataElement<ProcessorEvent, String> retVal = new SimpleDataElement<>(StringElements.TokenUserContext, FAIL_FAST);

        retVal.extractWith(userContextLocator);
        retVal.convertUsing((userCtx, sid) -> {
            if (userCtx != null) {
                allocOrGetToken(sid.getInput()).setUserContext(userCtx);
            }
        });

        return retVal;
    }

    private static DataElement<ProcessorEvent, SidecarInputToken> createToken(String param) {
        SimpleDataElement<ProcessorEvent, SidecarInputToken> retVal = new SimpleDataElement<>(ObjectElements.Token, FAIL_FAST);

        retVal.extractWith((ppe) -> fromOAuth2Context(ppe, TokenElementsFactory::buildInputToken));
        retVal.convertUsing((t, sid) -> sid.getInput().setToken(t));

        return retVal;
    }

    private static SidecarInputToken buildInputToken(OAuthContext ctx) {
        SidecarInputToken t = new SidecarInputToken();
        t.setUserContext(ctx.getAccessToken().getUserToken());
        t.setScope(ctx.getAccessToken().getScope());
        t.setGrantType(ctx.getAccessToken().getGrantType());
        t.setExpires(ctx.getAccessToken().getExpires());
        return t;
    }

    private static DataElement<ProcessorEvent, SidecarInputToken> createFullToken(String param) {
        SimpleDataElement<ProcessorEvent, SidecarInputToken> retVal = new SimpleDataElement<>(ObjectElements.FullToken, FAIL_FAST);

        retVal.extractWith((ppe) -> fromOAuth2Context(ppe, (ctx) -> {
            SidecarInputToken t = buildInputToken(ctx);
            t.setBearerToken(ctx.getAccessToken().getAccessToken());

            return t;
        }));

        retVal.convertUsing((t, sid) -> sid.getInput().setToken(t));

        return retVal;
    }

    static <T> T fromOAuth2Context(ProcessorEvent ppe, Function<OAuthContext, T> f) {
        if (ppe.getAuthorizationContext() instanceof OAuthContext) {
            OAuthContext ctx = (OAuthContext) ppe.getAuthorizationContext();
            return f.apply(ctx);
        } else {
            return null;
        }
    }

    public static void fill(ElementsFactoryBuilder b) {
        b.addCommonElement(StringElements.TokenGrantType, TokenElementsFactory::createGrantType);
        b.addCommonElement(StringElements.TokenScope, TokenElementsFactory::createScope);
        b.addCommonElement(StringElements.TokenUserContext, TokenElementsFactory::createUserContext);

        b.addCommonElement(ObjectElements.Token, TokenElementsFactory::createToken);
        b.addCommonElement(ObjectElements.FullToken, TokenElementsFactory::createFullToken);
    }

}
