package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.builders.PostProcessSidecarInputBuilder;
import com.airfranceklm.amt.sidecar.config.afklyaml.YAMLEndpointConfiguration;
import com.airfranceklm.amt.sidecar.config.afklyaml.YAMLPreProcessorPointConfiguration;
import com.airfranceklm.amt.sidecar.identity.CounterpartKeySet;
import com.airfranceklm.amt.sidecar.model.MasheryProcessorPointReference;
import com.airfranceklm.amt.sidecar.stack.InMemoryStack;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

import static com.airfranceklm.amt.sidecar.model.SidecarInputPoint.PostProcessor;
import static com.airfranceklm.amt.sidecar.model.SidecarInputPoint.PreProcessor;

/**
 * Local configuration directory loader. The loader has the following requirements:
 * <ul>
 *     <li>The ALCP sidecar keyset file must have name <code>alcp.yaml</code></li>;
 *     <li>Any other file is considered an endpoint configuration file</li>
 * </ul>
 */
@Slf4j
public class LocalDirectoryConfigurationProvider implements EndpointConfigurationProvider {

    private SidecarProcessor processor;
    private File root;
    private LocalDirectoryWatcher watcher;

    LocalDirectoryConfigurationProvider(File root) {
        this.root = Objects.requireNonNull(root);
    }

    @Override
    public String describe() {
        return String.format("Local directory (%s)", root.getAbsolutePath());
    }

    @Override
    public void start() {
        if (root.exists() && root.canRead()) {
            this.watcher = new LocalDirectoryWatcher(this.root);
            watcher.onFileModified(this::loadFile);
            watcher.onFileRemoved(this::forget);
            watcher.takeFileMatching((p) -> p.endsWith(".yaml") || p.endsWith(".yml"));

            watcher.startWatching();
        } else {
            log.warn(String.format("Watch for reload in directroy %s hasn't started: it either doesn't exist, or not accessible for writing", this.root));
        }
    }

    @Override
    public void shutdown() {
        if (this.watcher != null) {
            this.watcher.stopWatching();
        }
    }

    public void setup(SidecarProcessor processor) {
        this.processor = processor;

        if (root.exists()) {

            File[] configFiles = root.listFiles((dir, s) -> !s.startsWith(".") && (s.endsWith(".yaml") || s.endsWith(".yml")));
            if (configFiles != null) {
                for (File f : configFiles) {
                    loadFile(f);
                }
            }
        } else {
            log.warn(String.format("Directory for local configuration %s does not exist (yet)", root.getPath()));
        }
    }

    private void loadKeySet(File f) {
        CounterpartKeySet ks = ALCPLoaders.loadCounterpartsFromYaml(f);
        if (ks != null) {
            ks.rehydrateKeys();
        }
        this.processor.setAlcpCounterpartKeys(ks);
    }

    public void loadFile(File f) {
        if (f == null) {
            return;
        }

        if (SidecarProcessorConstants.ALCP_SIDECARS_KEYSET_FILE.equalsIgnoreCase(f.getName()) && f.exists()) {
            loadKeySet(f);
        } else {

            // This is configuration file, or in-memory configuration file.
            // We'll load it as such.

            final String filePath = f.getAbsolutePath();
            try {
                try (InputStream is = new BufferedInputStream(new FileInputStream(f))) {
                    loadStream(filePath, is);
                } catch (FileNotFoundException e) {
                    log.warn(String.format("File %s deleted while scanning", filePath), e);
                } catch (IOException e) {
                    log.warn(String.format("I/O exception while trying to process file %s: %s", f.getAbsolutePath(), e.getMessage()), e);
                }
            } catch (Throwable ex) {
                log.error(String.format("File %s failed to load due to unhandled problem: %s", f.getAbsolutePath(), ex.getMessage())
                        , ex);
            }
        }
    }

    public void loadStream(String filePath, InputStream is) {

        SidecarConfigurationStore configStore = processor.getConfigStore();

        Set<MasheryProcessorPointReference> existingRefs = configStore.getDeclaredIn(filePath);
        InMemoryStack stack = getInMemoryStack();

        Map<MasheryProcessorPointReference, List<String>> currentlyDeclaredInmMem;
        if (stack != null) {
            currentlyDeclaredInmMem = stack.getDeclaredIn(filePath);
        } else {
            currentlyDeclaredInmMem = new HashMap<>();
        }

        Iterable<Object> it = new Yaml().loadAll(new InputStreamReader(is));
        it.forEach((yamlDoc) -> loadStream(yamlDoc, filePath, existingRefs, currentlyDeclaredInmMem));

        // Forget all those references that were not loaded again.
        existingRefs.forEach(configStore::forget);
        if (stack != null) {
            currentlyDeclaredInmMem.forEach((k, v) -> v.forEach((checkSum) -> stack.forget(k, checkSum)));
        }
    }

    private void loadStream(Object yaml, String filePath,
                            Set<MasheryProcessorPointReference> existingRefs,
                            Map<MasheryProcessorPointReference, List<String>> currentlyDeclared) {

        YAMLEndpointConfiguration lc = JsonHelper.convert(yaml, YAMLEndpointConfiguration.class);

        if (lc != null) {
            lc.sync();

            Set<MasheryProcessorPointReference> loadedRefs = loadServicesConfiguration(lc, filePath);
            existingRefs.removeAll(loadedRefs);

            Map<MasheryProcessorPointReference, List<String>> loadedMemoryObject = loadInMemoryStore(lc, filePath);
            loadedMemoryObject.forEach((k, v) -> {
                List<String> existing = currentlyDeclared.get(k);
                if (existing != null) {
                    existing.removeAll(v);
                }
            });
        }
    }

    /**
     * Forgets all the data declared in the file path.
     *
     * @param f file path.
     */
    public void forget(File f) {
        if (f == null) {
            return;
        }

        if (SidecarProcessorConstants.ALCP_SIDECARS_KEYSET_FILE.equalsIgnoreCase(f.getName())) {
            this.processor.setAlcpCounterpartKeys(null);
        } else {

            // This is configuration file removal.

            try {
                final SidecarConfigurationStore configStore = processor.getConfigStore();
                Set<MasheryProcessorPointReference> refs = configStore.getDeclaredIn(f.getAbsolutePath());
                refs.forEach(configStore::forget);

                final InMemoryStack inMemStack = getInMemoryStack();

                if (inMemStack != null) {
                    Map<MasheryProcessorPointReference, List<String>> declMem = inMemStack.getDeclaredIn(f.getAbsolutePath());
                    declMem.forEach((ref, list) -> list.forEach((checksum) -> inMemStack.forget(ref, checksum)));
                }
            } catch (Throwable ex) {
                log.error(String.format("File %s could not forgotten: %s", f.getAbsolutePath(), ex.getMessage()), ex);
            }
        }
    }

    private InMemoryStack getInMemoryStack() {
        return processor.getSidecarStacks().getStackByName(InMemoryStack.STACK_NAME, InMemoryStack.class);
    }

    private Map<MasheryProcessorPointReference, List<String>> loadInMemoryStore(YAMLEndpointConfiguration endpCfg, String declaredInFile) {
        Map<MasheryProcessorPointReference, List<String>> retVal = new HashMap<>();

        InMemoryStack stack = getInMemoryStack();
        if (stack != null) {
            YAMLPreProcessorPointConfiguration cfg = endpCfg.getPreProcessor();
            if (cfg != null && cfg.getInMemory() != null) {
                MasheryProcessorPointReference ref = PreProcessor.at(endpCfg.getServiceId(), endpCfg.getEndpointId());

                List<String> checksums = new ArrayList<>();

                cfg.getInMemory().forEach((entry) -> {
                    stack.add(ref, entry.getHash(), entry.getOutput(), declaredInFile);
                    checksums.add(entry.getHash());
                });

                retVal.put(ref, checksums);
            }
        }

        return retVal;
    }


    private Set<MasheryProcessorPointReference> loadServicesConfiguration(YAMLEndpointConfiguration lc, String originalFile) {
        Set<MasheryProcessorPointReference> loadedRefs = new HashSet<>();

        if (lc.getPreProcessor() != null) {
            PreProcessorSidecarRuntime rt = ConfigurationStoreHelper.buildPreProcessorSidecarRuntime(processor, processor.getSupportedElements(),
                    lc.getPreProcessor().configuredPreflight(),
                    lc.getPreProcessor().configuredSidecar(),
                    lc.getPreProcessor().getStaticModification());

            processor.getConfigStore().acceptConfigurationChange(PreProcessor.at(lc.getServiceId(), lc.getEndpointId()), originalFile, rt);
        }

        if (lc.getPostProcessor() != null) {
            PostProcessSidecarInputBuilder builder = ConfigurationStoreHelper.buildPostProcessorInputBuilder(processor,
                    processor.getSupportedElements(),
                    lc.getPostProcessor().configuredSidecar());
            processor.getConfigStore().acceptConfigurationChange(PostProcessor.at(lc.getServiceId(), lc.getEndpointId()), originalFile, builder);
        }

        return loadedRefs;
    }


}
