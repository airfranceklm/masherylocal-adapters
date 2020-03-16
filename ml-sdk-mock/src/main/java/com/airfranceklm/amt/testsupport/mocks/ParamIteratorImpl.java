package com.airfranceklm.amt.testsupport.mocks;

import com.mashery.http.ParamIterator;

import java.util.Iterator;

import static org.junit.Assert.fail;

class ParamIteratorImpl implements ParamIterator {

    Iterator<String> source;
    String value;

    public ParamIteratorImpl(Iterator<String> source) {
        this.source = source;
    }

    @Override
    public void remove() throws IllegalStateException, UnsupportedOperationException {
        source.remove();
    }

    @Override
    public String value() throws IllegalStateException {
        if (value == null) {
            if (hasNext()) {
                value = next();
            } else {
                fail("Unexpected fail in call to next() of the iterator");
            }
            return value;
        } else {
            return value;
        }
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
