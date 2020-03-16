package com.airfranceklm.amt.sidecar.model.json;

import com.airfranceklm.amt.sidecar.JsonHelper;
import com.airfranceklm.amt.sidecar.model.SidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static com.airfranceklm.amt.sidecar.JsonHelper.*;
import static java.util.Calendar.*;
import static org.junit.Assert.*;

public class JsonHelperTest {

    @Test
    public void testBasicIdempotentUnmarshalling() throws IOException {
        String str = "{\"unchangedUntil\": \"2020-01-02T01:23:45Z\"}";

        SidecarPreProcessorOutput out = JsonHelper.toSidecarPreProcessorOutput(str);
        assertNotNull(out);
        assertDate(out.getUnchangedUntil());
    }

    @Test
    public void testBasicIdempotentUnmarshallingWithMillisecond() throws IOException {
        String str = "{\"unchangedUntil\": \"2020-01-02T01:23:45.000Z\"}";

        SidecarPreProcessorOutput out = JsonHelper.toSidecarPreProcessorOutput(str);
        assertNotNull(out);
        assertDate(out.getUnchangedUntil());
    }

    private void assertDate(Date dt) {
        assertNotNull(dt);

        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.setTime(dt);
        assertEquals(2020, c.get(YEAR));
        assertEquals(JANUARY, c.get(MONTH));
        assertEquals(2, c.get(DATE));

        assertEquals(1, c.get(HOUR_OF_DAY));
        assertEquals(23, c.get(MINUTE));
        assertEquals(45, c.get(SECOND));
        assertEquals(0, c.get(MILLISECOND));
    }

    @Test
    public void testNullPostProcessorOutputConversion() throws IOException {
       assertNull(JsonHelper.toSidecarPostProcessorOutput(null));
       assertNull(JsonHelper.toSidecarPostProcessorOutput("null"));
    }

    @Test
    public void testNullPreProcessorOutputConversion() throws IOException {
       assertNull(JsonHelper.toSidecarPreProcessorOutput(null));
       assertNull(JsonHelper.toSidecarPreProcessorOutput("null"));
    }

    @Test @SuppressWarnings("unchecked")
    public void testRemovePath() {
        Map<String,Object> map = new HashMap<>();
        map.put("a", "A");
        map.put("b", "B");

        Map<String, Object> nested = new HashMap<>(map);
        map.put("n", nested);

        ObjectNode on = convert(map, ObjectNode.class);
        JsonHelper.remove(on, "/non/existent");
        assertEquals(map, toMap(on));

        JsonHelper.remove(on, "/non_existent");
        assertEquals(map, toMap(on));

        JsonHelper.remove(on, "/");
        assertEquals(map, toMap(on));

        JsonHelper.remove(on, "/n/a");
        Map<String,?> output = toMap(on);

        assertNotNull(map.get("n"));
        assertNull(((Map<String,Object>)output.get("n")).get("a"));
        assertEquals("B", ((Map<String,Object>)output.get("n")).get("b"));

        JsonHelper.remove(on, "/n");

        output = toMap(on);
        assertNull(output.get("n"));

        on = convert(map, ObjectNode.class);
        JsonHelper.remove(on, "a");
        assertNull(toMap(on).get("a"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void replacePathTest() {
        Map<String,Object> map = new HashMap<>();
        map.put("a", "A");
        map.put("b", "B");

        ObjectNode on = convert(map, ObjectNode.class);
        replacePath(on, "/", "C");
        assertEquals(map, toMap(on));

        on = convert(map, ObjectNode.class);
        replacePath(on, null, "C");
        assertEquals(map, toMap(on));

        on = convert(map, ObjectNode.class);
        replacePath(on, "/c", "C");
        Map<String,?> output = toMap(on);
        assertEquals("C", output.get("c"));

        on = convert(map, ObjectNode.class);
        replacePath(on, "/c/d", "C");
        output = toMap(on);
        assertNull(output.get("c"));

        Map<String, Object> nested = new HashMap<>(map);
        map.put("n", nested);

        on = convert(map, ObjectNode.class);
        replacePath(on, "/n/b", "BB");
        output = toMap(on);
        assertNotNull(output.get("n"));
        assertEquals("BB", ((Map<String,Object>)output.get("n")).get("b"));

        on = convert(map, ObjectNode.class);
        replacePath(on, "a", "AA");
        assertEquals("AA", toMap(on).get("a"));
    }

    @Test
    public void testGetUnmarshallingClass() {
        assertEquals(JsonSidecarPreProcessorOutput.class, getUnmarshallingImplementation(SidecarPreProcessorOutput.class));
        assertEquals(JsonSidecarPostProcessorOutput.class, getUnmarshallingImplementation(SidecarPostProcessorOutput.class));
    }

    @Test
    public void testDefaultUnmarshallerIsAvailable() {
        assertNotNull(JsonHelper.getDefaultUnmarshaller());
    }

    @Test
    public void testToMapWithNullInput() {
        assertNull(toMap(null));
    }

    @Test
    public void testReadTransportOptimizedJSON() throws IOException {
        assertNull(readTransportOptimizedJSONForModification(null));
        assertNull(readTransportOptimizedJSONForModification("null".getBytes()));
        assertNull(readTransportOptimizedJSONForModification("12345".getBytes()));
        assertNull(readTransportOptimizedJSONForModification("true".getBytes()));
        assertNull(readTransportOptimizedJSONForModification("\"value\"".getBytes()));
        assertNull(readTransportOptimizedJSONForModification("[1,2,3,4,5]".getBytes()));

        ObjectNode on = readTransportOptimizedJSONForModification("{\"a\": 44}".getBytes());
        assertNotNull(on);
        assertNotNull(on.get("a"));
        assertEquals(44, on.get("a").asInt());
    }

}
