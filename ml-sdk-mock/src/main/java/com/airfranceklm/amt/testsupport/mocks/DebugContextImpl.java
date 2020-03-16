package com.airfranceklm.amt.testsupport.mocks;

import com.mashery.trafficmanager.debug.DebugContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DebugContextImpl implements DebugContext {
    Map<String, Serializable> entries = new HashMap<>();

    public DebugContextImpl(Map<String, Serializable> starter) {
        if (starter != null) {
            entries.putAll(starter);
        }
    }

    @Override
    public Serializable getEntry(String s) {
        return entries.get(s);
    }

    @Override
    public void logEntry(String s, Serializable serializable) {
        this.entries.put(s, serializable);

    }

    @Override
    public void clearEntries() {
        this.entries.clear();
    }

    @Override
    public Serializable removeEntry(String s) {
        return this.entries.remove(s);
    }
}
