package com.airfranceklm.amt.sidecar;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class SidecarPostprocessorTest extends SidecarMockSupport {



    @Test
    public void testBasicInvocation() throws IOException {
        autoVerify(new SidecarInvocationTestSuite(getClass()
                , "./base-postprocessor.yml"), "base post-processing scenario");
    }

    @Test
    public void testInvocationWithFullBody() throws IOException {
        autoVerify(new SidecarInvocationTestSuite(getClass()
                , "./base-postprocessor-offload.yml"), "base post-processing scenario");
    }
}
