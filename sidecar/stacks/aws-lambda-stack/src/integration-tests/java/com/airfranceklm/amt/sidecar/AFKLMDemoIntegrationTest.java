package com.airfranceklm.amt.sidecar;

public class AFKLMDemoIntegrationTest /*extends AFKLMSidecarMockSupport */ {
/*
    private static AFKLMSidecarCaseYAMLReader reader;

    @BeforeClass
    public static void initReader() {
        reader = new AFKLMSidecarCaseYAMLReader("./integration-aws-jwt.yml");
    }

    @Test @Ignore("Ignored for automatic running")
    public void testInvokingAWS() {
        SidecarRequestCase rc = reader.getRequestCase("Integration JWT Validation Scenarios", "Lambda Denies");
        assertNotNull(rc);
        reader.applyEndpointConfigurationPrivateSecrets(rc, "/aws-credentials.yml", "integration-test");

        TestContext tc = createTestContextFrom(rc);

        PreProcessEvent ppe = createPreProcessorMock(tc);
        AFKLMSidecarProcessor processor = new AFKLMSidecarProcessor();

        replayAll();

        processor.handleEvent(ppe);
        verifyAll();
    }

    @Test @Ignore("Ignored for automatic running")
    public void invokeVishalSamlpeJWTFunction() {
        SidecarRequestCase rc = reader.getRequestCase("Real JWT Validation", "positive");
        assertNotNull(rc);
        reader.applyEndpointConfigurationPrivateSecrets(rc, "/aws-credentials.yml", "vishal-functions");

        TestContext tc = createTestContextFrom(rc);

        PreProcessEvent ppe = createPreProcessorMock(tc);
        AFKLMSidecarProcessor processor = new AFKLMSidecarProcessor();

        replayAll();

        processor.handleEvent(ppe);
        verifyAll();
    }


 */

}
