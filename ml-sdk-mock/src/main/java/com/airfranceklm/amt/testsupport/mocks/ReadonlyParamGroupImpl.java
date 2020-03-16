package com.airfranceklm.amt.testsupport.mocks;

import com.mashery.http.ParamGroup;
import com.mashery.http.ParamIterator;

import java.util.*;

public class ReadonlyParamGroupImpl implements ParamGroup {

    Map<String,String> contents;

    public ReadonlyParamGroupImpl(Map<String, String> contents) {
        this.contents = contents != null ? contents: new HashMap<>();
    }

    @Override
    public boolean contains(String s) {
        return contents.containsKey(s);
    }

    @Override
    public String get(String s) {
        return contents.get(s);
    }

    @Override
    public ParamIterator iterator() {
        return new ParamsIteratorImpl();
    }

    class ParamsIteratorImpl implements ParamIterator {

        Iterator<String> backingIterator;
        String value = null;

        ParamsIteratorImpl() {
            this.backingIterator = contents.keySet().iterator();
        }

        @Override
        public void remove() throws IllegalStateException, UnsupportedOperationException {
            throw new UnsupportedOperationException("Should not be called.");
        }

        @Override
        public String value() throws IllegalStateException {
            return value;
        }

        @Override
        public boolean hasNext() {
            return backingIterator.hasNext();
        }

        @Override
        public String next() {
            if (backingIterator.hasNext()) {
                value = backingIterator.next();
            } else {
                value = null;
            }
            return value;
        }
    }
}
