package com.airfranceklm.amt.sidecar;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class AFKLMSidecarPostprocessorTest extends AFKLMSidecarMockSupport {

    private static AFKLMSidecarCaseYAMLReader reader;

    @BeforeClass
    public static void initReader() {
        reader = new AFKLMSidecarCaseYAMLReader("./base-postprocessor.yml");
    }

    @Test
    public void testBasicInvocation() {
        SidecarRequestCase rc = reader.getRequestCase("Smoke Test", "base post-processing scenario");
        assertNotNull(rc);

        verifyPostProcessorRequestCase(rc);
    }

    @Test
    public void testInvocationWithFullBody() {
        SidecarRequestCase rc = reader.getRequestCase("Complete Offload Example", "base post-processing scenario");
        assertNotNull(rc);

        verifyPostProcessorRequestCase(rc);
    }
}
