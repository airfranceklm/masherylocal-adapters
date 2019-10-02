package com.airfranceklm.amt.phase1;

import com.airfranceklm.amt.sidecar.AFKLMSidecarCaseYAMLReader;
import com.airfranceklm.amt.sidecar.AFKLMSidecarMockSupport;
import com.airfranceklm.amt.sidecar.SidecarRequestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

public class AsyncAnalyticsTest extends AFKLMSidecarMockSupport {
    static AFKLMSidecarCaseYAMLReader reader;

    @BeforeClass
    public static void initReader() {
        reader = new AFKLMSidecarCaseYAMLReader("/phase-1/async_analytics.yml");
    }

    @Test
    public void testPayloadExtractionOnGet() {
        SidecarRequestCase src = reader.getRequestCase("Asynchronous analytics", "payload extraction on get");
        assertNotNull(src);

        verifyPostProcessorRequestCase(src);
    }

    @Test
    public void testPayloadExtractionOnPost() {
        SidecarRequestCase src = reader.getRequestCase("Asynchronous analytics", "payload extraction on post");
        assertNotNull(src);

        verifyPostProcessorRequestCase(src);
    }

    @Test
    public void testNoEffectIfInvocationWillFail() {
        SidecarRequestCase src = reader.getRequestCase("Asynchronous analytics", "invocation failure");
        assertNotNull(src);

        verifyPostProcessorRequestCase(src);
    }
}
