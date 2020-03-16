package com.airfranceklm.amt.phase1;

import com.airfranceklm.amt.sidecar.SidecarInvocationTestSuite;
import com.airfranceklm.amt.sidecar.SidecarMockSupport;
import com.airfranceklm.amt.sidecar.SidecarInvocationTestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertNotNull;

public class CustomHeaderBasedAuthorizationTest extends SidecarMockSupport {

    private static SidecarInvocationTestSuite suite;
    private static SidecarInvocationTestSuite authzJwt;

    @BeforeClass
    public static void init() {
        suite = new SidecarInvocationTestSuite();
        suite.loadCasesFrom(CustomHeaderBasedAuthorizationTest.class.getResourceAsStream("/phase-1/custom-headerbased-authn.yml"));

        authzJwt = new SidecarInvocationTestSuite();
        authzJwt.loadCasesFrom(CustomHeaderBasedAuthorizationTest.class.getResourceAsStream("/phase-1/authz-server-jwt.yml"));
    }

    @Test
    public void testNegativeConclusion() throws IOException {
        SidecarInvocationTestCase src = suite.getCase("negative conclusion");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }

    @Test
    public void testPositiveConclusion() throws IOException {
        SidecarInvocationTestCase src = suite.getCase("positive conclusion");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }

    @Test
    public void testPositiveConclusionWithAdditionalAuthorization() throws IOException {
        SidecarInvocationTestCase src = suite.getCase("positive conclusion with additional authorization");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }

    @Test
    public void test500ReturnedOnInvocationFailure() throws IOException {
        SidecarInvocationTestCase src = suite.getCase("invocation failure");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }

    // ----------------------------------
    // Header-based authentication with pre-shared signature keys

    @Test
    public void testSuccessfulB2EValidation() throws IOException {
        SidecarInvocationTestCase src = authzJwt.getCase("authorization granted");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }

    @Test
    public void testB2EAuthorizationIsDenied() throws IOException {
        SidecarInvocationTestCase src = authzJwt.getCase("authorization denied");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }
}
