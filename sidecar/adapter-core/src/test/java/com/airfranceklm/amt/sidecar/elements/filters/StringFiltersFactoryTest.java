package com.airfranceklm.amt.sidecar.elements.filters;

import org.junit.Test;

import java.util.function.Function;

import static com.airfranceklm.amt.sidecar.filters.StringFilterFactory.createCaseInsensitiveMatcher;
import static com.airfranceklm.amt.sidecar.filters.StringFilterFactory.createMatcher;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class StringFiltersFactoryTest {

    @Test
    public void testRegexExpression() {
        Function<String,Boolean> regexMatched = createMatcher("~/[abc]+/")::test;

        assertTrue(regexMatched.apply("aaabbbccc"));
        assertFalse(regexMatched.apply("aaabbbddccc"));
    }

    @Test
    public void testLiteralExpression() {
        Function<String,Boolean> f = createMatcher("`r56\"`")::test;

        assertTrue(f.apply("r56\""));
        assertFalse(f.apply("fgr4"));
    }
    @Test
    public void testSetExpression() {
        Function<String,Boolean> f = createMatcher("password|client_credentials")::test;

        assertTrue(f.apply("password"));
        assertTrue(f.apply("client_credentials"));
        assertFalse(f.apply("jwt_bearer"));
    }

    @Test
    public void testCaseInsensitiveRegexExpression() {
        Function<String,Boolean> regexMatched = createCaseInsensitiveMatcher("~/[abc]+/");

        assertTrue(regexMatched.apply("aaabBBbccc"));
        assertFalse(regexMatched.apply("aaaBBbddccc"));
    }

    @Test
    public void testCaseInsensitiveLiteralExpression() {
        Function<String,Boolean> f = createCaseInsensitiveMatcher("`r56\"`");

        assertTrue(f.apply("R56\""));
        assertFalse(f.apply("fgr4"));
    }
    @Test
    public void testCaseInsensitiveSetExpression() {
        Function<String,Boolean> f = createCaseInsensitiveMatcher("password|client_credentials");

        assertTrue(f.apply("paSSword"));
        assertTrue(f.apply("client_cReDentials"));
        assertFalse(f.apply("jwt_Bearer"));
    }
}
