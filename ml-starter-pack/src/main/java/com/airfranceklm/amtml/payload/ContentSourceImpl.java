package com.airfranceklm.amtml.payload;

import com.mashery.http.io.ContentSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ContentSourceImpl implements ContentSource {

    byte[] data;
    boolean repeatable;

    public ContentSourceImpl(byte[] data) {
        this(data, true);
    }

    public ContentSourceImpl(byte[] data, boolean repeatable) {
        this.data = data;
        this.repeatable = repeatable;
    }

    @Override
    public long getContentLength() {
        return this.data != null ? this.data.length : 0;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (data != null) {
            final ByteArrayInputStream retVa = new ByteArrayInputStream(data);
            if (!repeatable) {
                data = null;
            }
            return retVa;
        } else {
            throw new IOException("No data");
        }
    }

    @Override
    public boolean isRepeatable() {
        return repeatable;
    }
}
