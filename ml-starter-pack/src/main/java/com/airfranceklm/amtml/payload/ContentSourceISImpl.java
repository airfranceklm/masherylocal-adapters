package com.airfranceklm.amtml.payload;

import com.mashery.http.io.ContentSource;

import java.io.IOException;
import java.io.InputStream;

public class ContentSourceISImpl implements ContentSource {

    InputStream is;

    public ContentSourceISImpl(InputStream is) {
        this.is = is;
    }

    @Override
    public long getContentLength() {
        try {
            return is != null ? is.available() : 0;
        } catch (IOException ex) {
            return -1;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return is;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }
}
