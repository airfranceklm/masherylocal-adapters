package com.airfranceklm.amt.sidecar.config;

import com.airfranceklm.amt.sidecar.SidecarOutput;
import org.junit.Test;

import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.*;
import static java.util.Calendar.YEAR;
import static junit.framework.Assert.*;

public class YamlSidecarOutputBuildingTest {
    @Test
    public void testLoadingCode() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/code.yaml");
        assertNotNull(yamlDocs);

        // Case 1: empty code
        SidecarOutput so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNull(so.getCode());

        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertEquals(new Integer(301), so.getCode());

        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNull(so.getCode());
    }

    @Test
    public void testLoadingMessage() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/message.yaml");
        assertNotNull(yamlDocs);

        // Case 1: empty code
        SidecarOutput so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNull(so.getMessage());

        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNull(so.getMessage());

        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertEquals("Error", so.getMessage());
    }

    @Test
    public void testLoadingPayload() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/payload.yaml");
        assertNotNull(yamlDocs);

        // Case 1: empty code
        SidecarOutput so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNull(so.getPayload());

        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNull(so.getPayload());

        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertEquals("Payload", so.getPayload());
    }

    @Test
    public void testLoadingAddHeaders() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/addHeaders.yaml");
        assertNotNull(yamlDocs);

        // Case 1: empty code
        SidecarOutput so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNull(so.getAddHeaders());

        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNotNull(so.getAddHeaders());
        assertEquals(2, so.getAddHeaders().size());
        assertEquals("B", so.getAddHeaders().get("a"));
        assertEquals("C", so.getAddHeaders().get("b"));

        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNull(so.getAddHeaders());
    }

    @Test
    public void testLoadingDropHeadersHeaders() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/dropHeaders.yaml");
        assertNotNull(yamlDocs);

        // Case 1: empty code
        SidecarOutput so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNull(so.getDropHeaders());

        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNotNull(so.getDropHeaders());
        assertEquals(2, so.getDropHeaders().size());
        assertTrue(so.getDropHeaders().contains("B"));
        assertTrue(so.getDropHeaders().contains("C"));

        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNull(so.getAddHeaders());
    }

    @Test
    public void testLoadingUnchangedUntil() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/unchangedUntil.yaml");
        assertNotNull(yamlDocs);

        SidecarOutput so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNull(so.getUnchangedUntil());

        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNull(so.getUnchangedUntil());

        // First date, with millisecond support
        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNotNull(so.getUnchangedUntil());
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTime(so.getUnchangedUntil());

        assertEquals(2019, c.get(YEAR));
        assertEquals(9, c.get(Calendar.MONTH));
        assertEquals(6, c.get(Calendar.DATE));

        assertEquals(17, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(46, c.get(Calendar.MINUTE));
        assertEquals(3, c.get(Calendar.SECOND));
        assertEquals(356, c.get(Calendar.MILLISECOND));

        // Second date date, without millisecond support
        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNotNull(so.getUnchangedUntil());
        c.setTime(so.getUnchangedUntil());

        assertEquals(2020, c.get(YEAR));
        assertEquals(10, c.get(Calendar.MONTH));
        assertEquals(7, c.get(Calendar.DATE));

        assertEquals(18, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(56, c.get(Calendar.MINUTE));
        assertEquals(4, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));

        // Wrong date format completely.
        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNull(so.getUnchangedUntil());

        // Third date date, with offset time zone
        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNotNull(so.getUnchangedUntil());
        c.setTime(so.getUnchangedUntil());

        assertEquals(2021, c.get(YEAR));
        assertEquals(1, c.get(Calendar.MONTH));
        assertEquals(3, c.get(Calendar.DATE));

        assertEquals(15, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(36, c.get(Calendar.MINUTE));
        assertEquals(34, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));
    }

    @Test
    public void testLoadingChangeRoute() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/changeRoute.yaml");
        assertNotNull(yamlDocs);

        SidecarOutput so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNull(so.getChangeRoute());

        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNotNull(so.getChangeRoute());
        assertEquals("aHost", so.getChangeRoute().getHost());
        assertEquals("aFile", so.getChangeRoute().getFile());
        assertEquals("get", so.getChangeRoute().getHttpVerb());
        assertEquals("aUri", so.getChangeRoute().getUri());
        assertEquals(new Integer(4567), so.getChangeRoute().getPort());

        // All nested elements are null in this scenario.
        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNull(so.getChangeRoute());
    }

    @Test
    public void testLoadingRelayParams() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/relayParams.yaml");
        assertNotNull(yamlDocs);

        SidecarOutput so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNull(so.getRelayParams());

        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNotNull(so.getRelayParams());
        assertEquals(3, so.getRelayParams().size());
        assertEquals("string", so.getRelayParams().get("a"));
        assertEquals(32, so.getRelayParams().get("b"));
        assertEquals(false, so.getRelayParams().get("c"));
    }

    @Test
    public void testLoadingJson() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/json.yaml");
        assertNotNull(yamlDocs);

        SidecarOutput so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNull(so.getJSONPayload());

        so = buildSidecarOutputFromYAML(nextYamlDocument(yamlDocs));
        assertNotNull(so);
        assertNotNull(so.getJSONPayload());
        assertEquals("string", so.getJSONPayload().get("a"));
        assertEquals(32, so.getJSONPayload().get("b"));
        assertEquals(false, so.getJSONPayload().get("c"));
    }
}
