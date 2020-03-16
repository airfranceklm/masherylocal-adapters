package com.airfranceklm.amt.sidecar;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocalDirectoryWatcherTest {

    @Test @Ignore("Ignore for automatic runnning")
    public void interactiveTest() throws InterruptedException {
        LocalDirectoryWatcher watcher = new LocalDirectoryWatcher(new File("c:/work/watch"));
        AtomicInteger counter = new AtomicInteger();

        watcher.takeFileMatching((s) -> s.endsWith(".txt"));

        watcher.onFileModified((f) -> {
            System.out.println(String.format("%d. Modified: %s (%d)", counter.incrementAndGet(), f.getAbsolutePath(), f.lastModified()));
        });

        watcher.onFileRemoved((f) -> {
            System.out.println(String.format("Removed: %s", f.getAbsolutePath()));
        });

        watcher.startWatching();
        assertTrue(watcher.isWatching());

        Thread.sleep(TimeUnit.SECONDS.toMillis(60));
        watcher.stopWatching();
        assertFalse(watcher.isWatching());
    }
}
