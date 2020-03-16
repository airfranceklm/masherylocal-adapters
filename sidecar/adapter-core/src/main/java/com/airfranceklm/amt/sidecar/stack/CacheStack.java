package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.*;
import com.mashery.trafficmanager.cache.CacheException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Cache stack, using Mashery cache stack to load data.
 */
@Slf4j
public class CacheStack extends CommonStack {

    public static final String STACK_NAME = "cache";

    public CacheStack() {
    }

    @Override
    public String getStackName() {
        return STACK_NAME;
    }

    @Override @SuppressWarnings("unchecked")
    public <T extends CallModificationCommand, U extends SidecarOutput<T>> U invoke(SidecarStackConfiguration cfg, SidecarInvocationData cmd, Class<U> expectedType) throws IOException {
        if (cmd.getCache() == null || cmd.getInput() == null) {
            return getProcessorServices().doNothing(expectedType);
        }

        CacheConfiguration mCfg = (CacheConfiguration)cfg;
        String key = getCacheKey(mCfg, cmd.getInput());

        try {
            Object c = cmd.getCache().get(getClass().getClassLoader(), key);

            if (c == null) {
                return null;
            } else {
                if (expectedType.isInstance(c)) {
                    return (U)c;
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
    public SidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return new CacheConfiguration(cfg);

    }

    static class CacheConfiguration implements SidecarStackConfiguration {
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
                input.getInputChecksum());
    }
}
