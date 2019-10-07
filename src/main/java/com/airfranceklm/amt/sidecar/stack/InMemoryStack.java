package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.*;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;

import javax.imageio.ImageReader;
import java.io.IOException;
import java.util.*;

/**
 * In-memory stack, used mainly in two scenarios:
 * - quick PoC settings; or
 * - where the sidecar needs to respond only to fixed set of events.
 */
public class InMemoryStack implements AFKLMSidecarStack {
    private static final SidecarOutput doNothing = new SidecarOutputImpl();
    private Map<MasheryPreprocessorPointReference, Map<String,SidecarOutputHolder>> memoryBase = new HashMap<>();

    @Override
    public SidecarOutput invoke(AFKLMSidecarStackConfiguration cfg, SidecarInput input) throws IOException {
        ConfigurationImpl privCfg = (ConfigurationImpl)cfg;

        Map<String,SidecarOutputHolder> target = memoryBase.get(privCfg.ref);
        if (target != null) {
            SidecarOutputHolder h = target.get(input.getPayloadChecksum());
            if (h != null) {
                return h.output;
            }
        }

        return doNothing;
    }



    public void add(MasheryPreprocessorPointReference ref, String hash, SidecarOutput output, String declaredIn) {
        Map<String, SidecarOutputHolder> target = memoryBase.computeIfAbsent(ref, k -> new HashMap<>());
        target.put(hash, new SidecarOutputHolder(output, declaredIn));
    }

    @Override
    public AFKLMSidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return new ConfigurationImpl(cfg);
    }

    /**
     * Retrieves the list of references that were declared in the given file.
     * @param path path of the source file
     * @return Map from reference to the list of checksums
     */
    public Map<MasheryPreprocessorPointReference, List<String>> getDeclaredIn(String path) {
        Map<MasheryPreprocessorPointReference,List<String>> retVal = new HashMap<>();
        memoryBase.forEach((ref, storedRecs) -> {
            storedRecs.forEach((checksum, obj) -> {
                if (Objects.equals(obj.declaredIn, path)) {
                    List<String> l = retVal.computeIfAbsent(ref, r -> new ArrayList<>());
                    l.add(checksum);
                }
            });
        });

        return retVal;
    }

    public void forget(MasheryPreprocessorPointReference ref, String checksum) {
        Map<String,SidecarOutputHolder> m = memoryBase.get(ref);
        if (m != null) {
            m.remove(checksum);
            if (m.size() == 0) {
                memoryBase.remove(ref);
            }
        }
    }

    static class ConfigurationImpl implements AFKLMSidecarStackConfiguration {
        MasheryPreprocessorPointReference ref;

        private ConfigurationImpl(SidecarConfiguration sCfg) {
            this.ref = new MasheryPreprocessorPointReference(sCfg);
        }

        @Override
        public boolean isValid() {
            return true;
        }
    }

    static class SidecarOutputHolder {
        SidecarOutput output;
        String declaredIn;

        SidecarOutputHolder(SidecarOutput output, String declaredIn) {
            this.output = output;
            this.declaredIn = declaredIn;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SidecarOutputHolder that = (SidecarOutputHolder) o;
            return Objects.equals(output, that.output) &&
                    Objects.equals(declaredIn, that.declaredIn);
        }

        @Override
        public int hashCode() {
            return Objects.hash(output, declaredIn);
        }
    }


}
