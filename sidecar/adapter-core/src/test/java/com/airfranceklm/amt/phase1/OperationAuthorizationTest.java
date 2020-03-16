package com.airfranceklm.amt.phase1;

import com.airfranceklm.amt.sidecar.SidecarInvocationTestSuite;
import com.airfranceklm.amt.sidecar.SidecarMockSupport;
import com.airfranceklm.amt.sidecar.SidecarInvocationTestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

public class OperationAuthorizationTest extends SidecarMockSupport {

    static SidecarInvocationTestSuite tokenAuthz;
    static SidecarInvocationTestSuite userAuthz;
    static SidecarInvocationTestSuite suite;

    @BeforeClass
    public static void init() {
        tokenAuthz = new SidecarInvocationTestSuite();
        tokenAuthz.loadCasesFrom(OperationAuthorizationTest.class.getResourceAsStream("/phase-1/operation_authz_token.yml"));

        userAuthz = new SidecarInvocationTestSuite();
        userAuthz.loadCasesFrom(OperationAuthorizationTest.class.getResourceAsStream("/phase-1/operation_authz_userlevel.yml"));

        suite = new SidecarInvocationTestSuite();
        suite.loadCasesFrom(OperationAuthorizationTest.class.getResourceAsStream("/phase-1/operation_authorization.yml"));
    }

    @Test
    public void testAccessIsGranted() {
        SidecarInvocationTestCase src = suite.getCase("access is granted");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }

    @Test
    public void testAccessIsDenied() {
        SidecarInvocationTestCase src = suite.getCase("access is denied");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }

    @Test
    public void test500IsSentIfErrorOccurs() {
        SidecarInvocationTestCase src = suite.getCase("invocation failed");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }

    @Test
    public void testAccessGrantedToIndividualUser() {
        SidecarInvocationTestCase src = userAuthz.getCase("access is granted to individual user");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }

    @Test
    public void testAccessGrantedToIndividualUserAndToken() {
        SidecarInvocationTestCase src = tokenAuthz.getCase("access is granted to individual user token");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }
}
