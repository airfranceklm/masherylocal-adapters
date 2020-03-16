package com.airfranceklm.amt.phase1;

import com.airfranceklm.amt.sidecar.SidecarInvocationTestSuite;
import com.airfranceklm.amt.sidecar.SidecarMockSupport;
import com.airfranceklm.amt.sidecar.SidecarInvocationTestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertNotNull;

public class XTELoggingTest extends SidecarMockSupport {

    private static SidecarInvocationTestSuite suite;

    @BeforeClass
    public static void init() {
        suite = new SidecarInvocationTestSuite();
        suite.loadCasesFrom(XTELoggingTest.class.getResourceAsStream("/phase-1/xte_logging.yml"));
    }

    @Test
    public void testPassAlongWithoutModification() throws IOException {
        SidecarInvocationTestCase src = suite.getCase("pass-along without modifications");
        assertNotNull(src);

        verifyPostProcessorCase(src);
    }

    @Test
    public void testRejectingDeliveryToTheAPIClient() throws IOException {
        SidecarInvocationTestCase src = suite.getCase("rejecting delivery to API client");
        assertNotNull(src);

        verifyPostProcessorCase(src);
    }

    @Test
    public void test500WillBeReturnedIfInvocationWillFail() throws IOException {
        SidecarInvocationTestCase src = suite.getCase("invocation failure");
        assertNotNull(src);

        verifyPostProcessorCase(src);
    }
}
