package com.airfranceklm.amtml.payload;

import com.mashery.http.io.ContentProducer;

import java.io.IOException;
import java.io.OutputStream;

public class BinaryContentProducerImpl implements ContentProducer {
    private byte[] data;

    public BinaryContentProducerImpl(byte[] data) {
        this.data = data;
    }

    @Override
    public long getContentLength() {
        return data != null ? data.length : 0;
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        if (data != null) {
            outputStream.write(data);
        }
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }
}
