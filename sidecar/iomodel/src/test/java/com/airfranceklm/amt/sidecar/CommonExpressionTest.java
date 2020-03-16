package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.CommonExpressions;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.*;

public class CommonExpressionTest {

    @Test
    public void testSplittingNullString() {
        String[] split = CommonExpressions.splitStandardValueList(null);
        assertNotNull(split);
        assertEquals(0, split.length);
    }

    @Test
    public void testCommonSplit() {
        String[] split = CommonExpressions.splitStandardValueList("Aa , Bb ; Cc | Dd");
        HashSet<String> s = new HashSet<>();
        Collections.addAll(s, split);

        assertTrue(s.contains("Aa"));
        assertTrue(s.contains("Bb"));
        assertTrue(s.contains("Cc"));
        assertTrue(s.contains("Dd"));
    }

    @Test
    public void testCommonSplitWithEscapes() {
        String[] split = CommonExpressions.splitStandardValueList("A\\,A,B\\;B;C\\|C|D");
        HashSet<String> s = new HashSet<>();
        Collections.addAll(s, split);

        assertTrue(s.contains("A,A"));
        assertTrue(s.contains("B;B"));
        assertTrue(s.contains("C|C"));
        assertTrue(s.contains("D"));
    }

    @Test
    public void testShortTimeExpressions() {
        assertEquals(new Integer(60000), CommonExpressions.parseShortTimeInterval("1m"));
        assertEquals(new Integer(84000), CommonExpressions.parseShortTimeInterval("1.4m"));

        assertEquals(new Integer(1000), CommonExpressions.parseShortTimeInterval("1s"));
        assertEquals(new Integer(1400), CommonExpressions.parseShortTimeInterval("1.4s"));

        assertNull( CommonExpressions.parseShortTimeInterval("1.4sx"));
    }
}
