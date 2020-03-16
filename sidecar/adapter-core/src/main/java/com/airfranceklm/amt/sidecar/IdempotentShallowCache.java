package com.airfranceklm.amt.sidecar;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shallow cache for the idempotent objects.
 */
public class IdempotentShallowCache extends LinkedHashMap<String, SidecarOutputCache> {

    private int maxSize;

    IdempotentShallowCache(int maxSize) {
        super(maxSize/3, .75f, true);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, SidecarOutputCache> eldest) {
        return size() > maxSize;
    }
}
