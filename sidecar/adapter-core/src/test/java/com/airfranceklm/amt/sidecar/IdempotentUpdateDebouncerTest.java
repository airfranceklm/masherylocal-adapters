package com.airfranceklm.amt.sidecar;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertEquals;

public class IdempotentUpdateDebouncerTest {

    @Test
    public void testUpdateBouncer() throws InterruptedException {
        IdempotentUpdateDebouncer bouncer = new IdempotentUpdateDebouncer(10, TimeUnit.SECONDS, 5);

        final String key = "abcdefghijklmnop";

        long start = System.currentTimeMillis();
        long end = start + TimeUnit.SECONDS.toMillis(6);

        int allowed = 0;

        while (true) {
            if (!bouncer.shouldBounce(key)) {
                allowed++;
            }

            if (System.currentTimeMillis() > end) {
                break;
            } else {
                Thread.sleep(1);
            }
        }

        assertEquals(2, allowed);
    }

    @Test
    public void testMultithreadedBounce() throws InterruptedException {
        IdempotentUpdateDebouncer bouncer = new IdempotentUpdateDebouncer(10, TimeUnit.SECONDS, 5);

        final String key = "abcdefghijklmnop";
        final AtomicInteger counter = new AtomicInteger(0);

        ExecutorService es = Executors.newCachedThreadPool();

        long start = System.currentTimeMillis();
        long end = start + TimeUnit.SECONDS.toMillis(6);

        while (true) {
            es.submit(() -> {
                // Add some randomness
                try {
                    Thread.sleep(Math.round(100 * Math.random()));
                } catch (InterruptedException ex) {
                    // Do nothing.
                }
                if (!bouncer.shouldBounce(key)) {
                    counter.incrementAndGet();
                }
            });

            if (System.currentTimeMillis() > end) {
                break;
            }
        }

        es.shutdown();
        es.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(2, counter.get());
    }
}
