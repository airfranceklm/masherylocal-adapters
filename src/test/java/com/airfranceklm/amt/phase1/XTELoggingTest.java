package com.airfranceklm.amt.phase1;

import com.airfranceklm.amt.sidecar.AFKLMSidecarCaseYAMLReader;
import com.airfranceklm.amt.sidecar.AFKLMSidecarMockSupport;
import com.airfranceklm.amt.sidecar.SidecarRequestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

public class XTELoggingTest extends AFKLMSidecarMockSupport {
    static AFKLMSidecarCaseYAMLReader reader;

    @BeforeClass
    public static void initReader() {
        reader = new AFKLMSidecarCaseYAMLReader("/phase-1/xte_logging.yml");
    }

    @Test
    public void testPassAlongWithoutModification() {
        SidecarRequestCase src = reader.getRequestCase("XTE Logging", "pass-along without modifications");
        assertNotNull(src);

        verifyPostProcessorRequestCase(src);
    }

    @Test
    public void testRejectingDeliveryToTheAPIClient() {
        SidecarRequestCase src = reader.getRequestCase("XTE Logging", "rejecting delivery to API client");
        assertNotNull(src);

        verifyPostProcessorRequestCase(src);
    }

    @Test
    public void test500WillBeReturnedIfInvocationWillFail() {
        SidecarRequestCase src = reader.getRequestCase("XTE Logging", "invocation failure");
        assertNotNull(src);

        verifyPostProcessorRequestCase(src);
    }
}
