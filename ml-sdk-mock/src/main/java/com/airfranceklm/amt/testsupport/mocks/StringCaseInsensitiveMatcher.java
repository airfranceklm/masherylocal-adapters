package com.airfranceklm.amt.testsupport.mocks;

import org.easymock.IArgumentMatcher;

import java.util.Objects;

import static org.easymock.EasyMock.reportMatcher;

public class StringCaseInsensitiveMatcher implements IArgumentMatcher {
    private String exp;

    public StringCaseInsensitiveMatcher(String exp) {

        this.exp = exp;
    }

    @Override
    public boolean matches(Object o) {
        if (o instanceof String) {
            return exp.equalsIgnoreCase((String)o);
        }
        return false;
    }

    @Override
    public void appendTo(StringBuffer stringBuffer) {
        stringBuffer.append(exp).append("(case insensitive)");
    }

}
