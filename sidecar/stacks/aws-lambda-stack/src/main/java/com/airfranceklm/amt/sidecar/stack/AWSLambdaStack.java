package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.identity.BasicCredentialsDescriptor;
import com.airfranceklm.amt.sidecar.identity.KeyType;
import com.airfranceklm.amt.sidecar.identity.PartyKeyDescriptor;
import com.airfranceklm.amt.sidecar.identity.ProcessorKeySet;
import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarSynchronicity;
import com.airfranceklm.amt.sidecar.model.alcp.EncryptedMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.ProxyConfiguration;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;
import software.amazon.awssdk.services.lambda.LambdaAsyncClientBuilder;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.ServiceException;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.StsClientBuilder;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.airfranceklm.amt.sidecar.CommonExpressions.splitStandardValueList;

/**
 * AWS invoker; essentially allowing unused client to be reaped.
 */
@Slf4j
public class AWSLambdaStack extends ALCPEnabledStack {

    private static Pattern functionARNPattern = Pattern.compile("arn:aws:lambda:([a-z0-9-]{3,}):(\\d{5,}):function:(.{3,})");

    public static final KeyType KEY_TYPE_AWS_WHITELISTED_ACCNTS = new KeyType("#awsStack.whitelisted.accounts");
    public static final KeyType KEY_TYPE_AWS_IDENTITY = new KeyType("#awsStack.assumeRole.identity");
    public static final KeyType KEY_TYPE_AWS_EXTERNAL_ID = new KeyType("#awsStack.account.externalId");
    public static final KeyType KEY_TYPE_AWS_REFERENCED_ID = new KeyType("#awsStack.reference.identity");

    public static final String STACK_NAME = "aws-lambda";

    @Getter @Setter
    private AWSLambdaConfiguration configurationDefaults;

    private Map<String, AWSLambdaHolder> invokers = new HashMap<>();

    @Override
    public String getStackName() {
        return STACK_NAME;
    }

    @Override
    protected <TProtectedIn, TProtectedOutput> TProtectedOutput doInvoke(SidecarStackConfiguration cfg
            , EncryptedMessage<TProtectedIn> m
            , Class<TProtectedOutput> protectedResponseCls) throws IOException {

        String retVal = doInvokeAWSLambda(cfg, m.getSynchronicity(), getProcessorServices().stringify(m.getPayload()), m.getContext());
        return getProcessorServices().readJson(retVal, protectedResponseCls);
    }

    @Override
    protected <TType> TType doInvoke(SidecarStackConfiguration cfg, SidecarInput si, Class<TType> respCls) throws IOException {
        final String retVal = doInvokeAWSLambda(cfg, si.getSynchronicity(), getProcessorServices().stringify(si), null);
        return getProcessorServices().readJson(retVal, respCls);
    }

    @Override
    public SidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        if (cfg == null || cfg.getStack() == null) {
            return AlwaysInvalidConfiguration.INVALID_CFG;
        }
        return verifyBuiltConfiguration(cfg, new AWSLambdaConfiguration(cfg.getStack().getParams(), cfg.getTimeout(), configurationDefaults));
    }

    private AWSLambdaConfiguration verifyBuiltConfiguration(SidecarConfiguration cfg, AWSLambdaConfiguration retVal) {

        final ProcessorKeySet alcpIds = getAlcpIdentities();

        // Lookup the key
        if (retVal.getAwsKeyRef() != null && !retVal.specifiesAwsIdentity() && !retVal.assumesRole()) {
            if (alcpIds != null) {
                PartyKeyDescriptor pkd = alcpIds.getKey(retVal.getAwsKeyRef(), KEY_TYPE_AWS_REFERENCED_ID);
                if (pkd != null) {
                    if (pkd.getPasswordCredentials() != null && pkd.getPasswordCredentials().fullyDefined()) {
                        retVal.setAwsKey(pkd.getPasswordCredentials().getKey());
                        retVal.setAwsSecret(pkd.getPasswordCredentials().getSecret());
                    } else {
                        cfg.addMessage(String.format("Password credentials are missing for key %s or not defined fully", retVal.getAwsKeyRef()));
                        cfg.incrementError();
                    }
                } else {
                    cfg.addMessage(String.format("Key %s has not been defined", retVal.getAwsKeyRef()));
                    cfg.incrementError();
                }
            } else {
                cfg.addMessage("ALCP key set not specified to load keys by referenced name");
                cfg.incrementError();
            }
        }

        if (retVal.isValid() && retVal.assumesRole()) {
            // If the white-list is configured, make sure that the
            // function is configured within the white-listed account.



            if (alcpIds != null) {
                Matcher m = matcherForFunctionName(retVal.getFunctionARN());

                if (m.matches()) {
                    final String accountId = m.group(2);

                    checkAccountWhiteListing(cfg, accountId);

                    checkAWSIdentityKeyIsSet(cfg, retVal, alcpIds);

                    checkExternalIdRequired(retVal, alcpIds, accountId);

                } else {
                    cfg.addMessage(String.format("Function name %s is not well-formed.", retVal.getFunctionARN()));
                    cfg.incrementError();
                }
            } else {
                cfg.addMessage("ALCP identities are required to store communication keys");
                cfg.incrementError();
            }
        }

        return retVal;
    }

    protected void checkExternalIdRequired(AWSLambdaConfiguration retVal, ProcessorKeySet alcpIds, String accountId) {
        PartyKeyDescriptor awsExternalId = alcpIds.getKey(accountId, KEY_TYPE_AWS_EXTERNAL_ID);
        if (awsExternalId != null) {
            if (awsExternalId.getPasswordCredentials() != null) {
                retVal.setAssumeAwsRoleExternalId(awsExternalId.getPasswordCredentials().getKey());
            }
        }
    }

    protected void checkAWSIdentityKeyIsSet(SidecarConfiguration cfg, AWSLambdaConfiguration retVal, ProcessorKeySet alcpIds) {
        PartyKeyDescriptor awsIdent = alcpIds.getKeyByType(KEY_TYPE_AWS_IDENTITY);
        if (awsIdent == null || awsIdent.getPasswordCredentials() == null || !awsIdent.getPasswordCredentials().fullyDefined()) {
            cfg.addMessage("AWS identity is not found or completely defined.");
            cfg.incrementError();
        } else {
            retVal.setAwsClusterIdentity(awsIdent);
        }
    }

    protected Matcher matcherForFunctionName(String functionARN) {
        return functionARNPattern.matcher(functionARN);
    }

    protected void checkAccountWhiteListing(SidecarConfiguration cfg, String accountId) {
        PartyKeyDescriptor pkd = getAlcpIdentities().getKeyByType(KEY_TYPE_AWS_WHITELISTED_ACCNTS);

        // The key-set should contain the key specifying what's being white-listed.
        if (pkd != null) {
            // The configuration has supplied
            String[] whiteListedAccounts = splitStandardValueList(pkd.getKeyIdentifier().getKeyId());

            boolean found = false;
            for (String acc : whiteListedAccounts) {
                if (acc.equals(accountId)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                cfg.addMessage(String.format("Account '%s' is not white-listed", accountId));
                cfg.incrementError();
            }
        }
    }

    private String doInvokeAWSLambda(SidecarStackConfiguration cfg, SidecarSynchronicity sync, String input, Map<String, String> context) throws IOException {
        AWSLambdaConfiguration lambdaCfg = (AWSLambdaConfiguration) cfg;

        InvocationType type = InvocationType.EVENT;
        if (sync == SidecarSynchronicity.RequestResponse) {
            type = InvocationType.REQUEST_RESPONSE;
        }

        InvokeResponse result = doInvokeAWSLambda(lambdaCfg, type, input, context);

        if (result.functionError() != null) {
            final String msg = String.format("Functional error '%s' returned, error code: %d"
                    , result.functionError()
                    , result.statusCode());
            log.error(msg);
            throw new IOException(msg);
        }

        // This is a quick check to tell apart 200 for sync and 202 for posted from
        // all other errors.
        switch (result.statusCode()) {
            case 200:
                return result.payload().asUtf8String();
            case 202:
                return null;
            default:
                // This failure. An extended diagnostic context may be logged.
                String msg = String.format("Failed to invoke function %s: returned error code %d (%s).",
                        lambdaCfg.getFunctionARN(),
                        result.statusCode(),
                        result.functionError());

                log.error(msg);
                throw new IOException(msg);
        }
    }

    public InvokeResponse invoke(SidecarStackConfiguration cfg, InvocationType sync, String input, Map<String, String> context) throws IOException {
        return doInvokeAWSLambda((AWSLambdaConfiguration)cfg, sync, input, context);
    }

    private InvokeResponse doInvokeAWSLambda(AWSLambdaConfiguration lambdaCfg, InvocationType type, String input, Map<String, String> context) throws IOException {


        InvokeRequest.Builder req = InvokeRequest.builder()
                .functionName(lambdaCfg.getFunctionARN())
                .invocationType(type)
                .payload(SdkBytes.fromString(input, Charset.defaultCharset()));

        if (context != null) {
            Map<String, Map<String, String>> custom = new HashMap<>();
            custom.put("Custom", context);
            req = req.clientContext(getProcessorServices().base64Stringify(custom));
        }

        InvokeResponse result;
        try {
            result = get(lambdaCfg).invoke(req.build()).get();
        } catch (ServiceException | InterruptedException | ExecutionException ex) {
            String msg = String.format("Failed to invoke function %s: %s (class: %s).",
                    lambdaCfg.getFunctionARN(),
                    ex.getMessage(),
                    ex.getClass().getName());

            log.error(msg, ex);
            throw new IOException(msg);
        }
        return result;
    }


    private LambdaAsyncClient get(AWSLambdaConfiguration cfg) {
        return getSync(cfg, getAWSHash(cfg));
    }

    private StsClient createSTSClient(AWSLambdaConfiguration cfg) {

        final BasicCredentialsDescriptor awsCreds = cfg.getAwsClusterIdentity().getPasswordCredentials();

        AwsCredentials baseCreds = AwsBasicCredentials.create(awsCreds.getKey()
                , awsCreds.getSecret());

        StsClientBuilder builder = StsClient.builder()
                .region(cfg.getAwsRegion())
                .credentialsProvider(StaticCredentialsProvider.create(baseCreds));

        ApacheHttpClient.Builder clBuilder = ApacheHttpClient.builder();
        if (cfg.requiresProxy()) {

            URI proxyURL = cfg.definesProxyPort() ? URI.create(String.format("http://%s:%d", cfg.getProxyHost(),  cfg.getProxyPort()))
                    : URI.create(String.format("http://%s", cfg.getProxyPort()));

            final ProxyConfiguration proxy = ProxyConfiguration.builder()
                    .endpoint(proxyURL)
                    .build();
            clBuilder.proxyConfiguration(proxy);
        }

        return builder
                .httpClientBuilder(clBuilder)
                .build();
    }

    private AwsCredentialsProvider getCredentialsProviderFor(AWSLambdaConfiguration cfg) {
        if (cfg.assumesRole()) {
            return getAssumeRoleCredentialsProvider(cfg);
        } else {
            AwsCredentials baseCreds = AwsBasicCredentials.create(cfg.getAwsKey()
                    , cfg.getAwsSecret());
            return StaticCredentialsProvider.create(baseCreds);
        }
    }

    private AwsCredentialsProvider getAssumeRoleCredentialsProvider(AWSLambdaConfiguration cfg) {

        AssumeRoleRequest arr = AssumeRoleRequest.builder()
                .roleArn(cfg.getAssumeAwsRole())
                .roleSessionName(String.format("MasheryLocal-v4-Lambda@%s", cfg.getAwsClusterIdentity().getKeyIdentifier().getKeyId()))
                .externalId(cfg.getAssumeAwsRoleExternalId())
                .build();

        return StsAssumeRoleCredentialsProvider.builder()
                .refreshRequest(arr)
                .stsClient(createSTSClient(cfg))
                .build();
    }

    private LambdaAsyncClient getSync(AWSLambdaConfiguration cfg, String hash) {
        if (invokers.containsKey(hash)) {
            AWSLambdaHolder awsLambda = invokers.get(hash);
            if (awsLambda != null) {
                awsLambda.lastUsed = System.currentTimeMillis();
                return awsLambda.client;
            }
        }

        synchronized (this) {

            AWSLambdaHolder raceCheckH = invokers.get(hash);
            if (raceCheckH != null && raceCheckH.client != null) {
                return invokers.get(hash).client;
            }

            // The key is not found or was race-deleted. We need to re-build the
            // AWS invoker

            NettyNioAsyncHttpClient.Builder nettyBuilder = NettyNioAsyncHttpClient.builder()
                    .readTimeout(Duration.ZERO)
                    .connectionTimeout(Duration.of(cfg.getTimeout(), ChronoUnit.MILLIS));
            if (cfg.requiresProxy()) {
                final software.amazon.awssdk.http.nio.netty.ProxyConfiguration nettyProxy = software.amazon.awssdk.http.nio.netty.ProxyConfiguration.builder()
                        .host(cfg.getProxyHost())
                        .port(cfg.effectiveProxyPort())
                        .build();
                nettyBuilder = nettyBuilder.proxyConfiguration(nettyProxy);
            }


            // We are using the standard client settings, without overriding these.
            // Maybe these need to be tweaked for high performance?
            LambdaAsyncClientBuilder clientBuilder = LambdaAsyncClient.builder();
            if (cfg.getAwsRegion() != null) {
                clientBuilder = clientBuilder.region(cfg.getAwsRegion());
            }
            clientBuilder = clientBuilder
                    .credentialsProvider(getCredentialsProviderFor(cfg))
                    .httpClientBuilder(nettyBuilder);

            final LambdaAsyncClient rtClient = clientBuilder.build();
            invokers.put(hash, new AWSLambdaHolder(rtClient));

            return rtClient;
        }
    }

    /*
    private void reap() {
        // This method is not implemented. Essentially, the purpose is to iterate over
        // the AWS clients and shut down those that haven't been used for some time.

        synchronized (this) {
            // Do the cleaning
        }
    }
     */

    /**
     * Get the hash of the AWS invoker.
     *
     * @return String for the hash of the AWS invoker.
     */
    private String getAWSHash(AWSLambdaConfiguration cfg) {
        return cfg.getAwsRegion() + "@" +
                cfg.getAwsKey() + ":" + cfg.getAwsSecret();
    }

    public void shutdown() {
        invokers.clear();
    }

    private static class AWSLambdaHolder {
        LambdaAsyncClient client;
        long lastUsed;

        private AWSLambdaHolder(LambdaAsyncClient client) {
            this.client = client;
            this.lastUsed = System.currentTimeMillis();
        }
    }

}
