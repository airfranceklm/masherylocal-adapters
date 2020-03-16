package com.airfranceklm.amt.yaml.map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static com.airfranceklm.amt.yaml.YamlHelper.loadSingleYamlDocument;
import static com.airfranceklm.amt.yaml.YamlLoader.loadFromYAML;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MapLoadingTest {

    @Test
    public void testLoadingContainer() throws InstantiationException, IllegalAccessException, IOException {
        Map<String,?> yaml = loadSingleYamlDocument(getClass(), "/map/mapContained.yaml");
        assertNotNull(yaml);

        assertMapContainer(yaml);
    }

    protected void assertMapContainer(Map<String, ?> yaml) throws IllegalAccessException, InstantiationException {
        MapContainer mc = loadFromYAML(yaml, MapContainer.class);
        assertNotNull(mc);
        assertNotNull(mc.getContents());
        assertEquals(2, mc.getContents().size());

        assertNotNull(mc.getContents().get("bean-a"));
        assertNotNull(mc.getContents().get("bean-b"));

        MapContainedBean mc1 = mc.getContents().get("bean-a");
        assertEquals("str-a", mc1.getStrValue());
        assertEquals(30, mc1.getIntValue());

        MapContainedBean mc2 = mc.getContents().get("bean-b");
        assertEquals("str-b", mc2.getStrValue());
        assertEquals(50, mc2.getIntValue());
    }

    @Test
    public void testLoadingContainerMap() throws InstantiationException, IllegalAccessException, IOException {
        /*
        Map<String, ?> yaml = loadSingleYamlDocument(getClass(), "/map/mapContained.yaml");
        assertNotNull(yaml);

        Map<String,MapContainedBean> mb = new ObjectMapper().convertValue(yaml, new TypeReference<Map<String, MapContainedBean>>() {});
        assertMapContainer(mb);

         */
    }

    @Test
    public void testLoadingSpecified() throws InstantiationException, IllegalAccessException, IOException {
        Map<String,?> yaml = loadSingleYamlDocument(getClass(), "/map/mapSpecified.yaml");
        assertNotNull(yaml);

        MapSpecified mc = loadFromYAML(yaml, MapSpecified.class);
        assertMapSpecified(mc);
    }

    protected void assertMapSpecified(MapSpecified mc) {
        assertNotNull(mc);
        assertNotNull(mc.getMap());
        assertEquals(2, mc.getMap().size());

        assertNotNull(mc.getMap().get("bean-a"));
        assertNotNull(mc.getMap().get("bean-b"));

        MapContainedBean mc1 = mc.getMap().get("bean-a");
        assertEquals("str-a", mc1.getStrValue());
        assertEquals(30, mc1.getIntValue());

        MapContainedBean mc2 = mc.getMap().get("bean-b");
        assertEquals("str-b", mc2.getStrValue());
        assertEquals(50, mc2.getIntValue());
    }

    @Test
    public void testLoadingSpecifiedViaMapper() throws  IOException {
        Map<String, ?> yaml = loadSingleYamlDocument(getClass(), "/map/mapSpecified.yaml");
        assertNotNull(yaml);

        MapSpecified mc = new ObjectMapper().convertValue(yaml, MapSpecified.class);
        assertMapSpecified(mc);
    }
}
