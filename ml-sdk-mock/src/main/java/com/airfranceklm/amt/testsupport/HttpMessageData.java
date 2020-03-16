package com.airfranceklm.amt.testsupport;

import com.airfranceklm.amtml.payload.PayloadOperations;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mashery.http.io.ContentSource;
import lombok.*;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.airfranceklm.amt.testsupport.Mocks.copyIfNull;
import static com.airfranceklm.amt.testsupport.Mocks.copyIfNullMap;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.fail;

public class HttpMessageData extends PayloadSourceModel  {

    @JsonProperty("http version") @Getter @Setter
    protected String version = "HTTP/1.1";

    @JsonProperty("headers")
    @Getter @Setter @Singular
    protected Map<String, String> headers;

    @Getter @Setter
    protected boolean reproducible = false;

    @Getter @Setter
    protected String contentAccessException;

    public HttpMessageData() {
        super();
    }

    public HttpMessageData(long payloadLength, String payload, String base64BinaryPayload, byte[] binaryPayload, Class<?> payloadOwner, String payloadResource, Map<String, String> headers, boolean reproducible, String contentAccessException, String version) {
        super(payloadLength, payload, base64BinaryPayload, binaryPayload, payloadOwner, payloadResource);
        this.headers = headers;
        this.reproducible = reproducible;
        this.contentAccessException = contentAccessException;
        this.version = version;
    }

    public HttpMessageData deepCopyFrom(HttpMessageData another) {
        super.deepCopyFrom(another);
        Mocks.cloneNullableMap(another::getHeaders, this::setHeaders, HashMap::new);

        this.reproducible = another.reproducible;
        this.contentAccessException = another.contentAccessException;
        this.version = another.version;

        return this;
    }

    public ContentSource mockContentSource(EasyMockSupport owner) {
        byte[] buf = resolvePayload();

        if (buf == null) {
            return null;
        }

        try {
            if (contentAccessException != null) {
                ContentSource cs = owner.createMock(ContentSource.class);
                expect(cs.getInputStream()).andThrow(new IOException(contentAccessException));
                return cs;
            }

            if (reproducible) {
                ContentSource cs = owner.createMock(ContentSource.class);
                expect(cs.getContentLength()).andReturn(getPayloadLength()).anyTimes();
                expect(cs.isRepeatable()).andReturn(reproducible).anyTimes();
                expect(cs.getInputStream()).andAnswer(new ContentSourceAnswer(buf)).anyTimes();

                return cs;
            } else {
                return PayloadOperations.source(buf, false);
            }
        } catch (IOException ex) {
            fail("Exception should not be thrown during the setup of the mock");
            throw new IllegalStateException("Unreachable code, exception should be thrown up");
        }
    }

    protected HttpMessageData inheritFrom(HttpMessageData another) {
        super.inheritFrom(another);

        copyIfNull(this::getVersion, another::getVersion, this::setVersion);
        copyIfNullMap(this::getHeaders, another::getHeaders, this::setHeaders, HashMap::new);

        copyIfNull(this::getContentAccessException, another::getContentAccessException, this::setContentAccessException);

        return this;
    }

    static class ContentSourceAnswer implements IAnswer<InputStream> {
        byte[] source;

        public ContentSourceAnswer(byte[] source) {
            this.source = source;
        }

        @Override
        public InputStream answer() {
            return source != null ? new ByteArrayInputStream(source) : null;
        }
    }
}
