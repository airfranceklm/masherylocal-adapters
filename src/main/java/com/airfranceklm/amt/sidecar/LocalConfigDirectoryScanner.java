package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.config.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarOutput;
import com.airfranceklm.amt.sidecar.model.SidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import com.airfranceklm.amt.sidecar.stack.InMemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.*;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Local configuration directory scanner.
 */
class LocalConfigDirectoryScanner {

    private static Logger log = LoggerFactory.getLogger(LocalConfigDirectoryScanner.class);

    private AFKLMSidecarProcessor processor;
    private File root;
    private Path rootPath;

    private WatchService watcher;
    private WatchKey watchKey;
    private Thread watcherThread;

    LocalConfigDirectoryScanner(AFKLMSidecarProcessor processor, File root) {
        this.processor = processor;
        this.root = root;
    }

    void scanOnStartup() {
        if (root.exists()) {
            File[] files = root.listFiles((dir, s) -> s.endsWith(".yaml"));
            if (files != null) {
                for (File f : files) {
                    loadFile(f);
                }
            }
        } else {
            log.warn(String.format("Directory for local configuration %s does not exist", root.getPath()));
        }
    }

    void watch() {
        try {
            watcher = FileSystems.getDefault().newWatchService();
            rootPath = root.toPath();
            watchKey = rootPath.register(watcher,
                    ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            watcherThread = new Thread(new WatcherRunnable());
            watcherThread.start();
        } catch (IOException e) {
            log.error(String.format("Watching for the service could not be started.: %s", e.getMessage()), e);
        }
    }

    /**
     * Stops watching of the directory
     */
    void stopWatching() {
        watchKey.cancel();
        if (watcherThread != null) {
            try {
                watcherThread.interrupt();
                watcherThread.join(TimeUnit.SECONDS.toMillis(10));
            } catch (InterruptedException ex) {
                // Can't do anything about it.
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFile(File f) {
        if (f == null) {
            return;
        }

        try {
            Iterable<Object> it = new Yaml().loadAll(new FileReader(f));
            it.forEach((yamlDoc) -> {
                load((Map<String, Object>) yamlDoc, f.getAbsolutePath());
            });
        } catch (FileNotFoundException e) {
            log.warn(String.format("File %s deleted while scanning", f.getAbsolutePath()), e);
        }
    }

    private void load(Map<String, Object> yaml, String filePath) {
        forDefinedObjectMap(yaml, "configuration", (cfg) -> {
            loadServicesConfiguration(cfg, filePath);
        });
        forDefinedObjectMap(yaml, "in-memory", (cfg) -> {
            loadInMemoryStore(cfg, filePath);
        });
    }

    /**
     * Forgets all the data declared in the file path.
     *
     * @param f file path.
     */
    private void forget(File f) {
        final SidecarConfigurationStore configStore = processor.getConfigStore();
        Set<MasheryPreprocessorPointReference> refs = configStore.getDeclaredIn(f.getAbsolutePath());
        refs.forEach(configStore::forget);

        final InMemoryStack inMemStack = processor.getInMemoryStack();
        Map<MasheryPreprocessorPointReference,List<String>> declMem = inMemStack.getDeclaredIn(f.getAbsolutePath());
        declMem.forEach((ref, list) -> {
            list.forEach((checksum) -> inMemStack.forget(ref, checksum));
        });
    }

    private void loadInMemoryStore(Map<String, Object> yaml, String declaredInFile) {
        InMemoryStack stack = processor.getInMemoryStack();
        Map<MasheryPreprocessorPointReference, List<String>> currentlyDeclared = stack.getDeclaredIn(declaredInFile);

        forEachObjectMapIn(yaml, (serviceId, serviceYaml) -> {
            forEachObjectMapIn(serviceYaml, (endpointId, endpointYaml) -> {
                MasheryPreprocessorPointReference ref = new MasheryPreprocessorPointReference(
                        serviceId,
                        endpointId,
                        SidecarInputPoint.PreProcessor);

                loadSingleInMemoryMap(ref, declaredInFile, endpointYaml, currentlyDeclared, stack);

                ref = new MasheryPreprocessorPointReference(
                        serviceId,
                        endpointId,
                        SidecarInputPoint.PostProcessor);

                loadSingleInMemoryMap(ref, declaredInFile, endpointYaml, currentlyDeclared, stack);
            });
        });

        currentlyDeclared.forEach((ref, list)-> {
            list.forEach((checksum) -> {
                stack.forget(ref, checksum);
            });
        });
    }

    private void loadSingleInMemoryMap(MasheryPreprocessorPointReference ref, String declaredInFile, Map<String, Object> endpointYaml, Map<MasheryPreprocessorPointReference, List<String>> currentlyDeclared, InMemoryStack stack) {

        forDefinedObjectMap(endpointYaml, "pre-processor", (preProcYaml) -> {
            iterateListOfObjectMaps(endpointYaml, (inOut) -> {
                SidecarInput input = parseDefinedObjectMap(inOut,
                        "input",
                        YamlConfigurationBuilder::buildSidecarInputFromYAML);

                SidecarPreProcessorOutput output = parseDefinedObjectMap(inOut,
                        "output",
                        YamlConfigurationBuilder::buildSidecarPreProcessorOutputFromYAML);

                if (input != null && output != null) {
                    final String checksum = input.getPayloadChecksum();
                    stack.add(ref, checksum, output, declaredInFile);

                    List<String> l = currentlyDeclared.get(ref);
                    if (l != null) {
                        l.remove(checksum);
                    }
                }
            });
        });

        forDefinedObjectMap(endpointYaml, "post-processor", (preProcYaml) -> {
            iterateListOfObjectMaps(endpointYaml, (inOut) -> {
                SidecarInput input = parseDefinedObjectMap(inOut,
                        "input",
                        YamlConfigurationBuilder::buildSidecarInputFromYAML);

                SidecarPostProcessorOutput output = parseDefinedObjectMap(inOut,
                        "output",
                        YamlConfigurationBuilder::buildSidecarPostProcessorOutputFromYAML);

                if (input != null && output != null) {
                    final String checksum = input.getPayloadChecksum();
                    stack.add(ref, checksum, output, declaredInFile);

                    List<String> l = currentlyDeclared.get(ref);
                    if (l != null) {
                        l.remove(checksum);
                    }
                }
            });
        });

    }

    private void loadServicesConfiguration(Map<String, Object> yaml, String originalFile) {
        final SidecarConfigurationStore configStore = processor.getConfigStore();
        Set<MasheryPreprocessorPointReference> existingRefs = configStore.getDeclaredIn(originalFile);

        forEachObjectMapIn(yaml, (serviceId, serviceYaml) -> {
            forEachObjectMapIn(serviceYaml, (endpointId, endpointYaml) -> {

                forDefinedObjectMap(endpointYaml, "pre-processor", (preCfgYaml) -> {
                    SidecarConfiguration cfg = YamlConfigurationBuilder
                            .getSidecarConfiguration(SidecarInputPoint.PreProcessor, preCfgYaml);

                    MasheryPreprocessorPointReference ref = new MasheryPreprocessorPointReference(
                            serviceId,
                            endpointId,
                            SidecarInputPoint.PreProcessor);

                    configStore.acceptConfigurationChange(ref, originalFile, cfg);
                    existingRefs.remove(ref);
                });

                forDefinedObjectMap(endpointYaml, "post-processor", (postCfgYaml) -> {
                    SidecarConfiguration cfg = YamlConfigurationBuilder
                            .getSidecarConfiguration(SidecarInputPoint.PostProcessor, postCfgYaml);

                    MasheryPreprocessorPointReference ref = new MasheryPreprocessorPointReference(
                            serviceId,
                            endpointId,
                            SidecarInputPoint.PreProcessor);

                    configStore.acceptConfigurationChange(ref, originalFile, cfg);
                    existingRefs.remove(ref);
                });
            });
        });

        // We need to clean-up references that existed earlier in this file, but were
        // deleted
        existingRefs.forEach(configStore::forget);
    }

    class WatcherRunnable implements Runnable {

        @Override
        public void run() {
            for (; ; ) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException x) {
                    log.warn(String.format("Wait for the file system changes has terminated: %s", x.getMessage()), x);
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == OVERFLOW) {
                        continue;
                    }

                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path touchedChild = ev.context();

                    Path p = rootPath.resolve(touchedChild);
                    File f = p.toFile();
                    if (!f.exists()) {
                        forget(f);
                        log.warn(String.format("Dropped local information from %s", f.getAbsolutePath()));
                    } else {
                        loadFile(f);
                        log.warn(String.format("Re-loaded local information from %s", f.getAbsolutePath()));
                    }
                }
            }
        }
    }
}
