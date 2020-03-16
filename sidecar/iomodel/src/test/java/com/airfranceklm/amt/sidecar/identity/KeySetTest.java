package com.airfranceklm.amt.sidecar.identity;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;

public class KeySetTest extends EasyMockSupport {
    private KeySet<IdentifiableKey> keySet;

    @Before
    public void setup() {
        keySet = new KeySet<>();

        IdentifiableKey ik_A = createMock(IdentifiableKey.class);
        expect(ik_A.getKeyIdentifier()).andReturn(new KeyIdentifier("Key1", new KeyType("Type A"))).anyTimes();

        IdentifiableKey ik_B = createMock(IdentifiableKey.class);
        expect(ik_B.getKeyIdentifier()).andReturn(new KeyIdentifier("KeyB", new KeyType("Type B"))).anyTimes();

        keySet.addKey(ik_A);
        keySet.addKey(ik_B);
    }

    @Test
    public void testGettingKeyById() {
        replayAll();

        assertNull(keySet.getKeyById("missingId"));
        IdentifiableKey ik = keySet.getKeyById("Key1");
        assertNotNull(ik);

        assertEquals(new KeyType("Type A"), ik.getKeyIdentifier().getKeyType());
        assertEquals("Type A", ik.getKeyIdentifier().getType());
    }

    @Test
    public void testGettingKeyByType() {
        replayAll();

        assertNull(keySet.getKeyByType(new KeyType("missingType")));
        IdentifiableKey ik = keySet.getKeyByType(new KeyType("Type A"));
        assertNotNull(ik);

        assertEquals("Type A", ik.getKeyIdentifier().getType());
        assertEquals("Key1", ik.getKeyIdentifier().getKeyId());
    }

    @Test
    public void testGettingKeyByIdAndType() {
        replayAll();

        assertNull(keySet.getKey("missingKey", new KeyType("missingType")));
        assertNull(keySet.getKey("Key1", new KeyType("Type A.m")));
        assertNull(keySet.getKey("Key1.m", new KeyType("Type A")));

        IdentifiableKey ik = keySet.getKey("Key1", new KeyType("Type A"));
        assertNotNull(ik);

        assertEquals("Type A", ik.getKeyIdentifier().getType());
        assertEquals("Key1", ik.getKeyIdentifier().getKeyId());
    }
}
