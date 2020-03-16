package com.airfranceklm.amt.sidecar;

/**
 * Tests checking that local YAML configuration is working correctly.
 */
public class YamlLocalConfigurationTest {
    /*
    @Test
    @SuppressWarnings("unchecked")
    public void testParingMultiDocument() {
        Iterable<Object> it = new Yaml().loadAll(new InputStreamReader(getClass().getResourceAsStream("/yaml/multi-doc.yaml")));
        Iterator<Object> iter = it.iterator();

        assertTrue(iter.hasNext());
        Object e = iter.next();
        assertTrue(e instanceof Map);

        AtomicInteger count = new AtomicInteger(0);
        forDefinedObjectMap((Map<String,Object>)e, "configuration", (c) -> {
            forDefinedString(c, "name", (checker) -> {
                if (checker.equals("Doc 1")) {
                    count.incrementAndGet();
                }
            });
        });

        assertEquals(1, count.get());

        assertTrue(iter.hasNext());
        e = iter.next();
        assertTrue(e instanceof Map);

        forDefinedObjectMap((Map<String,Object>)e, "configuration", (c) -> {
            forDefinedString(c, "name", (checker) -> {
                if (checker.equals("Doc 2")) {
                    count.incrementAndGet();
                }
            });
        });

        assertEquals(2, count.get());
        assertFalse(iter.hasNext());
    }

     */
}
