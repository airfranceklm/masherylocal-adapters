package com.airfranceklm.amt.sidecar;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertNotNull;

/**
 * Unit test for the AFKLM Lambda sidecar for JWT scenarios
 */
public class SidecarJWTTest extends SidecarMockSupport {

    private static SidecarInvocationTestSuite suite;

    @BeforeClass
    public static void init() {
        suite = new SidecarInvocationTestSuite();
        suite.loadCasesFrom(SidecarJWTTest.class.getResourceAsStream("./base-jwt.yml"));
    }


    @Test
    public void testSuccessfulLambdaSidecarInvocation() {
        SidecarInvocationTestCase rc = suite.getCase("Successful Request");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testDenialResponseFromLambda() {
        SidecarInvocationTestCase rc = suite.getCase("Lambda Denies");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testProcessorRejectsIncompleteRequests() {
        SidecarInvocationTestCase rc = suite.getCase("Incomplete Request");
        assertNotNull(rc);
        verifyPreProcessorCase(rc);
    }

    @Test
    public void testProcessorRejectsIncompleteRequestsForMissingEAVs() throws IOException {
        SidecarInvocationTestCase rc = suite.getCase("Missing EAV Request");
        assertNotNull(rc);
        verifyPreProcessorCase(rc);
    }
}
