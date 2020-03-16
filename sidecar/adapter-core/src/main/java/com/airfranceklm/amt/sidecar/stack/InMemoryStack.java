package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.*;

import java.io.IOException;
import java.util.*;

/**
 * In-memory stack, used mainly in two scenarios:
 * - quick PoC settings; or
 * - where the sidecar needs to respond only to fixed set of events.
 */
public class InMemoryStack extends CommonStack {

    public static final String STACK_NAME = "in-memory";

    private Map<MasheryProcessorPointReference, Map<String, SidecarOutputHolder<SidecarPreProcessorOutput>>> preMemoryBase = new HashMap<>();
    private Map<MasheryProcessorPointReference, Map<String, SidecarOutputHolder<SidecarPostProcessorOutput>>> postMemoryBase = new HashMap<>();

    @Override
    public String getStackName() {
        return STACK_NAME;
    }

    @Override @SuppressWarnings("unchecked")
    public <T extends CallModificationCommand, U extends SidecarOutput<T>> U invoke(SidecarStackConfiguration cfg, SidecarInvocationData cmd, Class<U> expectedType) throws IOException {
        ConfigurationImpl privCfg = (ConfigurationImpl) cfg;

        if (SidecarPreProcessorOutput.class.isAssignableFrom(expectedType)) {
            Map<String, SidecarOutputHolder<SidecarPreProcessorOutput>> target = preMemoryBase.get(privCfg.ref);
            if (target != null) {
                SidecarOutputHolder<SidecarPreProcessorOutput> h = target.get(cmd.getInput().getInputChecksum());
                if (h != null) {
                    return (U)h.output;
                }
            }

            return getProcessorServices().doNothing(expectedType);
        } else if (SidecarPostProcessorOutput.class.isAssignableFrom(expectedType)) {
            Map<String, SidecarOutputHolder<SidecarPostProcessorOutput>> target = postMemoryBase.get(privCfg.ref);
            if (target != null) {
                SidecarOutputHolder<SidecarPostProcessorOutput> h = target.get(cmd.getInput().getInputChecksum());
                if (h != null) {
                    return (U)h.output;
                }
            }

            return getProcessorServices().doNothing(expectedType);
        }

        throw new IOException(String.format("Unknown return type: %s", expectedType.getName()));
    }

    public void add(MasheryProcessorPointReference ref, String hash, SidecarPreProcessorOutput output, String declaredIn) {
        Map<String, SidecarOutputHolder<SidecarPreProcessorOutput>> target = preMemoryBase.computeIfAbsent(ref, k -> new HashMap<>());
        target.put(hash, new SidecarOutputHolder<>(output, declaredIn));
    }

    public void add(MasheryProcessorPointReference ref, String hash, SidecarPostProcessorOutput output, String declaredIn) {
        Map<String, SidecarOutputHolder<SidecarPostProcessorOutput>> target = postMemoryBase.computeIfAbsent(ref, k -> new HashMap<>());
        target.put(hash, new SidecarOutputHolder<>(output, declaredIn));
    }

    @Override
    public SidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return new ConfigurationImpl(cfg);
    }

    /**
     * Retrieves the list of references that were declared in the given file.
     *
     * @param path path of the source file
     * @return Map from reference to the list of checksums
     */
    public Map<MasheryProcessorPointReference, List<String>> getDeclaredIn(String path) {
        Map<MasheryProcessorPointReference, List<String>> retVal = new HashMap<>();
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

    public void forget(MasheryProcessorPointReference ref, String checksum) {
        Map<String, SidecarOutputHolder<SidecarPreProcessorOutput>> mPre = preMemoryBase.get(ref);
        if (mPre != null) {
            mPre.remove(checksum);
            if (mPre.size() == 0) {
                preMemoryBase.remove(ref);
            }
        }

        Map<String, SidecarOutputHolder<SidecarPostProcessorOutput>> mPost = postMemoryBase.get(ref);
        if (mPost != null) {
            mPost.remove(checksum);
            if (mPost.size() == 0) {
                postMemoryBase.remove(ref);
            }
        }
    }

    static class ConfigurationImpl implements SidecarStackConfiguration {
        MasheryProcessorPointReference ref;

        private ConfigurationImpl(SidecarConfiguration sCfg) {
//            this.ref = new MasheryProcessorPointReference(sCfg);
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
