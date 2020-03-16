package com.airfranceklm.amt.testsupport.mocks;

import com.mashery.trafficmanager.cache.Cache;
import com.mashery.trafficmanager.cache.CacheException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CacheImpl implements Cache {

    Map<String, Serializable> contents;

    public CacheImpl(Map<String, Serializable> contents) {
        this.contents = contents != null ? new HashMap<>(contents) : new HashMap<>();
    }

    @Override
    public Object get(String s) {
        return contents.get(s);
    }

    @Override
    public Object get(ClassLoader classLoader, String s) {
        return contents.get(s);
    }

    @Override
    public void put(String s, Object o, int i) throws CacheException {
        if (o instanceof Serializable) {
            contents.put(s, (Serializable) o);
        } else {
            throw new CacheException("Not a serializable object");
        }
    }
}
