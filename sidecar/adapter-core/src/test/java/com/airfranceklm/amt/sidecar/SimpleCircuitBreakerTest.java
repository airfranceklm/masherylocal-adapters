package com.airfranceklm.amt.sidecar;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class SimpleCircuitBreakerTest {

    @Test
    public void testCleanupAfterRecovery() {
        final String endpointId = "alkdjfladsjflkdsa";

        SimpleCircuitBreaker scb = new SimpleCircuitBreaker();
        nTimes(30, scb, (c) -> c.trackError(endpointId));
        assertFalse(scb.isOpen(endpointId));
        assertEquals(1, scb.getFaultsCount());

        nTimes(29, scb, (c) -> {
            c.trackSuccess(endpointId);
            assertEquals(1, c.getFaultsCount());
            assertFalse(c.isOpen(endpointId));
        });

        scb.trackSuccess(endpointId);
        assertEquals(0, scb.getFaultsCount());
        assertFalse(scb.isOpen(endpointId));
    }

    @Test
    public void testSuccessfulCustomization() {
        SimpleCircuitBreaker scb = new SimpleCircuitBreaker(5, TimeUnit.SECONDS, 6);
        assertEquals(5, scb.getCircuitOpenThreshold());
        assertEquals(6000, scb.getCircuitOpenTime());
    }

    @Test
    public void testFailingOnNegativeThreshold() {
        try {
            new SimpleCircuitBreaker(-1, TimeUnit.MINUTES, 1);
        } catch (IllegalArgumentException ex) {
            assertEquals("Threshold must be a positive value", ex.getMessage());
            return;
        }
        fail("Exception must be thrown");
    }

    @Test
    public void testFailingOnNegativeDuration() {
        try {
            new SimpleCircuitBreaker(TimeUnit.MINUTES, -1);
        } catch (IllegalArgumentException ex) {
            assertEquals("Duration must be a positive value", ex.getMessage());
            return;
        }
        fail("Exception must be thrown");
    }

    @Test
    public void testDefaultInitialization() {
        SimpleCircuitBreaker scb = new SimpleCircuitBreaker();
        assertEquals(50, scb.getCircuitOpenThreshold());
        assertEquals(TimeUnit.SECONDS.toMillis(30), scb.getCircuitOpenTime());
    }

    @Test
    public void testClosedOnInit() {
        SimpleCircuitBreaker scb = new SimpleCircuitBreaker();
        assertFalse(scb.isOpen("RandomEndpoint"));
    }

    @Test
    public void testRemainsOpenOnIntermittent() {
        final String endpointId = "alkdjflkdsajfa";

        SimpleCircuitBreaker scb = new SimpleCircuitBreaker();
        nTimes(20, scb, (c) -> c.trackError(endpointId));

        for (int i=0; i<100; i++) {
            nTimes(5, scb, (c) -> {
                c.trackError(endpointId);
                assertFalse(c.isOpen(endpointId));
            });

            nTimes(5, scb, (c) -> {
                c.trackSuccess(endpointId);
                assertFalse(c.isOpen(endpointId));
            });
        }
    }

    @Test
    public void testClosesAfter50Consecutive() {
        final String endpointId = "alkdjflkdsajfa";

        SimpleCircuitBreaker scb = new SimpleCircuitBreaker();
        nTimes(49, scb, (c) -> {
            c.trackError(endpointId);
            assertFalse(c.isOpen(endpointId));
        });

        scb.trackError(endpointId);
        assertTrue(scb.isOpen(endpointId));
    }

    @Test
    public void testRemainsClosedFor30Seconds() throws InterruptedException {
        final String endpointId = "alkdjflkdsajfa";

        final TimeUnit tu = TimeUnit.SECONDS;
        final int tuVal = 5;

        SimpleCircuitBreaker scb = new SimpleCircuitBreaker(tu, tuVal);
        nTimes(49, scb, (c) -> {
            c.trackError(endpointId);
            assertFalse(c.isOpen(endpointId));
        });

        scb.trackError(endpointId);
        assertTrue(scb.isOpen(endpointId));
        assertEquals(1, scb.getFaultsCount());

        long exp = System.currentTimeMillis() + tu.toMillis(tuVal);
        while (System.currentTimeMillis() < exp) {
            assertTrue(scb.isOpen(endpointId));
            assertEquals(1, scb.getFaultsCount());
            Thread.sleep(10);
        }

        assertFalse(scb.isOpen(endpointId));
        assertEquals(0, scb.getFaultsCount());
    }

    private void nTimes(int count, SimpleCircuitBreaker scb, Consumer<SimpleCircuitBreaker> c) {
        for (int i=0; i<count; i++) {
            c.accept(scb);
        }
    }
}
