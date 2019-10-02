package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.config.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.events.Event;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.nio.file.FileSystem;
import java.util.Map;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.forDefinedObjectMap;
import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.forEachObjectMapIn;

class LocalConfigDirectoryScanner  {

    private AFKLMSidecarProcessor processor;
    private File root;

    public LocalConfigDirectoryScanner(AFKLMSidecarProcessor processor, File root) {
        this.processor = processor;
        this.root = root;
    }

    void scanOnStartup() {
        File[] files = root.listFiles((dir, s) -> s.endsWith(".yaml"));
        for (File f: files) {
            loadFile(f);
        }
    }

    private void loadFile(File f) {
        try {
            Iterable<Event> it = new Yaml().parse(new FileReader(f));
            it.forEach((yamlDoc) -> {
                Map<String,Object> obj = (Map<String,Object>)yamlDoc;
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void load(Map<String, Object> yaml) {
        forDefinedObjectMap(yaml, "configuration", this::loadServicesConfiguration);
    }

    private void loadInMemoryStore(Map<String, Object> yaml) {
        forEachObjectMapIn(yaml, (serviceId, serviceYaml) -> {
            forEachObjectMapIn(serviceYaml, (endpointId, endpointYaml) -> {
                // TODO
            });
        });
    }

    private void loadServicesConfiguration(Map<String,Object> yaml) {
        forEachObjectMapIn(yaml, (serviceId, serviceYaml) -> {
            forEachObjectMapIn(serviceYaml, (endpointId, endpointYaml) -> {

                forDefinedObjectMap(endpointYaml, "pre-processor", (preCfgYaml) -> {
                    SidecarConfiguration cfg = YamlConfigurationBuilder
                            .getSidecarConfiguration(SidecarInputPoint.PreProcessor, preCfgYaml);

                    processor.getConfigStore().acceptConfigurationChange(serviceId, endpointId, cfg);
                });

                forDefinedObjectMap(endpointYaml, "post-processor", (postCfgYaml) -> {
                    SidecarConfiguration cfg = YamlConfigurationBuilder
                            .getSidecarConfiguration(SidecarInputPoint.PostProcessor, postCfgYaml);

                    processor.getConfigStore().acceptConfigurationChange(serviceId, endpointId, cfg);
                });

            });

        });

    }
}
