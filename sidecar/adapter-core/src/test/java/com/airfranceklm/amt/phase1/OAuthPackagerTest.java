package com.airfranceklm.amt.phase1;

import com.airfranceklm.amt.sidecar.SidecarInvocationTestSuite;
import com.airfranceklm.amt.sidecar.SidecarMockSupport;
import com.airfranceklm.amt.sidecar.SidecarInvocationTestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertNotNull;

public class OAuthPackagerTest extends SidecarMockSupport {

    private static SidecarInvocationTestSuite suite;

    @BeforeClass
    public static void init() {
        suite = new SidecarInvocationTestSuite();
        suite.loadCasesFrom(OAuthPackagerTest.class.getResourceAsStream("/phase-1/oauth-packager.yml"));
    }

    @Test
    public void testAccessIsGranted() throws IOException {
        SidecarInvocationTestCase src = suite.getCase("access is granted");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }

    @Test
    public void testAccessIsGrantedWithExtraHeaders() throws IOException {
        SidecarInvocationTestCase src = suite.getCase("access is granted with extra headers");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }

    @Test
    public void testAccessIsDenied() throws IOException {
        SidecarInvocationTestCase src = suite.getCase("access is denied");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }

    @Test
    public void test500IsSentIfInvocationFails() throws IOException {
        SidecarInvocationTestCase src = suite.getCase("invocation fails");
        assertNotNull(src);

        verifyPreProcessorCase(src);
    }


}
