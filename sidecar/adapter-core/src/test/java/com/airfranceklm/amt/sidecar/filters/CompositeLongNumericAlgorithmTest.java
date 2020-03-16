package com.airfranceklm.amt.sidecar.filters;

import com.airfranceklm.amt.sidecar.elements.ElementFilterCreator;
import com.airfranceklm.amt.sidecar.elements.IllegalFilterExpressionException;
import com.airfranceklm.amt.sidecar.elements.NumericAlgorithms;
import org.junit.Test;

import java.util.function.Predicate;

import static com.airfranceklm.amt.sidecar.elements.StandardElementsFactory.getFilterAlgorithm;
import static com.airfranceklm.amt.sidecar.filters.NumericAlgorithmsFactoryTest.testNumberRange;
import static junit.framework.Assert.*;

public class CompositeLongNumericAlgorithmTest {

    @Test
    public void testCompositeEq() throws IllegalFilterExpressionException {
        ElementFilterCreator<Long> efc = getFilterAlgorithm(Long.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Long> p = efc.create("unit-test", "34");
        assertFalse(p.test(33L));
        assertTrue(p.test(34L));
        assertFalse(p.test(35L));

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
        ElementFilterCreator<Long> efc = getFilterAlgorithm(Long.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Long> p = efc.create("unit-test", ">34");
        assertFalse(p.test(33L));
        assertFalse(p.test(34L));
        assertTrue(p.test(35L));

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
        ElementFilterCreator<Long> efc = getFilterAlgorithm(Long.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Long> p = efc.create("unit-test", ">=34");
        assertFalse(p.test(33L));
        assertTrue(p.test(34L));
        assertTrue(p.test(35L));

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
        ElementFilterCreator<Long> efc = getFilterAlgorithm(Long.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Long> p = efc.create("unit-test", "<=34");
        assertTrue(p.test(33L));
        assertTrue(p.test(34L));
        assertFalse(p.test(35L));

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
        ElementFilterCreator<Long> efc = getFilterAlgorithm(Long.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Long> p = efc.create("unit-test", "<34");
        assertTrue(p.test(33L));
        assertFalse(p.test(34L));
        assertFalse(p.test(35L));

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
        ElementFilterCreator<Long> efc = getFilterAlgorithm(Long.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Long> p = efc.create("unit-test", "34..57");
        assertFalse(p.test(33L));
        for (long i=34; i<=57; i++) {
            assertTrue(p.test(i));
        }
        assertFalse(p.test(58L));

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
        ElementFilterCreator<Long> efc = getFilterAlgorithm(Long.class, NumericAlgorithms.Composite);
        assertNotNull(efc);

        Predicate<Long> p = efc.create("unit-test", "<25,34,50..70");
        for (long i=-10; i<25; i++) {
            assertTrue(p.test(i));
        }
        assertFalse(p.test(25L));

        testNumberRange(p, 34, 34);
        testNumberRange(p, 50, 70);

        try {
            efc.create("unit-test", "<25,50..70,httpOk");
        } catch (IllegalFilterExpressionException ex) {
            assertEquals("<25,50..70,httpOk", ex.getExpression());
            assertEquals("Unknown expression 'httpOk' for Long", ex.getMessage());
            assertEquals("unit-test", ex.getElementName());

            return;
        }

        fail("Exception should have been thrown and caught in the code above");

    }
}
