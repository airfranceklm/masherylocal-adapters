package com.airfranceklm.amt.testsupport;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.forDefinedObjectMap;
import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.forDefinedString;
import static junit.framework.Assert.assertNotNull;

/**
 * Reader that automates reading the data from the YAML file.
 *
 * @param <T> type of the request case.
 */
public abstract class RequestCaseYAMLReader<T extends RequestCase>  {
    private Map<String, TestScenario<T>> scenarios;

    public RequestCaseYAMLReader(String path) {

        InputStream is = getClass().getResourceAsStream(path);
        if (is == null) {
            throw new IllegalStateException(String.format("Missing resource: %s", path));
        }

        scenarios = new HashMap<>();
        slurpScenarioData(is);

        secondPassAllCases();
    }

    protected abstract T createRequestCase(String name);

    private void secondPassAllCases() {
        scenarios.forEach((key, value) -> {
            value.forEachCase(RequestCase::secondPass);
        });
    }

    /**
     * Creates a request mock factory from the given resource set.
     *
     * @param is resource from which the tests need to be created.
     */
    private void slurpScenarioData(InputStream is) {
        Map<String, Object> yaml = new Yaml().load(is);

        yaml.forEach((key, value) -> {
            scenarios.put(key, toTestScenario((Map<String, Object>) value));
        });
    }

    public T getRequestCase(String setName, String scenarioName) {
        TestScenario<T> ts = scenarios.get(setName);
        assertNotNull(ts);
        return ts.getRequestCase(scenarioName);
    }

    private TestScenario toTestScenario(Map<String, Object> yaml) {
        TestScenario retVal = new TestScenario();
        retVal.endpointData = new EndpointData();

        forDefinedObjectMap(yaml, "endpoint", retVal.endpointData::buildFromYAML);

        forDefinedObjectMap(yaml, "cases", (casesMap) -> {
            casesMap.forEach((key, value) -> {
                final T sr = toRequestCase(key, (Map<String, Object>) value);
                retVal.addRequestCase(key, sr);
            });
        });

        return retVal;
    }

    protected T toRequestCase(String name, Map<String, Object> yaml) {
        T sc = createRequestCase(name);

        sc.preProcessor = true;
        forDefinedString(yaml, "point", (v) -> {
            if ("post-processor".equalsIgnoreCase(v)) {
                sc.preProcessor = false;
            }
        });

        forDefinedObjectMap(yaml, "client", (clYaml) -> {
            sc.buildAPIClientRequestFromYaml(clYaml);
        });


        if (!sc.preProcessor &&
                yaml.containsKey("expect api origin request modifications")) {
            throw new IllegalStateException("You are trying to express MODIFICATION of Mashery's request TOWARDS API origin in the " +
                    "POST-processor. Either your test is about PRE-processor, or the section(s) is(are) " +
                    "unnecessary copy-paste.");
        }

        forDefinedObjectMap(yaml, "authorization context", (acYaml) -> {
            sc.buildAuthorizationContextFromYaml(acYaml);
        });

        forDefinedObjectMap(yaml, "request to the api provider", (apiProvReqYaml) -> {
            sc.apiOriginRequest = new APIOriginRequest(apiProvReqYaml);

            forDefinedObjectMap(yaml, "expect api origin request modifications", sc.apiOriginRequest::buildSidecarEffectExpectation);
        });

        forDefinedObjectMap(yaml, "api origin response", (apiResponseYaml) -> {
            sc.buildAPIOriginResponseFromYaml(apiResponseYaml);
        });


        forDefinedObjectMap(yaml, "key", (keyYaml) -> {
            sc.buildPackageKeyFromYaml(keyYaml);
        });

        forDefinedObjectMap(yaml, "expect traffic manager", (tmExpYaml) -> {
            sc.buildTrafficManagerExpectationFromYaml(tmExpYaml);
        });

        forDefinedObjectMap(yaml, "expect http response modification", (httpResYaml) -> {
            sc.buildExpectedHTTPResponseFromYaml(httpResYaml);
        });

        readSpecificCaseSettings(sc, yaml);
        return sc;
    }

    /**
     * Read the specific case settings
     *
     * @param rc   case instance
     * @param yaml yaml configuration to read this from.
     */
    protected void readSpecificCaseSettings(T rc, Map<String, Object> yaml) {
        // Default implementation does nothing.
    }


}
