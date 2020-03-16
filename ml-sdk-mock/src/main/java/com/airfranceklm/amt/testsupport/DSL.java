package com.airfranceklm.amt.testsupport;

import com.airfranceklm.amt.yaml.YamlHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.*;
import static org.junit.Assert.fail;

/**
 * Capability to create the test cases programmatically in a clear sequence, using lambda syntax.
 * Using this class, the unit test can create a base scenario and then "extend" the scenario
 * by supplying the delta configuration, which makes it easier (at least, for the programmer)
 * to read.
 */
public abstract class DSL<T extends MasheryProcessorTestCase> {

    protected abstract T create();

    private Supplier<T> initial;

    protected TypeReference<T> getCaseTypeReference() {
        return new TypeReference<T>() {
        };
    }

    @Getter
    @Setter
    private List<Consumer<T>> expressions;

    public DSL() {
        expressions = new ArrayList<>();
    }

    public T build() {
        return build("DSL-case");
    }

    public DSL<T> expr(Consumer<T> cfg) {
        expressions.add(cfg);
        return this;
    }

    public T build(String name) {
        T retVal = initial != null ? initial.get(): create();
        retVal.setName(name);

        for (Consumer<T> c : expressions) {
            c.accept(retVal);
        }

        return retVal;
    }


    /**
     * Copies the data of the lambdas to another one.
     *
     * @param other another DSL instance to receive the values.
     */
    protected void copy(@NonNull DSL<T> other) {
        this.initial = other.initial;
        Mocks.cloneNullableCollection(other::getExpressions, this::setExpressions, ArrayList::new);
    }

    /**
     * Save and load resource
     * @param owner owner class
     * @param resourceName name of the resource
     * @return DSL with the initializer.
     */
    public DSL<T> startFrom(Class<?> owner, String resourceName) {
        initial = () -> loadInitialObject(owner, resourceName);

        return this;
    }

    private T loadInitialObject(Class<?> owner, String resourceName) {
        try {
            Map<String, ?> m = YamlHelper.loadSingleYamlDocument(owner, resourceName);
            return MasheryProcessorTestSuite.objectMapper.convertValue(m, getCaseTypeReference());
        } catch (IOException ex) {
            fail(String.format("Can't load scenario file %s owned by %s", resourceName, owner.getName()));

            throw new IllegalStateException("Unreachable code");
        }
    }

    public DSL<T> emptyClientRequest() {
        expr((tc) -> {
            buildClientRequest(tc, (cfg) -> {});
        });

        return this;
    }

    public DSL<T> identifyEndpoint() {
        expr((tc) -> {
            buildEndpoint(tc, (cfg) -> {
                cfg.endpointId("anEndpointId")
                        .serviceId("aServiceId")
                        .endpointName("anEndpointName");
            });
        });

        return this;
    }

    public DSL<T> expectCurtailingWith(int code, String payload) {
        expr((tc) -> buildMasheryResponse(tc, (cfg) -> cfg
                .clearHeaders()
                .header("Content-Type", "application/xml")
                .statusCode(code)
                .payload(payload)));
        return this;
    }

    public DSL<T> postProcessorCase() {
        expr((tc) -> tc.setPoint(TestCasePoint.PostProcessor));
        return this;
    }
}
