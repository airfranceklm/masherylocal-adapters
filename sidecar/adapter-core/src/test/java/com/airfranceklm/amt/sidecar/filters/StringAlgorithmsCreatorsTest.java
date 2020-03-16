package com.airfranceklm.amt.sidecar.filters;

import com.airfranceklm.amt.sidecar.elements.ElementFilterCreator;
import com.airfranceklm.amt.sidecar.elements.IllegalFilterExpressionException;
import com.airfranceklm.amt.sidecar.elements.StringFilterAlgorithms;
import org.junit.Test;

import java.util.function.Predicate;

import static com.airfranceklm.amt.sidecar.elements.StandardElementsFactory.getFilterAlgorithm;
import static org.junit.Assert.*;

/**
 * Test whether the string algorithms are accessible via the {@link com.airfranceklm.amt.sidecar.elements.StandardElementsFactory}.
 */
public class StringAlgorithmsCreatorsTest {

    @Test
    public void testAbsent() throws IllegalFilterExpressionException {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.Empty);
        assertNotNull(efc);

        Predicate<String> p = efc.create("unit-test-null", null);
        assertTrue(p.test(null));
        assertTrue(p.test(""));
        assertFalse(p.test("A"));
    }

    @Test
    public void testPresent() throws IllegalFilterExpressionException {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.NonEmpty);
        assertNotNull(efc);

        Predicate<String> p = efc.create("unit-test-null", null);
        assertFalse(p.test(null));
        assertFalse(p.test(""));
        assertTrue(p.test("A"));
    }

    @Test
    public void testEq() throws IllegalFilterExpressionException {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.Eq);
        assertNotNull(efc);

        Predicate<String> p = efc.create("unit-test-null", null);
        assertTrue(p.test(null));
        assertFalse(p.test(""));
        assertFalse(p.test("A"));

        p = efc.create("unit-test-null", "A");
        assertFalse(p.test(null));
        assertFalse(p.test(""));
        assertTrue(p.test("A"));
        assertFalse(p.test("a"));
    }

    @Test
    public void testEqi() throws IllegalFilterExpressionException {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.EqI);
        assertNotNull(efc);

        Predicate<String> p = efc.create("unit-test-null", null);
        assertTrue(p.test(null));
        assertFalse(p.test(""));
        assertFalse(p.test("A"));

        p = efc.create("unit-test-null", "A");
        assertFalse(p.test(null));
        assertFalse(p.test(""));
        assertTrue(p.test("A"));
        assertTrue(p.test("a"));
    }

    @Test
    public void voidTestRegexNullExpression() {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.Regex);
        assertNotNull(efc);

        try {
            efc.create("unit-test", null);
        } catch (IllegalFilterExpressionException ex) {
             assertEquals("Regex requires a non-null expression", ex.getMessage());
             assertEquals(ex.getElementName(), "unit-test");
             assertNull(ex.getExpression());

             return;
        }

        fail("Exception should have been caught");
    }

    @Test
    public void voidTestRegexMalformedExpression() {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.Regex);
        assertNotNull(efc);

        try {
            efc.create("unit-test", ".*[*");
        } catch (IllegalFilterExpressionException ex) {
             assertEquals("Expression '.*[*' is not a valid regexp", ex.getMessage());
             assertEquals(ex.getElementName(), "unit-test");
             assertEquals(ex.getExpression(), ".*[*");

             return;
        }

        fail("Exception should have been caught");
    }

    @Test
    public void testRegex() throws IllegalFilterExpressionException {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.Regex);
        assertNotNull(efc);

        Predicate<String> p = efc.create("unit-test-null", "Abc|Cde");
        assertTrue(p.test("Abc"));
        assertTrue(p.test("Cde"));
        assertFalse(p.test("ABc"));
    }

    @Test
    public void testRegex_i() throws IllegalFilterExpressionException {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.RegexI);
        assertNotNull(efc);

        Predicate<String> p = efc.create("unit-test-null", "Abc|Cde");
        assertTrue(p.test("Abc"));
        assertTrue(p.test("Cde"));
        assertTrue(p.test("ABc"));
        assertFalse(p.test("SomethingFullyOff"));
    }

    @Test
    public void testOneOf() throws IllegalFilterExpressionException {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.OneOf);
        assertNotNull(efc);

        Predicate<String> p = efc.create("unit-test-null", "A,B;C|D");
        assertTrue(p.test("A"));
        assertTrue(p.test("B"));
        assertTrue(p.test("C"));
        assertTrue(p.test("D"));
        assertFalse(p.test("a"));
        assertFalse(p.test("b"));
        assertFalse(p.test("c"));
        assertFalse(p.test("d"));
        assertFalse(p.test("SomethingFullyOff"));
    }

    @Test
    public void testOneOf_i() throws IllegalFilterExpressionException {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.OneOfI);
        assertNotNull(efc);

        Predicate<String> p = efc.create("unit-test-null", "A,B;C|D");
        assertTrue(p.test("A"));
        assertTrue(p.test("B"));
        assertTrue(p.test("C"));
        assertTrue(p.test("D"));
        assertTrue(p.test("a"));
        assertTrue(p.test("b"));
        assertTrue(p.test("c"));
        assertTrue(p.test("d"));
        assertFalse(p.test("SomethingFullyOff"));
    }

    @Test
    public void testJson() throws IllegalFilterExpressionException {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.Json);
        assertNotNull(efc);

        Predicate<String> p = efc.create("unit-test-null", null);
        assertTrue(p.test("application/json"));
        assertTrue(p.test("application/json; charset=utf-8"));
        assertTrue(p.test("application/json; charset = utf-8"));
        assertTrue(p.test("application/json+hal"));
        assertTrue(p.test("application/json+hal+something-else"));

        assertFalse(p.test(null));
        assertFalse(p.test("application/gzip"));
    }

    @Test
    public void testDslExpressionNull() {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.DslExpression);
        assertNotNull(efc);

        try {
            efc.create("unit-test", null);
        } catch (IllegalFilterExpressionException ex) {
            assertEquals("unit-test", ex.getElementName());
            assertEquals("DslExpression requires a string expression", ex.getMessage());
            assertNull(ex.getExpression());
            return;
        }

        fail("Exception should have been thrown");
    }

    @Test
    public void testDslExpressionEq() throws IllegalFilterExpressionException {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.DslExpression);
        assertNotNull(efc);

        Predicate<String> p = efc.create("unit-test-null", "`theString`");
        assertTrue(p.test("theString"));
        assertFalse(p.test("TheString"));
    }

    @Test
    public void testDslExpressionEqI() throws IllegalFilterExpressionException {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.DslExpression);
        assertNotNull(efc);

        Predicate<String> p = efc.create("unit-test-null", "`theString`i");
        assertTrue(p.test("theString"));
        assertTrue(p.test("TheString"));
        assertFalse(p.test("SomethingCompletelyOff"));
    }

    @Test
    public void testDslExpressionOneOf() throws IllegalFilterExpressionException {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.DslExpression);
        assertNotNull(efc);

        Predicate<String> p = efc.create("unit-test-null", "`[A,B;C|D]`");
        assertTrue(p.test("A"));
        assertTrue(p.test("B"));
        assertTrue(p.test("C"));
        assertTrue(p.test("D"));
        assertFalse(p.test("E"));

        assertFalse(p.test("a"));
        assertFalse(p.test("b"));
        assertFalse(p.test("c"));
        assertFalse(p.test("d"));
        assertFalse(p.test("e"));

        assertFalse(p.test("DefinitelyWrongString"));
    }

    @Test
    public void testDslExpressionOneOfI() throws IllegalFilterExpressionException {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.DslExpression);
        assertNotNull(efc);

        Predicate<String> p = efc.create("unit-test-null", "`[A,B;C|D]`i");
        assertTrue(p.test("A"));
        assertTrue(p.test("B"));
        assertTrue(p.test("C"));
        assertTrue(p.test("D"));
        assertFalse(p.test("E"));

        assertTrue(p.test("a"));
        assertTrue(p.test("b"));
        assertTrue(p.test("c"));
        assertTrue(p.test("d"));
        assertFalse(p.test("e"));

        assertFalse(p.test("DefinitelyWrongString"));
    }

    @Test
    public void testDslExpressionRegex() throws IllegalFilterExpressionException {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.DslExpression);
        assertNotNull(efc);

        Predicate<String> p = efc.create("unit-test-null", "~/Abc|Cde/");
        assertTrue(p.test("Abc"));
        assertTrue(p.test("Cde"));
        assertFalse(p.test("Off"));

        assertFalse(p.test("aBC"));
        assertFalse(p.test("cDE"));
        assertFalse(p.test("Off"));
    }

    @Test
    public void testDslExpressionRegex_I() throws IllegalFilterExpressionException {
        ElementFilterCreator<String> efc = getFilterAlgorithm(String.class, StringFilterAlgorithms.DslExpression);
        assertNotNull(efc);

        Predicate<String> p = efc.create("unit-test-null", "~/Abc|Cde/i");
        assertTrue(p.test("Abc"));
        assertTrue(p.test("Cde"));
        assertFalse(p.test("Off"));

        assertTrue(p.test("aBC"));
        assertTrue(p.test("cDE"));
        assertFalse(p.test("Off"));
    }
}
