package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.testsupport.RequestCaseYAMLReader;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.*;
import static org.junit.Assert.assertNotNull;

public class AFKLMSidecarCaseYAMLReader extends RequestCaseYAMLReader<SidecarRequestCase> {
    public AFKLMSidecarCaseYAMLReader(String path) {
        super(path);
    }

    @Override
    protected SidecarRequestCase createRequestCase(String name) {
        return new SidecarRequestCase(name);
    }

    /**
     * Apply endpoint configuration private secrets from a file that will not be
     * versioned in GIT.
     * @param rc request case
     * @param path path to the file with secrets
     * @param set name of the set to be specified.
     */
    protected void applyEndpointConfigurationPrivateSecrets(SidecarRequestCase rc, String path, String set) {
        final InputStream stream = getClass().getResourceAsStream(path);
        assertNotNull(stream);

        Map<String, Map<String,String>> yaml = new Yaml().load(stream);
        Map<String,String> cfg = yaml.get(set);
        assertNotNull("Private configuration file is not found", cfg);
        rc.getTestScenario().getEndpointData().addPreProcessorConfiguration(cfg);
    }

    protected void readSpecificCaseSettings(SidecarRequestCase rc, Map<String,Object> yaml) {
        forDefinedObjectMap(yaml, "sidecar", (sidecarYaml) -> {
            forDefinedObjectMap(sidecarYaml, "input", (inputYaml) -> {
                rc.sidecarInput = buildSidecarInputFromYAML(inputYaml);
            });

            forDefinedString(sidecarYaml, "throw error", (v) -> {
                rc.sidecarException = v;
            });

            forDefinedObjectMap(sidecarYaml, "output", (outputYaml) -> {
                if (rc.isPreProcessorCase()) {
                    rc.preProcessorOutput = buildSidecarPreProcessorOutputFromYAML(outputYaml);
                } else {
                    rc.postProcessorOutput = buildSidecarPostProcessorOutputFromYAML(outputYaml);
                }
            });


        });

        forDefinedObjectMap(yaml, "preflight", (sidecarYaml) -> {
            forDefinedObjectMap(sidecarYaml, "input", (inputYaml) -> {
                rc.preflightInput = buildSidecarInputFromYAML(inputYaml);
            });

            forDefinedString(sidecarYaml, "throw error", (v) -> {
                rc.preflightException = v;
            });

            forDefinedObjectMap(sidecarYaml, "output", (outputYaml) -> {
                rc.preflightOutput = buildSidecarPreProcessorOutputFromYAML(outputYaml);
            });
        });

    }
}
