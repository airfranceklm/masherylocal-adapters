package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.model.*;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.mashery.trafficmanager.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Cache stack, using Mashery cache stack to load data.
 */
public class CacheStack implements AFKLMSidecarStack {
    private static final Logger log = LoggerFactory.getLogger(CacheStack.class);

    CacheStack() {
    }

    @Override
    public SidecarPreProcessorOutput invokeAtPreProcessor(AFKLMSidecarStackConfiguration cfg, SidecarInvocationData cmd, ProcessorServices services) throws IOException {
        if (cmd.getCache() == null || cmd.getInput() == null) {
            return services.doNothingForPreProcessing();
        }

        CacheConfiguration mCfg = (CacheConfiguration)cfg;
        String key = getCacheKey(mCfg, cmd.getInput());

        try {
            Object c = cmd.getCache().get(getClass().getClassLoader(), key);

            if (c == null) {
                return null;
            } else {
                if (c instanceof SidecarPreProcessorOutput) {
                    return (SidecarPreProcessorOutput)c;
                } else {
                    log.error(String.format("Class collision for key %s", key));
                    throw new IOException(String.format("Class collision: %s expected, but %s was found",
                            SidecarPreProcessorOutput.class,
                            c.getClass().getName()));
                }
            }
        } catch (CacheException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public SidecarPostProcessorOutput invokeAtPostProcessor(AFKLMSidecarStackConfiguration cfg, SidecarInvocationData cmd, ProcessorServices services) throws IOException {
        if (cmd.getCache() == null || cmd.getInput() == null) {
            return services.doNothingForPostProcessing();
        }

        CacheConfiguration mCfg = (CacheConfiguration)cfg;
        String key = getCacheKey(mCfg, cmd.getInput());

        try {
            Object c = cmd.getCache().get(getClass().getClassLoader(), key);

            if (c == null) {
                return null;
            } else {
                if (c instanceof SidecarPostProcessorOutput) {
                    return (SidecarPostProcessorOutput)c;
                } else {
                    log.error(String.format("Class collision for key %s", key));
                    throw new IOException(String.format("Class collision: %s expected, but %s was found",
                            SidecarPostProcessorOutput.class,
                            c.getClass().getName()));
                }
            }
        } catch (CacheException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public AFKLMSidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return new CacheConfiguration(cfg);

    }

    static class CacheConfiguration implements AFKLMSidecarStackConfiguration {
        private String serviceId;
        private String endpointId;

        CacheConfiguration(SidecarConfiguration cfg) {
            this.serviceId = cfg.getServiceId();
            this.endpointId = cfg.getEndpointId();
        }

        @Override
        public boolean isValid() {
            return serviceId != null && endpointId != null;
        }

        public String getServiceId() {
            return serviceId;
        }

        public String getEndpointId() {
            return endpointId;
        }
    }

    private String getCacheKey(CacheConfiguration cfg, SidecarInput input) {
        return String.format("afklm:cacheSack:%s>%s:%s",
                cfg.getServiceId(),
                cfg.getEndpointId(),
                input.getPayloadChecksum());
    }
}
