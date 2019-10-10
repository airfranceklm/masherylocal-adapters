package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.*;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.config.SidecarSynchronicity;
import com.airfranceklm.amt.sidecar.model.*;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.AWSLambdaException;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * AWS invoker; essentially allowing unused client to be reaped.
 */
public class AWSLambdaStack implements AFKLMSidecarStack {

    private static Logger log = LoggerFactory.getLogger(AWSLambdaStack.class);

    private static final String CFG_FUNCTION_ARN = "functionARN";
    private static final String CFG_AWS_KEY = "key";
    private static final String CFG_AWS_SECRET = "secret";
    private static final String CFG_AWS_REGION = "region";

    private static final String CFG_PROXY_HOST = "proxyHost";
    private static final String CFG_PROXY_PORT = "proxyPort";

    private Map<String, AWSLambdaHolder> invokers = new HashMap<>();

    @Override
    public SidecarPreProcessorOutput invokeAtPreProcessor(AFKLMSidecarStackConfiguration cfg, SidecarInvocationData cmd, ProcessorServices services) throws IOException {
        String rVal = invoke(cfg, cmd.getInput());
        if (rVal != null) {
            return services.asPreProcessor(rVal);
        } else {
            return services.doNothingForPreProcessing();
        }
    }

    @Override
    public SidecarPostProcessorOutput invokeAtPostProcessor(AFKLMSidecarStackConfiguration cfg, SidecarInvocationData cmd, ProcessorServices services) throws IOException {
        String rVal = invoke(cfg, cmd.getInput());
        if (rVal != null) {
            return services.asPostProcessor(rVal);
        } else {
            return services.doNothingForPostProcessing();
        }
    }

    @Override
    public AFKLMSidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return new AWSLambdaConfiguration(cfg.getStackParams(), cfg.getSidecarTimeout());
    }

    public String invoke(AFKLMSidecarStackConfiguration cfg, SidecarInput input) throws IOException {
        AWSLambdaConfiguration lambdaCfg = (AWSLambdaConfiguration)cfg;

        InvocationType type = InvocationType.Event;
        if (input.getSynchronicity() == SidecarSynchronicity.RequestResponse) {
            type = InvocationType.RequestResponse;
        }

        InvokeRequest req = new InvokeRequest()
                .withFunctionName(lambdaCfg.getFunctionARN())
                .withInvocationType(type)
                .withPayload(JsonHelper.toJSON(input))
                .withSdkRequestTimeout((int)lambdaCfg.getTimeout());

        InvokeResult result = null;
        try {
            result = get(lambdaCfg).invoke(req);
        } catch (AWSLambdaException ex) {
            String msg = String.format("Failed to invoke function %s: %s.",
                    lambdaCfg.getFunctionARN(),
                    ex.getMessage());

            log.error(msg, ex);
            throw new IOException(msg);
        }

        // This is a quick check to tell apart 200 for sync and 202 for posted from
        // all other errors.
        switch (result.getStatusCode()) {
            case 200:
                return new String(result.getPayload().array(),
                        StandardCharsets.UTF_8);
            case 202:
                return null;
            default:
                // This failure. An extended diagnostic context may be logged.
                String msg = String.format("Failed to invoke function %s: returned error code %d (%s).",
                        lambdaCfg.getFunctionARN(),
                        result.getStatusCode(),
                        result.getFunctionError());

                log.error(msg);
                throw new IOException(msg);
        }
    }


    private AWSLambda get(AWSLambdaConfiguration cfg) {
        return getSync(cfg, getAWSHash(cfg));
    }

    private AWSLambda getSync(AWSLambdaConfiguration cfg, String hash) {
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

            BasicAWSCredentials credentials = new
                    BasicAWSCredentials(cfg.getAwsKey(), cfg.getAwsSecret());

            // We are using the standard client settings, without overriding these.
            // Maybe these need to be tweaked for high performance?
            AWSLambdaClientBuilder clientBuilder = AWSLambdaClientBuilder.standard();
            if (cfg.getAwsRegion() != null) {
                clientBuilder = clientBuilder.withRegion(cfg.getAwsRegion());
            }

            if (cfg.requiresProxy()) {
                ClientConfiguration cc = new ClientConfiguration().withProxyHost(cfg.getProxyHost());
                if (cfg.getProxyPort() > 0) {
                    cc = cc.withProxyPort(cfg.getProxyPort());
                }
                clientBuilder = clientBuilder.withClientConfiguration(cc);
            }

            clientBuilder = clientBuilder.withCredentials(new AWSStaticCredentialsProvider(credentials));

            AWSLambda rtClient = clientBuilder.build();
            invokers.put(hash, new AWSLambdaHolder(rtClient));

            return rtClient;
        }
    }

    private void reap() {
        // This method is not implemented. Essentially, the purpose is to iterate over
        // the AWS clients and shut down those that haven't been used for some time.

        synchronized (this) {
            // Do the cleaning
        }
    }

    /**
     * Get the hash of the AWS invoker.
     *
     * @return String for the hash of the AWS inovker.
     */
    private String getAWSHash(AWSLambdaConfiguration cfg) {
        return cfg.getAwsRegion() + "@" +
                cfg.getAwsKey() + ":" + cfg.getAwsSecret();
    }

    private class AWSLambdaHolder {
        AWSLambda client;
        long lastUsed;

        private AWSLambdaHolder(AWSLambda client) {
            this.client = client;
            this.lastUsed = System.currentTimeMillis();
        }
    }

    private class AWSLambdaConfiguration implements AFKLMSidecarStackConfiguration {
        String functionARN;
        String awsKey;
        String awsSecret;
        String awsRegion;
        long timeout;

        int proxyPort = -1;
        String proxyHost;

        AWSLambdaConfiguration(Map<String, String> params, long timeout) {
            functionARN = params.get(CFG_FUNCTION_ARN);
            awsKey = params.get(CFG_AWS_KEY);
            awsSecret = params.get(CFG_AWS_SECRET);
            awsRegion = params.get(CFG_AWS_REGION);
            this.timeout = timeout;

            this.proxyHost = params.get(CFG_PROXY_HOST);
            if (params.containsKey(CFG_PROXY_PORT)) {
                this.proxyPort = Integer.parseInt(CFG_PROXY_PORT);
            }
        }

        public int getProxyPort() {
            return proxyPort;
        }

        public String getProxyHost() {
            return proxyHost;
        }

        boolean requiresProxy() {
            return proxyHost != null;
        }

        public boolean isValid() {
            return functionARN != null && awsKey != null && awsSecret != null;
        }

        String getFunctionARN() {
            return functionARN;
        }

        String getAwsKey() {
            return awsKey;
        }

        String getAwsSecret() {
            return awsSecret;
        }

        String getAwsRegion() {
            return awsRegion;
        }

        long getTimeout() {
            return timeout;
        }
    }
}
