package com.airfranceklm.amt.testsupport;

import com.airfranceklm.amt.sidecar.AFKLMSidecarProcessor;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.forDefinedString;
import static org.junit.Assert.fail;

public class AuthorizationContextData extends RequestCaseDatum {
    String scope;
    String userContext;
    String grantType;
    Date expires;

    AuthorizationContextData() {
    }

    void copyFrom(AuthorizationContextData another) {
        if (this.scope == null) {
            this.scope = another.scope;
        }

        if (this.userContext == null) {
            this.userContext = another.userContext;
        }

        if (this.grantType == null) {
            this.grantType = another.grantType;
        }

        if (this.expires != null) {
            this.expires = another.expires;
        }
    }

    @Override
    void buildFromYAML(Map<String, Object> yaml) {
        super.buildFromYAML(yaml);

        forDefinedString(yaml, "scope", this::setScope);
        forDefinedString(yaml, "user context", this::setUserContext);
        forDefinedString(yaml, "grant type", this::setGrantType);
        forDefinedString(yaml, "expires", (v) -> {
            try {
                this.expires = AFKLMSidecarProcessor.jsonDate.parse(v);
            } catch (ParseException ex) {
                final String format = String.format("Malformed JSON date %s", v);
                fail(format);
            }
        });
    }

    AuthorizationContextData(Map<String, Object> yaml) {
        this();
        buildFromYAML(yaml);
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setUserContext(String userContext) {
        this.userContext = userContext;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }
}
