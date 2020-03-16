package com.airfranceklm.amt.sidecar.identity;

import org.junit.Test;

import static org.junit.Assert.*;

public class KeyIdentifierTest {
    @Test
    public void testAutoSettingKeyType() {
        KeyIdentifier ky = new KeyIdentifier();
        assertNull(ky.getType());

        ky.setType("ABC");
        assertNotNull(ky.getKeyType());

        assertEquals("ABC", ky.getType());
        assertEquals(new KeyType("ABC"), ky.getKeyType());
    }
}
