package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.*;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory stack, used mainly in two scenarios:
 * - quick PoC settings; or
 * - where the sidecar needs to respond only to fixed set of events.
 */
public class InMemoryStack implements AFKLMSidecarStack {
    private static final SidecarOutput doNothing = new SidecarOutputImpl();
    private Map<String, SidecarOutput> memoryBase = new HashMap<>();

    @Override
    public SidecarOutput invoke(AFKLMSidecarStackConfiguration cfg, SidecarInput input) throws IOException {
        SidecarOutput inMem = memoryBase.get(getLookupKey(((ConfigurationImpl)cfg).endpointId, input.getPayloadChecksum()));
        return inMem != null ? inMem : doNothing;
    }

    private String getLookupKey(String endpId, String payloadChecksum) {
        return endpId + "_" + payloadChecksum;
    }

    public void add(String endpointId, String hash, SidecarOutput output) {
        memoryBase.put(getLookupKey(endpointId, hash), output);
    }

    @Override
    public AFKLMSidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return new ConfigurationImpl(cfg);
    }

    static class ConfigurationImpl implements AFKLMSidecarStackConfiguration {
        String endpointId;

        private ConfigurationImpl(SidecarConfiguration sCfg) {
            this.endpointId = sCfg.getEndpointId();
        }

        @Override
        public boolean isValid() {
            return endpointId != null;
        }
    }
}
