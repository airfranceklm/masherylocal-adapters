package com.airfranceklm.amt.phase1;

import com.airfranceklm.amt.sidecar.AFKLMSidecarCaseYAMLReader;
import com.airfranceklm.amt.sidecar.AFKLMSidecarMockSupport;
import com.airfranceklm.amt.sidecar.SidecarRequestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

public class DynamicBackendAuthorizationTest extends AFKLMSidecarMockSupport {

    static AFKLMSidecarCaseYAMLReader reader;

    @BeforeClass
    public static void initReader() {
        reader = new AFKLMSidecarCaseYAMLReader("/phase-1/dynamic_backend_authorization.yml");
    }

    @Test
    public void testAuthenticationSucceeds() {
        SidecarRequestCase src = reader.getRequestCase("Dynamic endpoint authorization", "authentication succeeds");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }

    @Test
    public void testAuthenticationFails() {
        SidecarRequestCase src = reader.getRequestCase("Dynamic endpoint authorization", "authentication fails");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }

    @Test
    public void test500ReturnedOnInvocationFailure() {
        SidecarRequestCase src = reader.getRequestCase("Dynamic endpoint authorization", "invocation fails");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }
}
