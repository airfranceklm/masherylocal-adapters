package com.airfranceklm.amt.phase1;

import com.airfranceklm.amt.sidecar.SidecarInvocationTestSuite;
import com.airfranceklm.amt.sidecar.SidecarMockSupport;
import com.airfranceklm.amt.sidecar.SidecarInvocationTestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertNotNull;

public class DynamicBackendAuthorizationTest extends SidecarMockSupport {

    private static SidecarInvocationTestSuite suite;

    @BeforeClass
    public static void init() {
        suite = new SidecarInvocationTestSuite();
        suite.loadCasesFrom(DynamicBackendAuthorizationTest.class.getResourceAsStream("/phase-1/dynamic_backend_authorization.yml"));
    }

    @Test
    public void testAuthenticationSucceeds() throws IOException {
        SidecarInvocationTestCase src = suite.getCase("authentication succeeds");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }

    @Test
    public void testAuthenticationFails() throws IOException {
        SidecarInvocationTestCase src = suite.getCase("authentication fails");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }

    @Test
    public void test500ReturnedOnInvocationFailure() throws IOException {
        SidecarInvocationTestCase src = suite.getCase("invocation fails");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }
}
