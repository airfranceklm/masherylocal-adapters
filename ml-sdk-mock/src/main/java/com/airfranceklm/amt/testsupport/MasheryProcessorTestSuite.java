package com.airfranceklm.amt.testsupport;

import com.airfranceklm.amt.yaml.YamlHelper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.*;

/**
 * Support for the suite of tests that are loaded.
 * @param <T>
 */
@NoArgsConstructor
public class MasheryProcessorTestSuite<T extends MasheryProcessorTestCase> extends MasheryProcessorTestSupport<T> {

    static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }

    public MasheryProcessorTestSuite(@NonNull Class<?> owner, @NonNull String resource) {
        InputStream is = null;
        try {
            is = owner.getResourceAsStream(resource);
            assertNotNull(String.format("Null input stream for resource %s of class %s", resource, owner.getName()), is);

            loadCasesFrom(is);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    // Ignore.
                }
            }
        }
    }


    @Getter
    private MasheryEndpointModel endpointModel;
    private List<T> testCases = new ArrayList<>();

    protected TypeReference<T> getTestCaseTypeReference() {
        return new TypeReference<T>() {
        };
    }

    protected TestModelVisitor createInheritanceVisitor() {
        return new TestModelInheritanceVisitor<T>(this);
    }

    public void forExistingCase(@NonNull String name, @NonNull Consumer<T> c) {
        T tc = getCase(name);
        if (tc != null) {
            c.accept(tc);
        } else {
            fail(String.format("Test case %s is not found", name));
        }
    }

    public T getCase(@NonNull String name) {
        return testCases.stream()
                .filter((tc) -> name.equals(tc.getName()))
                .findFirst()
                .orElse(null);
    }

    public T getFirst() {
        if (testCases.size() > 0) {
            return testCases.get(0);
        } else {
            return null;
        }
    }

    public void loadCasesFrom(@NonNull InputStream is) {
        endpointModel = null;
        testCases.clear();

        Iterator<Object> objects = YamlHelper.loadAllYamlDocuments(is);
        assertTrue(objects.hasNext());

        endpointModel = objectMapper.convertValue(objects.next(), MasheryEndpointModel.class);

        while (objects.hasNext()) {
            T caseData = objectMapper.convertValue(objects.next(), getTestCaseTypeReference());
            testCases.add(caseData);
        }

        // Cases are loaded; now we'll need to do the second-pass on these.
        TestModelVisitor v = createInheritanceVisitor();
        testCases.forEach((tc) -> tc.acceptVisitor(v));
    }

}
