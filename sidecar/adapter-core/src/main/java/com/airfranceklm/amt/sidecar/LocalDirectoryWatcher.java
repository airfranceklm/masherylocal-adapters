package com.airfranceklm.amt.sidecar;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.nio.file.StandardWatchEventKinds.*;

@Slf4j
public class LocalDirectoryWatcher {

    private File root;
    private Path rootPath;

    private WatchService watcher;
    private WatchKey watchKey;
    private Thread watcherThread;

    private Consumer<File> removedConsumer;
    private Consumer<File> modifiedConsumer;

    private boolean watching = false;

    private Predicate<String> expectedNames;

    public LocalDirectoryWatcher(File root) {
        this.root = root;
    }

    void startWatching() {
        try {
            watcher = FileSystems.getDefault().newWatchService();
            rootPath = root.toPath();
            watchKey = rootPath.register(watcher,
                    ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

            watching = true;
            watcherThread = new Thread(new LocalDirectoryWatcher.WatcherRunnable());
            watcherThread.start();
        } catch (IOException e) {
            log.error(String.format("Watching for the service could not be started.: %s", e.getMessage()), e);
        }
    }

    public void takeFileMatching(Predicate<String> p) {
        this.expectedNames = p;
    }

    public boolean isWatching() {
        return watching;
    }

    void stopWatching() {
        if (watchKey != null) {
            watchKey.cancel();
        }

        watching = false;
        if (watcherThread != null && watcherThread.isAlive()) {
            watcherThread.interrupt();

            try {
                watcherThread.join(TimeUnit.SECONDS.toMillis(10));
            } catch (InterruptedException ex) {
                // Do nothing.
            }
        }


        try {
            watcher.close();
        } catch (IOException ex) {
            log.error(String.format("Failure during attempting to close the watcher service: %s.", ex.getMessage()), ex);
        }
    }

    void onFileRemoved(Consumer<File> c) {
        this.removedConsumer = c;
    }

    void onFileModified(Consumer<File> c) {
        this.modifiedConsumer = c;
    }

    class WatcherRunnable implements Runnable {

        @Override
        public void run() {
            Map<String,Long> debouncer = new HashMap<>();

            while (watching) {

                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException x) {
                    log.warn(String.format("Wait for the file system changes has terminated: %s", x.getMessage()), x);
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == OVERFLOW) {
                        continue;
                    }

                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path touchedChild = ev.context();

                    final String fileName = touchedChild.toString();

                    // Some files that are placed in the directory could be illegible, for example,
                    // vi swap files. These need to be filtered out. The filtering is based on the
                    // name where the caller has to supply the predicate as to which names are expected.
                    if (expectedNames != null && !expectedNames.test(fileName)) {
                        continue;
                    }

                    final long now = System.currentTimeMillis();

                    if (ev.kind() == ENTRY_MODIFY) {
                        long refTime = debouncer.computeIfAbsent(fileName, (mKey) -> 0L);
                        if (now - refTime < 2500) {
                            continue;
                        }
                    }

                    debouncer.put(fileName, now);

                    Path p = rootPath.resolve(touchedChild);
                    File f = p.toFile();

                    log.warn(String.format("File %s has been modified, reload required", f.getAbsolutePath()));

                    if (!f.exists()) {
                        log.warn(String.format("Dropped local information from %s", f.getAbsolutePath()));
                        if (removedConsumer != null) {
                            removedConsumer.accept(f);
                        }
                    } else {
                        log.warn(String.format("-> Starting re-loaded local information from %s", f.getAbsolutePath()));
                        if (modifiedConsumer != null) {
                            modifiedConsumer.accept(f);
                        }
                        log.warn(String.format("<- Finished re-loading local information from %s", f.getAbsolutePath()));
                    }
                }

                key.reset();
            }
        }
    }
}
