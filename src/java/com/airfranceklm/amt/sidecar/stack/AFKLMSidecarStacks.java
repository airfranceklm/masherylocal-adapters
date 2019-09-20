package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;

/**
 * Supported transports by this plugin.
 */
public class AFKLMSidecarStacks {
    private static final String AWS_STACK = "aws";
    private static final String HTTP_STACK = "http";
    private static final String IN_MEMORY_STACK = "in-memory";
    private static final String CACHE_STACK = "cache";

    private AWSLambdaStack awsStack;
    private HTTPSidecarStack httpStack;
    private InMemoryStack inMemoryStack;
    private CacheStack cacheStack;

    public AFKLMSidecarStacks() {
        awsStack = new AWSLambdaStack();
        httpStack = new HTTPSidecarStack();
        inMemoryStack = new InMemoryStack();
        cacheStack = new CacheStack();

    }

    public AFKLMSidecarStack getStackFor(SidecarConfiguration cfg) {
        switch (cfg.getStack()) {
            case AWS_STACK:
                return awsStack;
            case HTTP_STACK:
                return httpStack;
            case IN_MEMORY_STACK:
                return inMemoryStack;
            case CACHE_STACK:
                return cacheStack;
            default:
                return null;
        }
    }

}
