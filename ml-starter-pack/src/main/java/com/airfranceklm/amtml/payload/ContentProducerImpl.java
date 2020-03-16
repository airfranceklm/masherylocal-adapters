package com.airfranceklm.amtml.payload;

import com.mashery.http.io.ContentProducer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Content producer for the response messages.
 */
public class ContentProducerImpl implements ContentProducer {
    private String content;

    ContentProducerImpl(String content) {
        this.content = content;
    }

    @Override
    public long getContentLength() {
        return content == null ? 0 : content.length();
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        if (getContentLength() > 0) {
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentProducerImpl that = (ContentProducerImpl) o;
        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }
}
