package com.airfranceklm.amt.testsupport;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Test scenario, summarizing endpoint configuration and expected results of the interactoin
 * between the pre-processor and Lambda function.
 */
public class TestScenario<T extends RequestCase> {

    private Map<String, T> cases = new HashMap<>();
    EndpointData endpointData;

    void addRequestCase(String name, T sr) {
        sr.setTestScenario(this);
        cases.put(name, sr);
    }

    T getRequestCase(String name) {
        return cases.get(name);
    }

    Set<String> getCaseNames() {
        return cases.keySet();
    }

    void forEachCase(RequestCaseConsumer<T> consumer) {
        cases.forEach((key, value) -> {
            consumer.accept(value);
        });
    }

    public EndpointData getEndpointData() {
        return endpointData;
    }

    public void withEndpointData(Consumer<EndpointData> c) {
        this.endpointData = new EndpointData();
        c.accept(this.endpointData);
    }
}
