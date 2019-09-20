package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.SidecarInput;
import com.airfranceklm.amt.sidecar.SidecarOutput;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.mashery.trafficmanager.cache.Cache;
import com.mashery.trafficmanager.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Cache stack, using Mashery cache stack to load data.
 */
public class CacheStack implements AFKLMSidecarStack {
    private static final Logger log = LoggerFactory.getLogger(CacheStack.class);

    public CacheStack() {
    }

    @Override
    public SidecarOutput invoke(AFKLMSidecarStackConfiguration cfg, SidecarInput input) throws IOException {
        final String cacheKey = getCacheKey(input);
        final Cache cache = ((CacheStackConfiguration)cfg).getCache();

        try {
            return (SidecarOutput) cache.get(getClass().getClassLoader(), cacheKey);
        } catch (CacheException e) {
            // TODO: circuit-break logging errors.
            log.error(String.format("Failed to retrieve key %s from cache: %s", cacheKey, e.getMessage()), e);
            throw new IOException(e);
        }
    }

    @Override
    public AFKLMSidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return new CacheStackConfiguration(null);
        // TODO: Carrying forward the cache is not possible in the current code structure
        // It needs to be changed.
    }

    private String getCacheKey(SidecarInput input) {
        return String.format("afklm:cacheSack:%s", input.getPayloadChecksum());
    }


    class CacheStackConfiguration implements AFKLMSidecarStackConfiguration {
        private Cache cache;

        CacheStackConfiguration(Cache cache) {
            this.cache = cache;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        Cache getCache() {
            return cache;
        }
    }
}
