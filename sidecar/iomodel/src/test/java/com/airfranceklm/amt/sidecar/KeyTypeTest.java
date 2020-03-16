package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.identity.KeyType;
import org.junit.Test;

import static org.junit.Assert.*;

public class KeyTypeTest {
    @Test
    public void testKeyTypeEquality() {
        KeyType t1= new KeyType("AAA");
        KeyType t2= new KeyType("AAA");

        assertNotSame(t1, t2);
        assertEquals(t1, t2);
    }


}
