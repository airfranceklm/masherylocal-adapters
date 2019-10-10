package com.airfranceklm.amt.sidecar.config;

import com.airfranceklm.amt.sidecar.model.SidecarOutput;
import org.junit.Test;

import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.loadAllYamlDocuments;
import static java.util.Calendar.YEAR;
import static junit.framework.Assert.*;

/**
 * Common tests for building the sidecar output from YAML.
 * @param <T> concrete type of the sidecar output.
 */
abstract class CommonYamlSidecarOutputBuildingTest<T extends SidecarOutput> {

    //----------------
    // Common

    @Test
    public void testLoadingUnchangedUntil() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/unchangedUntil.yaml");
        assertNotNull(yamlDocs);

        SidecarOutput so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getUnchangedUntil());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getUnchangedUntil());

        // First date, with millisecond support
        so = getNextSidecarOutput(yamlDocs);
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
        so = getNextSidecarOutput(yamlDocs);
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
        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getUnchangedUntil());

        // Third date date, with offset time zone
        so = getNextSidecarOutput(yamlDocs);
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

    //---------------
    // Terminate

    @Test
    public void testTerminateLoadingCode() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/terminateCode.yaml");
        assertNotNull(yamlDocs);

        // Case 1: empty code
        SidecarOutput so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getTerminate());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNotNull(so.getTerminate());
        assertEquals(new Integer(301), so.getTerminate().getCode());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getTerminate());
    }

    @Test
    public void testTerminateLoadingMessage() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/terminateMessage.yaml");
        assertNotNull(yamlDocs);

        // Case 1: empty code
        SidecarOutput so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getTerminate());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getTerminate());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNotNull(so.getTerminate());
        assertEquals("Error", so.getTerminate().getMessage());
    }

    @Test
    public void testTerminateLoadingPayload() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/terminatePayload.yaml");
        assertNotNull(yamlDocs);

        // Case 1: empty code
        SidecarOutput so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getTerminate());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getTerminate());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNotNull(so.getTerminate());
        assertEquals("Payload", so.getTerminate().getPayload());
    }

    @Test
    public void testTerminateLoadingPayloadBase64Encoded() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/terminatePayloadBase64.yaml");
        assertNotNull(yamlDocs);

        // Case 1: empty code
        SidecarOutput so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getTerminate());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getTerminate());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNotNull(so.getTerminate());
        assertTrue(so.getTerminate().getBase64Encoded());
    }

    @Test
    public void testTerminateLoadingHeaders() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/terminateHeaders.yaml");
        assertNotNull(yamlDocs);

        // Case 1: empty code
        SidecarOutput so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getTerminate());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNotNull(so.getTerminate());
        assertEquals(2, so.getTerminate().getHeaders().size());
        assertEquals("B", so.getTerminate().getHeaders().get("a"));
        assertEquals("C", so.getTerminate().getHeaders().get("b"));

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getTerminate());
    }

    @Test
    public void testTerminateLoadingJSONPayload() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/terminateJSON.yaml");
        assertNotNull(yamlDocs);

        // Case 1: empty code
        SidecarOutput so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getTerminate());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNotNull(so.getTerminate());
        assertEquals(2, so.getTerminate().getJSONPayload().size());
        assertEquals("B", so.getTerminate().getJSONPayload().get("a"));
        assertEquals(35, so.getTerminate().getJSONPayload().get("c"));
    }

    protected abstract T getNextSidecarOutput(Iterator<Object> yamlDocs);

    public void testAllTerminateCommand() {
        testLoadingUnchangedUntil();
        testTerminateLoadingCode();
        testTerminateLoadingMessage();
        testTerminateLoadingPayload();
        testTerminateLoadingPayloadBase64Encoded();
        testTerminateLoadingHeaders();
        testTerminateLoadingJSONPayload();
    }

    //-----------------------------------
    // Modify command

    protected void testLoadingCommonModifyCommand() {
        testLoadingUnchangedUntil();

        testModifyLoadingAddHeaders();
        testModifyLoadingDropHeaders();

        testModifyLoadingPayload();
        testModifyLoadingPayloadBase64Encoded();
        testModifyLoadingJson();
    }

    @Test
    public void testModifyLoadingDropHeaders() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/modifyDropHeaders.yaml");
        assertNotNull(yamlDocs);

        // Case 1: empty code
        SidecarOutput so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getModify());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNotNull(so.getModify());
        assertNotNull(so.getModify().getDropHeaders());
        assertEquals(2, so.getModify().getDropHeaders().size());
        assertTrue(so.getModify().getDropHeaders().contains("B"));
        assertTrue(so.getModify().getDropHeaders().contains("C"));

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getModify());
    }

    @Test
    public void testModifyLoadingJson() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/json.yaml");
        assertNotNull(yamlDocs);

        SidecarOutput so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getModify());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNotNull(so.getModify());
        assertNotNull(so.getModify().getJSONPayload());
        assertEquals("string", so.getModify().getJSONPayload().get("a"));
        assertEquals(32, so.getModify().getJSONPayload().get("b"));
        assertEquals(false, so.getModify().getJSONPayload().get("c"));
    }

    @Test
    public void testModifyLoadingPayload() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/payload.yaml");
        assertNotNull(yamlDocs);

        // Case 1: empty code
        SidecarOutput so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getModify());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getModify());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNotNull(so.getModify());
        assertEquals("Payload", so.getModify().getPayload());
    }

    @Test
    public void testModifyLoadingPayloadBase64Encoded() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/base64Encoded.yaml");
        assertNotNull(yamlDocs);

        // Case 1: empty code
        SidecarOutput so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getModify());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getModify());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNotNull(so.getModify());
        assertEquals(Boolean.TRUE, so.getModify().getBase64Encoded());
    }

    @Test
    public void testModifyLoadingAddHeaders() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/addHeaders.yaml");
        assertNotNull(yamlDocs);

        // Case 1: empty code
        SidecarOutput so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getModify());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNotNull(so.getModify());
        assertEquals(2, so.getModify().getAddHeaders().size());
        assertEquals("B", so.getModify().getAddHeaders().get("a"));
        assertEquals("C", so.getModify().getAddHeaders().get("b"));

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getModify());
    }


}
