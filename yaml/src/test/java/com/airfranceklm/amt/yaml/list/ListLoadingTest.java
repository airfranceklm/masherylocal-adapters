package com.airfranceklm.amt.yaml.list;

import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static com.airfranceklm.amt.yaml.YamlHelper.*;
import static com.airfranceklm.amt.yaml.YamlLoader.loadFromYAML;
import static org.junit.Assert.*;

public class ListLoadingTest {
    @Test
    public void testWillLoadStringList() throws InstantiationException, IllegalAccessException, IOException {
        Iterator<Object> docs = loadAllYamlDocuments(getClass(), "/list/listLoading.yaml");
        StringListBean slb = loadFromYAML(nextYamlMap(docs), StringListBean.class);

        assertNotNull(slb);
        assertNotNull(slb.getColl());
        assertTrue(slb.getColl().contains("A"));
        assertTrue(slb.getColl().contains("B"));
        assertTrue(slb.getColl().contains("C"));
    }

    @Test
    public void testWillLoadIntList() throws InstantiationException, IllegalAccessException, IOException {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/list/intLoading.yaml");
        IntListBean lc = loadFromYAML(nextYamlMap(yamlDocs), IntListBean.class);
        assertNotNull(lc);
        assertNotNull(lc.getList());

        assertEquals(3, lc.getList().size());
        assertTrue(lc.getList().contains(12));
        assertTrue(lc.getList().contains(13));
        assertTrue(lc.getList().contains(14));

        // Check that offending list will not be laoded;
        lc = loadFromYAML(nextYamlMap(yamlDocs), IntListBean.class);
        assertNotNull(lc);
        assertNull(lc.getList());
    }

    @Test
    public void testWillLoadTypedListCollection() throws InstantiationException, IllegalAccessException, IOException {
        Map<String,?> yaml = loadSingleYamlDocument(getClass(), "/list/mappingObjectLoading.yaml");
        ListContainer lc = loadFromYAML(yaml, ListContainer.class);

        assertNotNull(lc);
        assertNotNull(lc.getList());

        StringListBean slb = lc.getList().get(0);

        assertTrue(slb.getColl().contains("A"));
        assertTrue(slb.getColl().contains("B"));
        assertTrue(slb.getColl().contains("C"));

        StringListBean slb2 = lc.getList().get(1);

        assertTrue(slb2.getColl().contains("D"));
        assertTrue(slb2.getColl().contains("E"));
        assertTrue(slb2.getColl().contains("F"));
    }
}
