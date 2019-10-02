package com.airfranceklm.amt.phase1;

import com.airfranceklm.amt.sidecar.AFKLMSidecarCaseYAMLReader;
import com.airfranceklm.amt.sidecar.AFKLMSidecarMockSupport;
import com.airfranceklm.amt.sidecar.SidecarRequestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

public class CustomHeaderBasedAuthorizationTest extends AFKLMSidecarMockSupport {

    static AFKLMSidecarCaseYAMLReader reader;

    @BeforeClass
    public static void initReader() {
        reader = new AFKLMSidecarCaseYAMLReader("/phase-1/custom-headerbased-authn.yml");
    }

    @Test
    public void testNegativeConclusion() {
        SidecarRequestCase src = reader.getRequestCase("Custom Header-Based Authentication", "negative conclusion");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }

    @Test
    public void testPositiveConclusion() {
        SidecarRequestCase src = reader.getRequestCase("Custom Header-Based Authentication", "positive conclusion");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }

    @Test
    public void testPositiveConclusionWithAdditionalAuthorization() {
        SidecarRequestCase src = reader.getRequestCase("Custom Header-Based Authentication", "positive conclusion with additional authorization");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }

    @Test
    public void test500ReturnedOnInvocationFailure() {
        SidecarRequestCase src = reader.getRequestCase("Custom Header-Based Authentication", "invocation failure");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }

    // ----------------------------------
    // Header-based authentication with pre-shared signature keys

    @Test
    public void testSuccessfulB2EValidation() {
        SidecarRequestCase src = reader.getRequestCase("Authorization Servers issued JWT Tokens Validation", "authorization granted");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }

    @Test
    public void testB2EAuthorizationIsDenied() {
        SidecarRequestCase src = reader.getRequestCase("Authorization Servers issued JWT Tokens Validation", "authorization denied");
        assertNotNull(src);

        verifyPreProcessorRequestCase(src);
    }
}
