package com.airfranceklm.amt.phase1;

import com.airfranceklm.amt.sidecar.SidecarInvocationTestSuite;
import com.airfranceklm.amt.sidecar.SidecarMockSupport;
import com.airfranceklm.amt.sidecar.SidecarInvocationTestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertNotNull;

public class AsyncAnalyticsTest extends SidecarMockSupport {

    private static SidecarInvocationTestSuite suite;

    @BeforeClass
    public static void init() {
        suite = new SidecarInvocationTestSuite();
        suite.loadCasesFrom(AsyncAnalyticsTest.class.getResourceAsStream("/phase-1/async_analytics.yml"));
    }

    @Test
    public void testPayloadExtractionOnGet() throws IOException {
        SidecarInvocationTestCase src = suite.getCase("payload extraction on get");
        assertNotNull(src);

        verifyPostProcessorCase(src);
    }

    @Test
    public void testPayloadExtractionOnPost() throws IOException {
        SidecarInvocationTestCase src = suite.getCase("payload extraction on post");
        assertNotNull(src);

        verifyPostProcessorCase(src);
    }

    @Test
    public void testNoEffectIfInvocationWillFail() throws IOException {
        SidecarInvocationTestCase src = suite.getCase("invocation failure");
        assertNotNull(src);

        verifyPostProcessorCase(src);
    }
}
