package com.airfranceklm.amt.testsupport.mocks;

import com.mashery.http.io.ContentProducer;
import org.easymock.IArgumentMatcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ContentProducerBinaryArgumentsMatcher implements IArgumentMatcher {
    private byte[] data;

    public ContentProducerBinaryArgumentsMatcher(byte[] pData) {
        Objects.requireNonNull(pData);
        this.data = pData;
    }

    @Override
    public boolean matches(Object o) {
        if (o instanceof ContentProducer) {
            ContentProducer cs = (ContentProducer)o;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                cs.writeTo(baos);
            } catch (IOException ex) {
                return false;
            }

            byte[] cmpData = baos.toByteArray();

            if (data.length != cmpData.length) {
                return false;
            }

            for (int i=0; i<data.length; i++) {
                if (data[i] != cmpData[i]) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public void appendTo(StringBuffer stringBuffer) {
        stringBuffer.append("(Binary Data)");
    }
}
