package com.airfranceklm.amt.testsupport;

import com.mashery.http.*;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * Helper that helps building the HTTP Headers.
 */
class HTTPHeadersHelper {

    private EasyMockSupport owner;
    private Map<String, String> sourceData;
    private MutableHTTPHeadersImpl delegate;
    private MutableHTTPHeaders mutableMock;

    HTTPHeadersHelper(EasyMockSupport owner, Map<String, String> pStartingSet) {
        this.owner = owner;
        this.sourceData = new HashMap<>();
        if (pStartingSet != null) {
            pStartingSet.forEach((k, v) -> {
                this.sourceData.put(k.toLowerCase(), v);
            });
        }
        this.delegate = new MutableHTTPHeadersImpl(this.sourceData);
    }

    HTTPHeaders createReadonlyHeaders() {
        HTTPHeaders mockHeaders = owner.createMock(HTTPHeaders.class);

        HeaderGroupIterable answerHelper = new HeaderGroupIterable(sourceData);

        expect(mockHeaders.iterator()).andAnswer(answerHelper.getIteratorAnswer()).anyTimes();

        // In some cases, the requester may send no headers. If there are no headers included,
        // there will also be no expectation of the method to be called here.
        if (sourceData != null && sourceData.size() > 0) {
            sourceData.forEach((key, value) -> {
                expect(mockHeaders.get(key)).andReturn(getStringValueOf(value)).anyTimes();
                expect(mockHeaders.contains(key)).andReturn(true).anyTimes();

                // This is a simple support for header case insensitivity
                if (!key.equals(key.toLowerCase())) {
                    expect(mockHeaders.get(key.toLowerCase())).andReturn(getStringValueOf(value)).anyTimes();
                    expect(mockHeaders.contains(key.toLowerCase())).andReturn(true).anyTimes();
                }
            });
        }

        // A request to the missing header will return null
        expect(mockHeaders.get(anyString())).andReturn(null).anyTimes();
        expect(mockHeaders.contains(anyString())).andReturn(false).anyTimes();

        return mockHeaders;
    }

    class MutableHTTPHeadersImpl implements MutableHTTPHeaders {
        private Map<String,String> data;

        MutableHTTPHeadersImpl(Map<String, String> startingSet) {
            this.data = new HashMap<>();
             if (startingSet != null) {
                 this.data.putAll(startingSet);
             }
        }

        @Override
        public MutableHTTPHeaderIterator iterator() {
            return new MutableHTTPHeadersIteratorImpl(this.data.entrySet().iterator());
        }

        @Override
        public HTTPHeaderElements getElements(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(String s, String s1) throws IllegalArgumentException {
            this.data.put(s.toLowerCase(), s1);
        }

        @Override
        public void add(String s, String s1) throws IllegalArgumentException {
            this.data.put(s.toLowerCase(), s1);
        }

        @Override
        public void remove(String s) {
            this.data.remove(s);
        }

        @Override
        public void clear() {
            this.data.clear();
        }

        @Override
        public boolean contains(String s) {
            return this.data.containsKey(s.toLowerCase());
        }

        @Override
        public String get(String s) {
            return this.data.get(s.toLowerCase());
        }
    }

    void initAccumulatingMock() {
       mutableMock = owner.createMock(MutableHTTPHeaders.class);
       expect(mutableMock.get(anyString())).andDelegateTo(delegate).anyTimes();
       expect(mutableMock.iterator()).andDelegateTo(delegate).anyTimes();
    }

    void accumulateOnce(String h, String value) {
        mutableMock.set(h, value);
        expectLastCall().andDelegateTo(delegate).once();
    }

    void dropOnce(String h) {
        mutableMock.remove(h);
        expectLastCall().once();
    }

    MutableHTTPHeaders getAccumulatedMock() {
        return mutableMock;
    }

    /**
     * A mock that setups up mutable HTTP headers.
     * @return
     */
    MutableHTTPHeaders createMutableHeadersMock() {
        MutableHTTPHeaders mockHeaders = owner.createMock(MutableHTTPHeaders.class);

        HeaderGroupIterable answerHelper = new HeaderGroupIterable(sourceData);

        expect(mockHeaders.iterator()).andAnswer(answerHelper.getMutableIteratorAnswer()).anyTimes();
        sourceData.forEach((key, value) -> {
            String v = getStringValueOf(value);
            expect(mockHeaders.get(key)).andReturn(v).anyTimes();
            expect(mockHeaders.contains(key)).andReturn(true).anyTimes();

            if (!key.equals(key.toLowerCase())) {
                expect(mockHeaders.get(key.toLowerCase())).andReturn(v).anyTimes();
                expect(mockHeaders.contains(key.toLowerCase())).andReturn(true).anyTimes();
            }
        });

        expect(mockHeaders.get(anyString())).andReturn(null).anyTimes();
        expect(mockHeaders.contains(anyString())).andReturn(false).anyTimes();

        return mockHeaders;

    }

    class HeaderGroupIterable {
        Map<String, String> backingHeaders;

        HeaderGroupIterable(Map<String, String> backingHeaders) {
            this.backingHeaders = backingHeaders;
        }

        IAnswer<HTTPHeaderIterator> getIteratorAnswer() {
            return new HTTPHeaderIteratorAnswer(HeaderGroupIterable.this);
        }

        IAnswer<MutableHTTPHeaderIterator> getMutableIteratorAnswer() {
            return new MutableHTTPHeaderIteratorAnswer(HeaderGroupIterable.this);
        }
    }

    class HTTPHeaderIteratorAnswer implements IAnswer<HTTPHeaderIterator> {
        HeaderGroupIterable scope;

        HTTPHeaderIteratorAnswer(HeaderGroupIterable scope) {
            this.scope = scope;
        }

        @Override
        public HTTPHeaderIterator answer() throws Throwable {
            return new HTTPHeaderIteratorImpl(this.scope.backingHeaders.entrySet().iterator());
        }
    }

    class MutableHTTPHeaderIteratorAnswer implements IAnswer<MutableHTTPHeaderIterator> {
        HeaderGroupIterable scope;

        public MutableHTTPHeaderIteratorAnswer(HeaderGroupIterable scope) {
            this.scope = scope;
        }

        @Override
        public MutableHTTPHeaderIterator answer() throws Throwable {
            return new MutableHTTPHeadersIteratorImpl(this.scope.backingHeaders.entrySet().iterator());
        }
    }

    class BaseHeadersIterator {
        Iterator<Map.Entry<String, String>> underlyingIterator;

        String currentValue = null;

        BaseHeadersIterator(Iterator<Map.Entry<String, String>> underlyingIterator) {
            this.underlyingIterator = underlyingIterator;
        }

        public HTTPHeaderElements elements() throws IllegalStateException {
            // What's this? How this is supposed to work?
            return null;
        }

        public void remove() throws IllegalStateException, UnsupportedOperationException {
            throw new UnsupportedOperationException("Removals must be explicitly subbed");
        }

        public String value() throws IllegalStateException {
            return currentValue;
        }

        public boolean hasNext() {
            return underlyingIterator.hasNext();
        }

        public String next() {
            Map.Entry<String, String> next = underlyingIterator.next();
            if (next != null) {
                currentValue = next.getKey();
            } else {
                currentValue = null;
            }

            return currentValue;
        }
    }

    class HTTPHeaderIteratorImpl extends BaseHeadersIterator implements HTTPHeaderIterator {

        HTTPHeaderIteratorImpl(Iterator<Map.Entry<String, String>> underlyingIterator) {
            super(underlyingIterator);
        }

    }

    class MutableHTTPHeadersIteratorImpl extends BaseHeadersIterator implements MutableHTTPHeaderIterator {

        Iterator<Map.Entry<String, Object>> underlyingIterator;

        MutableHTTPHeadersIteratorImpl(Iterator<Map.Entry<String, String>> underlyingIterator) {
            super(underlyingIterator);
        }

        @Override
        public void remove() throws IllegalStateException {
            throw new UnsupportedOperationException("This operation should not be used");
        }

        @Override
        public void set(String s, String s1) throws IllegalStateException, IllegalArgumentException {
            throw new UnsupportedOperationException("This operation should not be used");
        }

        @Override
        public void add(String s, String s1) throws IllegalArgumentException {
            throw new UnsupportedOperationException("This operation should not be used");
        }
    }

    /**
     * Returns a string, representing
     *
     * @param nextValue value to be formatted.
     * @return Value of the header.
     */
    private String getStringValueOf(Object nextValue) {
        if (nextValue == null) {
            return null;
        } else if (nextValue instanceof String) {
            return (String) nextValue;
        } else if (nextValue instanceof String[]) {
            String[] arr = (String[]) nextValue;
            if (arr.length > 0) {
                return arr[0];
            } else {
                return null;
            }
        } else {
            return String.valueOf(nextValue);
        }
    }
}
