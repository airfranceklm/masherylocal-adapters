package com.airfranceklm.amt.testsupport;

import com.mashery.http.io.ContentProducer;
import org.easymock.IArgumentMatcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Content source matcher: compa
 */
class ContentProducerArgumentsMatcher implements IArgumentMatcher {
    private String expContent;

    ContentProducerArgumentsMatcher(String expContent) {
        this.expContent = expContent;
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
