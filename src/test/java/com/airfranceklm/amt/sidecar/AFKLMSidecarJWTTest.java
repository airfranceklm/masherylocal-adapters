package com.airfranceklm.amt.sidecar;

import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

/**
 * Unit test for the AFKLM Lambda sidecar for JWT scenarios
 */
public class AFKLMSidecarJWTTest extends AFKLMSidecarMockSupport {

    static private AFKLMSidecarCaseYAMLReader reader;

    @BeforeClass
    public static void initReader() {
        reader = new AFKLMSidecarCaseYAMLReader("./base-jwt.yml");
    }

    @Test
    public void testSuccessfulLambdaSidecarInvocation() {
        SidecarRequestCase rc = reader.getRequestCase("Custom JWT Validation Scenarios", "Successful Request");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testDenialResponseFromLambda() {
        SidecarRequestCase rc = reader.getRequestCase("Custom JWT Validation Scenarios", "Lambda Denies");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testProcessorRejectsIncompleteRequests() {
        SidecarRequestCase rc = reader.getRequestCase("Custom JWT Validation Scenarios", "Incomplete Request");
        assertNotNull(rc);
        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testProcessorRejectsIncompleteRequestsForMissingEAVs() {
        SidecarRequestCase rc = reader.getRequestCase("Custom JWT Validation Scenarios", "Missing EAV Request");
        assertNotNull(rc);
        verifyPreProcessorRequestCase(rc);
    }
}
