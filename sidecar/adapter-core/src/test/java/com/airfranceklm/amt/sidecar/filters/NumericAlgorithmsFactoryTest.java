package com.airfranceklm.amt.sidecar.filters;

import org.junit.Test;

import java.util.function.Predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NumericAlgorithmsFactoryTest {

    @Test
    public void testEqCollision() {
        Predicate<Integer> p = NumericAlgorithmsFactory.eq(45);
        Predicate<Integer> p1 = NumericAlgorithmsFactory.eq(65);

        assertFalse(p.test(44));
        assertTrue(p.test(45));
        assertFalse(p.test(46));

        assertFalse(p1.test(64));
        assertTrue(p1.test(65));
        assertFalse(p1.test(66));
    }

    @Test
    public void testEq() {
        Predicate<Long> p = NumericAlgorithmsFactory.eq(45L);
        assertFalse(p.test(44L));
        assertTrue(p.test(45L));
    }

    @Test
    public void testEq_int() {
        Predicate<Integer> p = NumericAlgorithmsFactory.eq(45);
        assertFalse(p.test(44));
        assertTrue(p.test(45));
    }

    @Test
    public void testLt() {
        Predicate<Long> p = NumericAlgorithmsFactory.lt(45L);
        assertTrue(p.test(44L));
        assertFalse(p.test(45L));
    }

    @Test
    public void testLt_int() {
        Predicate<Integer> p = NumericAlgorithmsFactory.lt(45);
        assertTrue(p.test(44));
        assertFalse(p.test(45));
    }

    @Test
    public void testLte() {
        Predicate<Long> p = NumericAlgorithmsFactory.lte(45L);
        assertTrue(p.test(44L));
        assertTrue(p.test(45L));
        assertFalse(p.test(46L));
    }

    @Test
    public void testLte_int() {
        Predicate<Integer> p = NumericAlgorithmsFactory.lte(45);
        assertTrue(p.test(44));
        assertTrue(p.test(45));
        assertFalse(p.test(46));
    }

    @Test
    public void testGt() {
        Predicate<Long> p = NumericAlgorithmsFactory.gt(45L);
        assertFalse(p.test(45L));
        assertTrue(p.test(46L));
    }

    @Test
    public void testGt_int() {
        Predicate<Integer> p = NumericAlgorithmsFactory.gt(45);
        assertFalse(p.test(45));
        assertTrue(p.test(46));
    }

    @Test
    public void testGte() {
        Predicate<Long> p = NumericAlgorithmsFactory.gte(46L);
        assertFalse(p.test(45L));
        assertTrue(p.test(46L));
        assertTrue(p.test(47L));
    }

    @Test
    public void testGte_int() {
        Predicate<Integer> p = NumericAlgorithmsFactory.gte(46);
        assertFalse(p.test(45));
        assertTrue(p.test(46));
        assertTrue(p.test(47));
    }

    @Test
    public void testRange() {
        Predicate<Long> p = NumericAlgorithmsFactory.range(30L, 50L);
        assertFalse(p.test(29L));
        assertTrue(p.test(30L));
        assertTrue(p.test(31L));
        assertTrue(p.test(49L));
        assertTrue(p.test(50L));
        assertFalse(p.test(51L));
    }

    @Test
    public void testRange_int() {
        Predicate<Integer> p = NumericAlgorithmsFactory.range(30, 50);
        testNumberRange(p, 30, 50);
    }

    @Test
    public void testHttpOk() {
        Predicate<Integer> p = NumericAlgorithmsFactory.httpOk();

        testNumberRange(p, 200, 399);
    }

    static void testNumberRange(Predicate<Integer> p, int minPositive, int maxPositive) {
        assertFalse(p.test(minPositive - 1));
        for (int i = minPositive; i <= maxPositive; i++) {
            assertTrue(String.format("Code %d", i), p.test(i));
        }

        assertFalse(p.test(maxPositive+1));
    }

    static void testNumberRange(Predicate<Long> p, long minPositive, long maxPositive) {
        assertFalse(p.test(minPositive - 1));
        for (long i = minPositive; i <= maxPositive; i++) {
            assertTrue(String.format("Code %d", i), p.test(i));
        }

        assertFalse(p.test(maxPositive+1));
    }

    @Test
    public void testHttpAuth() {
        testHttpAuthPredicate(NumericAlgorithmsFactory.httpAuth());
    }

    static void testHttpAuthPredicate(Predicate<Integer> p) {
        assertFalse(p.test(400));
        assertTrue(p.test(401));
        assertFalse(p.test(402));
        assertTrue(p.test(403));
        assertFalse(p.test(404));
    }

    @Test
    public void testHttpFunc() {
        testFunctionalHttpCodesPredicate(NumericAlgorithmsFactory.httpFunc());
    }

    static void testFunctionalHttpCodesPredicate(Predicate<Integer> p) {
        assertFalse(p.test(399));
        assertFalse(p.test(500));

        for (int i=400; i<500; i++) {
            switch (i) {
                case 401:
                case 403:
                    assertFalse(p.test(i));
                    break;
                default:
                    assertTrue(String.format("Code %d", i), p.test(i));
            }
        }
    }

    @Test
    public void testHttpFatal() {
        testNumberRange(NumericAlgorithmsFactory.httpFatal(), 500, 599);
    }
}
