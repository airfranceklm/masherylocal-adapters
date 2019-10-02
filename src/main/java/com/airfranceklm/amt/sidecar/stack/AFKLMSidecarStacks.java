package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;

/**
 * Supported transports by this plugin.
 */
public class AFKLMSidecarStacks {
    private static final String AWS_LAMBDA_STACK = "aws-lambda";
    private static final String AWS_SNS_STACK = "aws-sns";
    private static final String HTTP_STACK = "http";
    private static final String IN_MEMORY_STACK = "in-memory";
    private static final String CACHE_STACK = "cache";
    private static final String FILE_STACK = "file";
    private static final String LOG_STACK = "log";

    private AWSLambdaStack awsStack;
    private AWSSNSStack awssnsStack;
    private HTTPSidecarStack httpStack;
    private InMemoryStack inMemoryStack;
    private CacheStack cacheStack;
    private FileStack fileStack;
    private LogStack logStack;

    public AFKLMSidecarStacks() {
        awsStack = new AWSLambdaStack();
        awssnsStack = new AWSSNSStack();
        httpStack = new HTTPSidecarStack();
        inMemoryStack = new InMemoryStack();
        cacheStack = new CacheStack();
        fileStack = new FileStack();
        logStack = new LogStack();

    }

    public AFKLMSidecarStack getStackFor(SidecarConfiguration cfg) {
        switch (cfg.getStack()) {
            case AWS_LAMBDA_STACK:
                return awsStack;
            case AWS_SNS_STACK:
                return awssnsStack;
            case HTTP_STACK:
                return httpStack;
            case IN_MEMORY_STACK:
                return inMemoryStack;
            case CACHE_STACK:
                return cacheStack;
            case FILE_STACK:
                return fileStack;
            case LOG_STACK:
                return logStack;
            default:
                return null;
        }
    }

    public InMemoryStack getInMemoryStack() {
        return inMemoryStack;
    }
}
