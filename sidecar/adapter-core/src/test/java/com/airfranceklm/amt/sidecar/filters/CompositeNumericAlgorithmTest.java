package com.airfranceklm.amt.sidecar.filters;

import com.airfranceklm.amt.sidecar.elements.ElementFilterCreator;
import com.airfranceklm.amt.sidecar.elements.IllegalFilterExpressionException;
import com.airfranceklm.amt.sidecar.elements.NumericAlgorithms;
import org.junit.Test;

import java.util.function.Predicate;

import static com.airfranceklm.amt.sidecar.elements.StandardElementsFactory.getFilterAlgorithm;
import static com.airfranceklm.amt.sidecar.filters.NumericAlgorithmsFactoryTest.testNumberRange;
import static junit.framework.Assert.*;

public class CompositeNumericAlgorithmTest {

    @Test
    public void testCompositeEq() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", "34");
        assertFalse(p.test(33));
        assertTrue(p.test(34));
        assertFalse(p.test(35));

        // Negative

        try {
            efc.create("unit-test", "invalid");
        } catch (IllegalFilterExpressionException ex) {
            assertEquals("invalid", ex.getExpression());
            assertEquals("unit-test", ex.getElementName());

            return;
        }

        fail("Exception should have been thrown");
    }

    @Test
    public void testCompositeGt() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", ">34");
        assertFalse(p.test(33));
        assertFalse(p.test(34));
        assertTrue(p.test(35));

        // Negative

        try {
            efc.create("unit-test", "invalid");
        } catch (IllegalFilterExpressionException ex) {
            assertEquals("invalid", ex.getExpression());
            assertEquals("unit-test", ex.getElementName());

            return;
        }

        fail("Exception should have been thrown");
    }

    @Test
    public void testCompositeGte() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", ">=34");
        assertFalse(p.test(33));
        assertTrue(p.test(34));
        assertTrue(p.test(35));

        // Negative

        try {
            efc.create("unit-test", "invalid");
        } catch (IllegalFilterExpressionException ex) {
            assertEquals("invalid", ex.getExpression());
            assertEquals("unit-test", ex.getElementName());

            return;
        }

        fail("Exception should have been thrown");
    }

    @Test
    public void testCompositeLte() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", "<=34");
        assertTrue(p.test(33));
        assertTrue(p.test(34));
        assertFalse(p.test(35));

        // Negative

        try {
            efc.create("unit-test", "invalid");
        } catch (IllegalFilterExpressionException ex) {
            assertEquals("invalid", ex.getExpression());
            assertEquals("unit-test", ex.getElementName());

            return;
        }

        fail("Exception should have been thrown");
    }

    @Test
    public void testCompositeLt() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", "<34");
        assertTrue(p.test(33));
        assertFalse(p.test(34));
        assertFalse(p.test(35));

        // Negative

        try {
            efc.create("unit-test", "invalid");
        } catch (IllegalFilterExpressionException ex) {
            assertEquals("invalid", ex.getExpression());
            assertEquals("unit-test", ex.getElementName());

            return;
        }

        fail("Exception should have been thrown");
    }

    @Test
    public void testCompositeRange() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", "34..57");
        assertFalse(p.test(33));
        for (int i=34; i<=57; i++) {
            assertTrue(p.test(i));
        }
        assertFalse(p.test(58));

        // Negative

        try {
            efc.create("unit-test", "invalid");
        } catch (IllegalFilterExpressionException ex) {
            assertEquals("invalid", ex.getExpression());
            assertEquals("unit-test", ex.getElementName());

            return;
        }

        fail("Exception should have been thrown");
    }

    @Test
    public void testCompositeHttpOk() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", "httpOk");
        NumericAlgorithmsFactoryTest.testNumberRange(p, 200, 399);

        // Negative

        try {
            efc.create("unit-test", "invalid");
        } catch (IllegalFilterExpressionException ex) {
            assertEquals("invalid", ex.getExpression());
            assertEquals("unit-test", ex.getElementName());

            return;
        }

        fail("Exception should have been thrown");
    }

    @Test
    public void testCompositeHttpAuth() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", "httpAuth");
        NumericAlgorithmsFactoryTest.testHttpAuthPredicate(p);

        // Negative

        try {
            efc.create("unit-test", "invalid");
        } catch (IllegalFilterExpressionException ex) {
            assertEquals("invalid", ex.getExpression());
            assertEquals("unit-test", ex.getElementName());

            return;
        }

        fail("Exception should have been thrown");
    }

    @Test
    public void testCompositeHttpFunc() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", "httpFunc");
        NumericAlgorithmsFactoryTest.testFunctionalHttpCodesPredicate(p);

        // Negative

        try {
            efc.create("unit-test", "invalid");
        } catch (IllegalFilterExpressionException ex) {
            assertEquals("invalid", ex.getExpression());
            assertEquals("unit-test", ex.getElementName());

            return;
        }

        fail("Exception should have been thrown");
    }

    @Test
    public void testCompositeHttpFatal() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", "httpFatal");
        NumericAlgorithmsFactoryTest.testNumberRange(p, 500, 599);

        // Negative

        try {
            efc.create("unit-test", "invalid");
        } catch (IllegalFilterExpressionException ex) {
            assertEquals("invalid", ex.getExpression());
            assertEquals("unit-test", ex.getElementName());

            return;
        }

        fail("Exception should have been thrown");
    }

    @Test
    public void testCompositionOfExpressions() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", "<25,34,50..70,httpOk");
        for (int i=-10; i<25; i++) {
            assertTrue(p.test(i));
        }
        assertFalse(p.test(25));

        NumericAlgorithmsFactoryTest.testNumberRange(p, 34, 34);
        NumericAlgorithmsFactoryTest.testNumberRange(p, 50, 70);
        NumericAlgorithmsFactoryTest.testNumberRange(p, 200, 399);

        try {
            efc.create("unit-test", "<25,50..70,httpOk,invalid");
        } catch (IllegalFilterExpressionException ex) {
            assertEquals("<25,50..70,httpOk,invalid", ex.getExpression());
            assertEquals("Unknown expression 'invalid'", ex.getMessage());
            assertEquals("unit-test", ex.getElementName());

            return;
        }

        fail("Exception should have been thrown and caught in the code above");

    }
}
