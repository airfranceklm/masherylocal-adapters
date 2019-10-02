package com.airfranceklm.amt.phase1;

import com.airfranceklm.amt.sidecar.AFKLMSidecarCaseYAMLReader;
import com.airfranceklm.amt.sidecar.AFKLMSidecarMockSupport;
import com.airfranceklm.amt.sidecar.SidecarRequestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

public class InstantMassiveLogoutTest extends AFKLMSidecarMockSupport {
    static AFKLMSidecarCaseYAMLReader reader;

    @BeforeClass
    public static void initReader() {
        reader = new AFKLMSidecarCaseYAMLReader("/phase-1/instant_mass_logout.yml");
    }

    @Test
    public void testAccessIsGranted() {
        SidecarRequestCase src = reader.getRequestCase("Instant massive logout", "access is granted");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }

    @Test
    public void test500IsReturnedOnFailure() {
        SidecarRequestCase src = reader.getRequestCase("Instant massive logout", "invocation failure");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }

    @Test
    public void testAccessIsDenied() {
        SidecarRequestCase src = reader.getRequestCase("Instant massive logout", "access is denied");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }
}
