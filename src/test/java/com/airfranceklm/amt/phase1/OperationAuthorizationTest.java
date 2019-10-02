package com.airfranceklm.amt.phase1;

import com.airfranceklm.amt.sidecar.AFKLMSidecarCaseYAMLReader;
import com.airfranceklm.amt.sidecar.AFKLMSidecarMockSupport;
import com.airfranceklm.amt.sidecar.SidecarRequestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

public class OperationAuthorizationTest extends AFKLMSidecarMockSupport  {
    static AFKLMSidecarCaseYAMLReader reader;

    @BeforeClass
    public static void initReader() {
        reader = new AFKLMSidecarCaseYAMLReader("/phase-1/operation_authorization.yml");
    }

    @Test
    public void testAccessIsGranted() {
        SidecarRequestCase src = reader.getRequestCase("Operation Authorization", "access is granted");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }

    @Test
    public void testAccessIsDenied() {
        SidecarRequestCase src = reader.getRequestCase("Operation Authorization", "access is denied");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }

    @Test
    public void test500IsSentIfErrorOccurs() {
        SidecarRequestCase src = reader.getRequestCase("Operation Authorization", "invocation failed");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }

    @Test
    public void testAccessGrantedToIndividualUser() {
        SidecarRequestCase src = reader.getRequestCase("User-level Operation Authorization", "access is granted to individual user");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }

    @Test
    public void testAccessGrantedToIndividualUserAndToken() {
        SidecarRequestCase src = reader.getRequestCase("User-level Operation Authorization with Token", "access is granted to individual user token");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }
}
