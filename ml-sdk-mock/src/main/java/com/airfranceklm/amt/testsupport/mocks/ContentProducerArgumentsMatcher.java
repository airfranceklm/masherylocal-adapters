package com.airfranceklm.amt.testsupport.mocks;

import com.mashery.http.io.ContentProducer;
import org.easymock.IArgumentMatcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Content source matcher: compa
 */
public class ContentProducerArgumentsMatcher implements IArgumentMatcher {
    private String expContent;

    public ContentProducerArgumentsMatcher(String expContent) {
        this.expContent = expContent;
    }

    @Override
    public String toString() {
        return expContent;
    }

    @Override
    public boolean matches(Object o) {
        if  (o instanceof ContentProducer) {
            ContentProducer cp = (ContentProducer)o;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                cp.writeTo(baos);
            } catch (IOException ex) {
                return false;
            }

            return expContent.equals(baos.toString());
        } else {
            return false;
        }
    }

    @Override
    public void appendTo(StringBuffer stringBuffer) {
        stringBuffer.append("contentProducer(...)");
    }
}
