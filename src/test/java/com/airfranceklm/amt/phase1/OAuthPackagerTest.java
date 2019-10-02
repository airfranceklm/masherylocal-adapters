package com.airfranceklm.amt.phase1;

import com.airfranceklm.amt.sidecar.AFKLMSidecarCaseYAMLReader;
import com.airfranceklm.amt.sidecar.AFKLMSidecarMockSupport;
import com.airfranceklm.amt.sidecar.SidecarRequestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

public class OAuthPackagerTest extends AFKLMSidecarMockSupport {

    static AFKLMSidecarCaseYAMLReader reader;

    @BeforeClass
    public static void initReader() {
        reader = new AFKLMSidecarCaseYAMLReader("/phase-1/oauth-packager.yml");
    }

    @Test
    public void testAccessIsGranted() {
        SidecarRequestCase src = reader.getRequestCase("OAuth Packager", "access is granted");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }

    @Test
    public void testAccessIsGrantedWithExtraHeaders() {
        SidecarRequestCase src = reader.getRequestCase("OAuth Packager", "access is granted with extra headers");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }

    @Test
    public void testAccessIsDenied() {
        SidecarRequestCase src = reader.getRequestCase("OAuth Packager", "access is denied");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }

    @Test
    public void test500IsSentIfInvocationFails() {
        SidecarRequestCase src = reader.getRequestCase("OAuth Packager", "invocation fails");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }


}
