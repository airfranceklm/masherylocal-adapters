package com.airfranceklm.amt.testsupport.mocks;

import com.mashery.http.HTTPHeaderElements;
import com.mashery.http.MutableHTTPHeaderIterator;
import com.mashery.http.MutableHTTPHeaders;
import lombok.Builder;
import lombok.Singular;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import static com.airfranceklm.amt.testsupport.Mocks.asTreeMap;
import static org.junit.Assert.fail;


public class MutableHTTPHeadersImpl implements MutableHTTPHeaders {


    TreeMap<String,String> entries;

    @Builder
    public MutableHTTPHeadersImpl(@Singular Map<String, String> entries) {
        this.entries = entries != null ? asTreeMap(entries) : new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    @Override
    public MutableHTTPHeaderIterator iterator() {
        return new MutableHTTPHeadersIteratorImpl();
    }

    @Override
    public HTTPHeaderElements getElements(String s) {
        return null;
    }

    @Override
    public void set(String s, String s1) throws IllegalArgumentException {
        entries.put(s, s1);
    }

    @Override
    public void add(String s, String s1) throws IllegalArgumentException {
        set(s, s1);
        // Maybe this is not correct
    }

    @Override
    public void remove(String s) {
        entries.remove(s);
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public boolean contains(String s) {
        return entries.containsKey(s);
    }

    @Override
    public String get(String s) {
        Object o = entries.get(s);
        if (o instanceof String) {
            return (String)o;
        } else if (o instanceof HTTPHeaderElements) {
            return ((HTTPHeaderElements)o).iterator().next();
        } else {
            return null;
        }
    }

    class MutableHTTPHeadersIteratorImpl implements MutableHTTPHeaderIterator {

        Iterator<String> source;
        String value;

        public MutableHTTPHeadersIteratorImpl() {
            source = entries.keySet().iterator();
        }

        @Override
        public HTTPHeaderElements elements() throws IllegalStateException {
            throw new UnsupportedOperationException("not supported");
        }

        @Override
        public void remove() throws IllegalStateException {
            source.remove();
        }

        @Override
        public void set(String s, String s1) throws IllegalStateException, IllegalArgumentException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(String s, String s1) throws IllegalArgumentException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String value() throws IllegalStateException {
            if (value == null) {
                if (hasNext()) {
                    return next();
                } else {
                    fail("Iterator is empty, can't get a value");
                }
            }

            return value;
        }

        @Override
        public boolean hasNext() {
            return source.hasNext();
        }

        @Override
        public String next() {
            value = source.next();
            return value;
        }
    }
}
