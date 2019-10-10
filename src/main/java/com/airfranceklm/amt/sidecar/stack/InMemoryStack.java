package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.*;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.*;

import java.io.IOException;
import java.util.*;

/**
 * In-memory stack, used mainly in two scenarios:
 * - quick PoC settings; or
 * - where the sidecar needs to respond only to fixed set of events.
 */
public class InMemoryStack implements AFKLMSidecarStack {

    private Map<MasheryPreprocessorPointReference, Map<String,SidecarOutputHolder<SidecarPreProcessorOutput>>> preMemoryBase = new HashMap<>();
    private Map<MasheryPreprocessorPointReference, Map<String,SidecarOutputHolder<SidecarPostProcessorOutput>>> postMemoryBase = new HashMap<>();

    @Override
    public SidecarPreProcessorOutput invokeAtPreProcessor(AFKLMSidecarStackConfiguration cfg,
                                                          SidecarInvocationData cmd,
                                                          ProcessorServices services) throws IOException {
        ConfigurationImpl privCfg = (ConfigurationImpl)cfg;

        Map<String,SidecarOutputHolder<SidecarPreProcessorOutput>> target = preMemoryBase.get(privCfg.ref);
        if (target != null) {
            SidecarOutputHolder<SidecarPreProcessorOutput> h = target.get(cmd.getInput().getPayloadChecksum());
            if (h != null) {
                return h.output;
            }
        }

        return services.doNothingForPreProcessing();
    }

    @Override
    public SidecarPostProcessorOutput invokeAtPostProcessor(AFKLMSidecarStackConfiguration cfg, SidecarInvocationData cmd, ProcessorServices services) throws IOException {
        ConfigurationImpl privCfg = (ConfigurationImpl)cfg;

        Map<String,SidecarOutputHolder<SidecarPostProcessorOutput>> target = postMemoryBase.get(privCfg.ref);
        if (target != null) {
            SidecarOutputHolder<SidecarPostProcessorOutput> h = target.get(cmd.getInput().getPayloadChecksum());
            if (h != null) {
                return h.output;
            }
        }

        return services.doNothingForPostProcessing();
    }

    public void add(MasheryPreprocessorPointReference ref, String hash, SidecarPreProcessorOutput output, String declaredIn) {
        Map<String, SidecarOutputHolder<SidecarPreProcessorOutput>> target = preMemoryBase.computeIfAbsent(ref, k -> new HashMap<>());
        target.put(hash, new SidecarOutputHolder<>(output, declaredIn));
    }

    public void add(MasheryPreprocessorPointReference ref, String hash, SidecarPostProcessorOutput output, String declaredIn) {
        Map<String, SidecarOutputHolder<SidecarPostProcessorOutput>> target = postMemoryBase.computeIfAbsent(ref, k -> new HashMap<>());
        target.put(hash, new SidecarOutputHolder<>(output, declaredIn));
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
        preMemoryBase.forEach((ref, storedRecs) -> {
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
        Map<String,SidecarOutputHolder<SidecarPreProcessorOutput>> mPre = preMemoryBase.get(ref);
        if (mPre != null) {
            mPre.remove(checksum);
            if (mPre.size() == 0) {
                preMemoryBase.remove(ref);
            }
        }

        Map<String,SidecarOutputHolder<SidecarPostProcessorOutput>> mPost = postMemoryBase.get(ref);
        if (mPost != null) {
            mPost.remove(checksum);
            if (mPost.size() == 0) {
                postMemoryBase.remove(ref);
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

    static class SidecarOutputHolder<T> {
        T output;
        String declaredIn;

        SidecarOutputHolder(T output, String declaredIn) {
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
