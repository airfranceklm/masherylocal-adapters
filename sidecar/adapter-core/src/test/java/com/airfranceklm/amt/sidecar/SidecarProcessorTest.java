package com.airfranceklm.amt.sidecar;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class SidecarProcessorTest extends SidecarMockSupport {

    private static SidecarInvocationTestSuite suite;
    private static SidecarInvocationTestSuite maxSizeSuite;
    private static SidecarInvocationTestSuite eavSuite;
    private static SidecarInvocationTestSuite operationSuite;
    private static SidecarInvocationTestSuite pkEavSuite;
    private static SidecarInvocationTestSuite authCtzSuite;

    @BeforeClass
    public static void initSuite() {
        suite = new SidecarInvocationTestSuite();
        suite.loadCasesFrom(SidecarProcessorTest.class.getResourceAsStream("./base-test.yml"));

        operationSuite = new SidecarInvocationTestSuite(SidecarProcessorTest.class, "./base-test-operation.yml");
        maxSizeSuite = new SidecarInvocationTestSuite(SidecarProcessorTest.class, "./base-test-max-size.yml");
        eavSuite = new SidecarInvocationTestSuite(SidecarProcessorTest.class, "./base-test-app-eavs.yml");
        pkEavSuite = new SidecarInvocationTestSuite(SidecarProcessorTest.class, "./base-test-pk-eav.yml");
        authCtzSuite = new SidecarInvocationTestSuite(SidecarProcessorTest.class, "./base-test-token-data.yml");
    }

    /**
     * Tests the basic sending 403 back to the client after lambda pre-processor invocation.
     */
    @Test
    public void testBasicSending403() throws IOException {
        SidecarInvocationTestCase rc = suite.getCase("base rejection scenario");
        if (rc == null) {
            fail("Cannot find the expected request case");
        }

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testSending403WithCodeAlone() {
        SidecarInvocationTestCase rc = suite.getCase("rejection with single code");
        if (rc == null) {
            fail("Cannot find the expected request case");
        }

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testSending403WithPayload() {
        SidecarInvocationTestCase rc = suite.getCase("rejection with payload");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testSending403WithJSON() {
        SidecarInvocationTestCase rc = suite.getCase("rejection with json and content type");
        if (rc == null) {
            fail("Cannot find the expected request case");
        }

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testSending403WithJSONWithoutExplicitContentType() {
        SidecarInvocationTestCase rc = suite.getCase("rejection with json and without content type");
        if (rc == null) {
            fail("Cannot find the expected request case");
        }

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testSending500OnInternalProblem() {
        SidecarInvocationTestCase rc = suite.getCase("error reporting on internal problem");
        assertNotNull("Cannot find the expected request case", rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testContinueAfterFaultOfFailSafeFunction() {
        SidecarInvocationTestSuite mSuite = new SidecarInvocationTestSuite(SidecarProcessorTest.class, "./base-test-failsafe.yml");
        autoVerify(mSuite, "continues on receiving internal error");
    }

    @Test
    public void testMaxSizeBlockingCorrectlyAllows() {
        SidecarInvocationTestCase rc = maxSizeSuite.getCase("fitting payload");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testMaxSizeBlockingCorrectlyBlocks() {
        SidecarInvocationTestCase rc = maxSizeSuite.getCase("exceeding payload");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testPassingFunctionParameters() throws IOException {
        SidecarInvocationTestSuite mSuite = new SidecarInvocationTestSuite(getClass(), "./base-test-params.yml");
        SidecarInvocationTestCase rc = mSuite.getCase("positively passing");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testOperationExpansionWithQueryString() throws IOException {
        SidecarInvocationTestCase rc = operationSuite.getCase("correct extraction");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testOperationExpansionWithoutQueryString() throws IOException {
        SidecarInvocationTestCase rc = operationSuite.getCase("correct extraction without query string");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);

    }

    @Test
    public void testAttributionOfRemoteAddress() {
        SidecarInvocationTestSuite mSuite = new SidecarInvocationTestSuite(getClass(), "base-test-remote-addr.yml");
        SidecarInvocationTestCase rc = mSuite.getCase("correct remote addr attribution");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);

    }


    @Test
    public void testTokenExpansion() {
        SidecarInvocationTestCase rc = authCtzSuite.getCase("expansion of token");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);

    }

    @Test
    public void testTokenExpansionWithTokenDataMissing() throws IOException {
        SidecarInvocationTestCase rc = authCtzSuite.getCase("expansion with token data missing");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);

    }

    @Test
    public void testRoutingExpansion() throws IOException {
        SidecarInvocationTestSuite mSuite = new SidecarInvocationTestSuite(getClass(), "./base-test-routing.yml");
        SidecarInvocationTestCase rc = mSuite.getCase("expansion of routing");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);

    }

    @Test
    public void testPackageKeyAttributeExpansion() throws IOException {
        SidecarInvocationTestCase rc = pkEavSuite.getCase("sending correct message with PkC missing");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);

    }

    @Test
    public void testWilLSend400IfRequiredPackageKeyEAVIsEmpty() throws IOException {
        SidecarInvocationTestCase rc = pkEavSuite.getCase("send 400 when required package key being empty");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);

    }

    @Test
    public void testAppAttributeExpansion() throws IOException {
        SidecarInvocationTestCase rc = eavSuite.getCase("sending correct message with PkC missing");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);

    }

    @Test
    public void testWilLSend400IfApplicationEAVIsEmpty() throws IOException {
        SidecarInvocationTestCase rc = eavSuite.getCase("send 400 when required eav being empty");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);

    }

    @Test
    public void testWilLSend400IfRequiredApplicationEAVIsNull() throws IOException {
        SidecarInvocationTestCase rc = eavSuite.getCase("send 400 when required eav is null");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);

    }

}
