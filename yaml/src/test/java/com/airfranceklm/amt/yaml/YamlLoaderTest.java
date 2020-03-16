package com.airfranceklm.amt.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import static com.airfranceklm.amt.yaml.YamlHelper.loadAllYamlDocuments;
import static com.airfranceklm.amt.yaml.YamlHelper.nextYamlMap;
import static com.airfranceklm.amt.yaml.YamlLoader.loadFromYAML;
import static java.util.Calendar.YEAR;
import static org.junit.Assert.*;

public class YamlLoaderTest {
    @Test
    public void testLoading() throws InstantiationException, IllegalAccessException, IOException {
        Iterator<Object> iter = loadAllYamlDocuments(getClass(), "/test.yaml");
        Map<String,?> map = nextYamlMap(iter);

        assertTestBean(loadFromYAML(map, TestBean.class));
    }

    protected void assertTestBean(TestBean tb) {
        assertNotNull(tb);
        assertEquals("this is a string value", tb.getStrValue());

        // Boolean values

        assertTrue(tb.isPrimitiveBoolean());
        assertNotNull(tb.getObjectBoolean());
        assertFalse(tb.getObjectBoolean());

        // String values

        assertEquals(34, tb.getPrimitiveInteger());
        assertEquals(new Integer(45), tb.getObjectInteger());

        // String map.

        assertNotNull(tb.getStringMap());
        assertEquals("b", tb.getStringMap().get("a"));
        assertEquals("d", tb.getStringMap().get("c"));

        // String map.
        assertNotNull(tb.getObjectMap());
        assertEquals(3, tb.getObjectMap().get("a"));
        assertFalse((Boolean)tb.getObjectMap().get("b"));

        // ------------------------
        // Context

//        assertNotNull(tb.getStringFromContext());
//        assertEquals("abc", tb.getStringFromContext());

        // Dates
        assertNotNull(tb.getGmtTime());
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("Zulu"));
        c.setTime(tb.getGmtTime());

        assertEquals(2019, c.get(YEAR));
        assertEquals(9, c.get(Calendar.MONTH));
        assertEquals(1, c.get(Calendar.DATE));
        assertEquals(22, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(33, c.get(Calendar.MINUTE));
        assertEquals(34, c.get(Calendar.SECOND));


        assertNotNull(tb.getNestedBean());
        assertEquals("adf", tb.getNestedBean().getValue());
    }

    @Test
    public void testLoadingViaObjectMapper() throws IOException {
        Iterator<Object> iter = loadAllYamlDocuments(getClass(), "/test.yaml");
        Map<String,?> map = nextYamlMap(iter);

        TestBean tb = new ObjectMapper().convertValue(map, TestBean.class);
        assertTestBean(tb);
    }


}
