package com.airfranceklm.amt.sidecar.filters;

import com.airfranceklm.amt.sidecar.elements.ElementFilterCreator;
import com.airfranceklm.amt.sidecar.elements.IllegalFilterExpressionException;
import com.airfranceklm.amt.sidecar.elements.NumericAlgorithms;
import org.junit.Test;

import java.util.function.Predicate;

import static com.airfranceklm.amt.sidecar.elements.StandardElementsFactory.getFilterAlgorithm;
import static junit.framework.Assert.*;

/**
 * Tests that the creators are yielding the expected function if accessed via the
 * {@link com.airfranceklm.amt.sidecar.elements.StandardElementsFactory}.
 */
public class NumericAlgorithmsCreatorsTest {

    @Test
    public void testEQLong() throws IllegalFilterExpressionException {
        ElementFilterCreator<Long> efc = getFilterAlgorithm(Long.class, NumericAlgorithms.Eq);
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
    public void testEQInt() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.Eq);
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
    public void testLtLong() throws IllegalFilterExpressionException {
        ElementFilterCreator<Long> efc = getFilterAlgorithm(Long.class, NumericAlgorithms.Lt);
        assertNotNull(efc);

        Predicate<Long> p = efc.create("unit-test", "34");
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
    public void testLtInt() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.Lt);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", "34");
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
    public void testLteLong() throws IllegalFilterExpressionException {
        ElementFilterCreator<Long> efc = getFilterAlgorithm(Long.class, NumericAlgorithms.Lte);
        assertNotNull(efc);

        Predicate<Long> p = efc.create("unit-test", "34");
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
    public void testLteInt() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.Lte);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", "34");
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
    public void testGtLong() throws IllegalFilterExpressionException {
        ElementFilterCreator<Long> efc = getFilterAlgorithm(Long.class, NumericAlgorithms.Gt);
        assertNotNull(efc);

        Predicate<Long> p = efc.create("unit-test", "34");
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
    public void testGtInt() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.Gt);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", "34");
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
    public void testGteLong() throws IllegalFilterExpressionException {
        ElementFilterCreator<Long> efc = getFilterAlgorithm(Long.class, NumericAlgorithms.Gte);
        assertNotNull(efc);

        Predicate<Long> p = efc.create("unit-test", "34");
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
    public void testGteInt() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.Gte);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", "34");
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
    public void testRangeInteger() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.Range);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", "34..45");
        assertFalse(p.test(33));
        assertTrue(p.test(34));
        assertTrue(p.test(35));
        assertTrue(p.test(44));
        assertTrue(p.test(45));
        assertFalse(p.test(46));

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
    public void testRangeLong() throws IllegalFilterExpressionException {
        ElementFilterCreator<Long> efc = getFilterAlgorithm(Long.class, NumericAlgorithms.Range);
        assertNotNull(efc);

        Predicate<Long> p = efc.create("unit-test", "34..45");
        assertFalse(p.test(33L));
        assertTrue(p.test(34L));
        assertTrue(p.test(35L));
        assertTrue(p.test(44L));
        assertTrue(p.test(45L));
        assertFalse(p.test(46L));

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
    public void testHttpOk() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.HttpOk);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", null);
        assertFalse(p.test(199));
        for (int i = 200; i < 400; i++) {
            assertTrue(String.format("Code %d", i), p.test(i));
        }
        assertFalse(p.test(400));
    }

    @Test
    public void testHttpAuth() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.HttpAuth);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", null);
        assertFalse(p.test(400));
        assertTrue(p.test(401));
        assertFalse(p.test(402));
        assertTrue(p.test(403));
        assertFalse(p.test(404));
    }

    @Test
    public void testHttpFunc() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.HttpFunc);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", null);
        assertFalse(p.test(399));
        for (int i = 400; i < 500; i++) {
            switch (i) {
                case 401:
                case 403:
                    assertFalse(p.test(i));
                    break;
                default:
                    assertTrue(p.test(i));
            }
        }
    }

    @Test
    public void testHttpFatal() throws IllegalFilterExpressionException {
        ElementFilterCreator<Integer> efc = getFilterAlgorithm(Integer.class, NumericAlgorithms.HttpFatal);
        assertNotNull(efc);

        Predicate<Integer> p = efc.create("unit-test", null);
        assertFalse(p.test(499));
        for (int i = 500; i < 600; i++) {
            assertTrue(p.test(i));
        }
    }

}
