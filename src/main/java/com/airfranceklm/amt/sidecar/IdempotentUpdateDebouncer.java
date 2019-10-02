package com.airfranceklm.amt.sidecar;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class IdempotentUpdateDebouncer extends LinkedHashMap<String, AtomicLong> {

    private int debounceMemory;
    private long bounceDuration;

    IdempotentUpdateDebouncer(int mem, TimeUnit to, int advance) {
        super(mem, 0.75f, true); // TODO: is the load factor chosen?
        debounceMemory = mem;
        bounceDuration = to.toMillis(advance);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, AtomicLong> eldest) {
        return size() > debounceMemory;
    }

    /**
     * Returns true if the key should be bounced
     * @param key String key to debounce.
     * @return true to debounce, false otherwise.
     */
    boolean debounce(String key) {
        AtomicLong al = get(key);

        // We've got no memory.
        if (al == null) {
            synchronized (this) {
                // Double guard for the race condition
                if (!containsKey(key)) {
                    al = new AtomicLong(System.currentTimeMillis());
                    put(key, al);
                    return false;
                } else {
                    // Just before we didn't have a key, and it's present now in the synchronized section
                    // of the code. This condition definitely needs to be de-bounced.
                    return true;
                }
            }
        }

        // If we are coming too quickly, then definitely debounce.
        final long exp = al.get();
        if (exp + bounceDuration > System.currentTimeMillis()) {
            return true;
        }

        // If we were able ot update the value, then this thread has won.
        // If the value has changed, then the it's debounce condition.
        return !al.compareAndSet(exp, System.currentTimeMillis());
    }
}
