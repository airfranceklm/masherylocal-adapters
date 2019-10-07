package com.airfranceklm.amt.sidecar;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class AFKLMSidecarProcessorTest extends AFKLMSidecarMockSupport {

    public static AFKLMSidecarCaseYAMLReader reader;

    @BeforeClass
    public static void initReader() {
        reader = new AFKLMSidecarCaseYAMLReader("./base-test.yml");
    }

    /**
     * Tests the basic sending 403 back to the client after lambda pre-processor invocation.
     */
    @Test
    public void testBasicSending403() {
        SidecarRequestCase rc = reader.getRequestCase("Smoke Test", "base rejection scenario");
        if (rc == null) {
            fail("Cannot find the expected request case");
        }

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testSending403WithCodeAlone() {
        SidecarRequestCase rc = reader.getRequestCase("Smoke Test", "rejection with single code");
        if (rc == null) {
            fail("Cannot find the expected request case");
        }

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testSending403WithPayload() {
        SidecarRequestCase rc = reader.getRequestCase("Smoke Test", "rejection with payload");
        if (rc == null) {
            fail("Cannot find the expected request case");
        }

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testSending403WithJSON() {
        SidecarRequestCase rc = reader.getRequestCase("Smoke Test", "rejection with json and content type");
        if (rc == null) {
            fail("Cannot find the expected request case");
        }

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testSending403WithJSONWithoutExplicitContentType() {
        SidecarRequestCase rc = reader.getRequestCase("Smoke Test", "rejection with json and without content type");
        if (rc == null) {
            fail("Cannot find the expected request case");
        }

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testSending500OnInternalProblem() {
        SidecarRequestCase rc = reader.getRequestCase("Smoke Test", "error reporting on internal problem");
        assertNotNull("Cannot find the expected request case", rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testContinueAfterFaultOfFailSafeFunction() {
        SidecarRequestCase rc = reader.getRequestCase("Fail-safe configuration cases", "continues on receiving internal error");
        assertNotNull("Cannot find the expected request case", rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testMaxSizeBlockingCorrectlyAllows() {
        SidecarRequestCase rc = reader.getRequestCase("Max Request Size Blocking Configuration", "fitting payload");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testMaxSizeBlockingCorrectlyBlocks() {
        SidecarRequestCase rc = reader.getRequestCase("Max Request Size Blocking Configuration", "exceeding payload");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testPassingFunctionParameters() {
        SidecarRequestCase rc = reader.getRequestCase("Passing parameters test", "positively passing");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testOperationExpansionWithQueryString() {
        SidecarRequestCase rc = reader.getRequestCase("Operation expansion test", "correct extraction");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testOperationExpansionWithoutQueryString() {
        SidecarRequestCase rc = reader.getRequestCase("Operation expansion test", "correct extraction without query string");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);

    }

    @Test
    public void testAttributionOfRemoteAddress() {
        SidecarRequestCase rc = reader.getRequestCase("Remote Address expansion", "correct remote addr attribution");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);

    }

    @Test
    public void testParsing() throws Exception {
        MarshallableSidecarOutput lso = new ObjectMapper().readValue("{\"json\":\"a\"}", MarshallableSidecarOutput.class);
        System.out.println(lso.getJson());
    }

    @Test
    public void testTokenExpansion() {
        SidecarRequestCase rc = reader.getRequestCase("Token data expansion", "expansion of token");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);

    }

    @Test
    public void testTokenExpansionWithTokenDataMissing() {
        SidecarRequestCase rc = reader.getRequestCase("Token data expansion", "expansion with token data missing");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);

    }

    @Test
    public void testRoutingExpansion() {
        SidecarRequestCase rc = reader.getRequestCase("Routing data expansion", "expansion of routing");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);

    }

    @Test
    public void testPackageKeyAttributeExpansion() {
        SidecarRequestCase rc = reader.getRequestCase("Including Package Key EAVs", "sending correct message with PkC missing");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);

    }

    @Test
    public void testWilLSend400IfRequiredPackageKeyEAVIsEmpty() {
        SidecarRequestCase rc = reader.getRequestCase("Including Package Key EAVs", "send 400 when required package key being empty");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);

    }

    @Test
    public void testAppAttributeExpansion() {
        SidecarRequestCase rc = reader.getRequestCase("Including Application EAVs", "sending correct message with PkC missing");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);

    }

    @Test
    public void testWilLSend400IfApplicationEAVIsEmpty() {
        SidecarRequestCase rc = reader.getRequestCase("Including Application EAVs", "send 400 when required eav being empty");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);

    }

    @Test
    public void testWilLSend400IfRequiredApplicationEAVIsNull() {
        SidecarRequestCase rc = reader.getRequestCase("Including Application EAVs", "send 400 when required eav is null");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);

    }

}
