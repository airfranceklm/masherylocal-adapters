package com.airfranceklm.amtml.payload;

import com.mashery.http.io.ContentProducer;
import com.mashery.http.io.ContentSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PipedContentProducer implements ContentProducer {

    private ContentSource cs;

    public PipedContentProducer(ContentSource cs) {
        this.cs = cs;
    }

    @Override
    public long getContentLength() {
        return cs.getContentLength();
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        if (cs.getContentLength() > 0) {
            try (InputStream is = cs.getInputStream()) {
                byte[] buf = new byte[(int)Math.max(10240, cs.getContentLength())];

                int k=0;
                while ((k=is.read(buf)) > 0) {
                    outputStream.write(buf, 0, k);
                }
            }
        }
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }
}
