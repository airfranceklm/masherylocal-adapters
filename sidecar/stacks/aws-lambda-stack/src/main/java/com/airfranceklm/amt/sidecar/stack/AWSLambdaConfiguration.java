package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.identity.PartyKeyDescriptor;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;

import java.util.Map;
import java.util.function.Consumer;

import static java.lang.Boolean.parseBoolean;

@AllArgsConstructor
@Builder
@Slf4j
public class AWSLambdaConfiguration implements SidecarStackConfiguration {

    public static final String CFG_FUNCTION_ARN = "functionARN";
    public static final String CFG_ASSUME_ROLE = "assumeRole";
    public static final String CFG_AWS_KEY = "key";
    public static final String CFG_AWS_SECRET = "secret";
    public static final String CFG_AWS_KEYREF = "keyRef";
    public static final String CFG_AWS_REGION = "region";
    public static final String CFG_PROXY_HOST = "proxyHost";
    public static final String CFG_PROXY_PORT = "proxyPort";
    public static final String CFG_HTTP_PROTO = "supportHttp";
    public static final String CFG_HTTPS_PROTO = "supportHttps";

    @Getter @Setter(AccessLevel.PROTECTED)
    String functionARN;
    @Getter  @Setter(AccessLevel.PROTECTED)
    Region awsRegion;

    @Getter @Setter(AccessLevel.PROTECTED)
    String awsKey;
    @Getter @Setter(AccessLevel.PROTECTED)
    String awsSecret;
    @Getter @Setter(AccessLevel.PROTECTED)
    String awsKeyRef;
    @Getter @Setter(AccessLevel.PROTECTED)
    String assumeAwsRole;

    @Getter @Setter
    PartyKeyDescriptor awsClusterIdentity;

    @Getter @Setter
    String assumeAwsRoleExternalId;

    @Getter
    long timeout;

    @Getter
    int proxyPort;
    @Getter @Setter(AccessLevel.PROTECTED)
    String proxyHost;
    @Getter
    boolean supportProxyHttp;
    @Getter
    boolean supportProxyHttps;

    protected AWSLambdaConfiguration() {
        this.proxyPort = -1;
        this.timeout = 3000L;
        this.supportProxyHttp = true;
        this.supportProxyHttps = false;
    }

    public AWSLambdaConfiguration(Map<String, String> params, long timeout) {
        this(params, timeout, null);
    }

    public AWSLambdaConfiguration(Map<String, String> params, long timeout, AWSLambdaConfiguration defaults) {
        this();
        init(params, timeout, defaults);
    }

    protected void init(Map<String, String> params, long timeout, AWSLambdaConfiguration defaults) {
        if (defaults != null) {
            inheritFrom(defaults);
        }

        this.timeout = timeout > 100 ? timeout : 100;

        if (params != null) {
            setNonNullValue(CFG_FUNCTION_ARN, params, this::setFunctionARN);
            setNonNullValue(CFG_AWS_KEY, params, this::setAwsKey);
            setNonNullValue(CFG_AWS_SECRET, params, this::setAwsSecret);
            setNonNullValue(CFG_AWS_KEYREF, params, this::setAwsKeyRef);
            setNonNullValue(CFG_AWS_REGION, params, (v) -> setAwsRegion(Region.of(v)));
            setNonNullValue(CFG_ASSUME_ROLE, params, this::setAssumeAwsRole);


            setNonNullValue(CFG_PROXY_HOST, params, this::setProxyHost);

            setNonNullValue(CFG_PROXY_PORT, params, (v) -> {
                try {
                    this.proxyPort = Integer.parseInt(params.get(CFG_PROXY_PORT));
                } catch (NumberFormatException ex) {
                    this.proxyPort = 8080;
                    log(String.format("Not a number for proxy port: %s", v));
                }
            });

            setNonNullValue(CFG_HTTP_PROTO, params, (v) -> supportProxyHttp = parseBoolean(v));
            setNonNullValue(CFG_HTTPS_PROTO, params, (v) -> supportProxyHttps = parseBoolean(v));
        }
    }

    protected void log(String v) {
        log.error(v);
    }

    private void setNonNullValue(String key, Map<String, String> params, Consumer<String> setter) {
        String v = params.get(key);
        if (v != null) {
            setter.accept(v);
        }
    }

    private void inheritFrom(AWSLambdaConfiguration other) {
        this.functionARN = other.functionARN;
        this.awsRegion = other.awsRegion;
        this.awsKey = other.awsKey;
        this.awsSecret = other.awsSecret;
        this.assumeAwsRole = other.assumeAwsRole;
        this.awsClusterIdentity = other.awsClusterIdentity;
        this.assumeAwsRoleExternalId = other.assumeAwsRoleExternalId;
        this.timeout = other.timeout;
        this.proxyPort = other.proxyPort;
        this.proxyHost = other.proxyHost;
        this.supportProxyHttp = other.supportProxyHttp;
        this.supportProxyHttps = other.supportProxyHttps;
    }

    boolean requiresProxy() {
        return proxyHost != null;
    }

    boolean definesProxyPort() {
        return proxyPort > 0;
    }

    int effectiveProxyPort() {
        return proxyPort > 0 ? proxyPort : 8080;
    }

    public boolean isValid() {
        final boolean tSpecifiedAwsId = specifiesAwsIdentity();
        final boolean tAssumesRole = assumesRole();
        return functionARN != null                      // Function ARN number be supplied
                && (tSpecifiedAwsId || tAssumesRole)    // Credentials in either form must be supplied
                && !(tSpecifiedAwsId && tAssumesRole);  // But only one option for credentials shall be present
    }

    public boolean assumesRole() {
        return this.assumeAwsRole != null;
    }

    public boolean specifiesAwsIdentity() {
        return this.awsKey != null && this.awsSecret != null;
    }

}
