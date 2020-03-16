package com.airfranceklm.amt.sidecar;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class SidecarFilteringTest extends SidecarMockSupport {

    private static SidecarInvocationTestSuite suite;
    private static SidecarInvocationTestSuite httpHeaderSuite;
    private static SidecarInvocationTestSuite httpVerbSuite;
    private static SidecarInvocationTestSuite httpVerbExclSuite;
    private static SidecarInvocationTestSuite operationPathSuite;
    private static SidecarInvocationTestSuite operationExclPathSuite;

    private static SidecarInvocationTestSuite inclPkSuite;
    private static SidecarInvocationTestSuite exclPkSuite;

    private static SidecarInvocationTestSuite inclScopeSuite;
    private static SidecarInvocationTestSuite exclScopeSuite;

    private static SidecarInvocationTestSuite inclContextSuite;
    private static SidecarInvocationTestSuite exclContextSuite;

    private static SidecarInvocationTestSuite inclEavSuite;
    private static SidecarInvocationTestSuite exclEavSuite;

    private static SidecarInvocationTestSuite inclPkEavSuite;
    private static SidecarInvocationTestSuite exclPkEavSuite;



    @BeforeClass
    public static void init() {
        suite = new SidecarInvocationTestSuite(SidecarFilteringTest.class, "./filtering/filtering-test.yml");

        httpHeaderSuite = new SidecarInvocationTestSuite(SidecarFilteringTest.class, "./filtering/single-http-header.yml");
        httpVerbSuite = new SidecarInvocationTestSuite(SidecarFilteringTest.class, "./filtering/inclusive-http-verb.yml");
        httpVerbExclSuite = new SidecarInvocationTestSuite(SidecarFilteringTest.class, "./filtering/exclusive-http-verb.yml");
//        suite.loadCasesFrom(SidecarFilteringTest.class.getResourceAsStream("./filtering-test.yml"));

        operationPathSuite =  new SidecarInvocationTestSuite(SidecarFilteringTest.class, "./filtering/inclusive-operationpath.yml");
        operationExclPathSuite =  new SidecarInvocationTestSuite(SidecarFilteringTest.class, "./filtering/exclusive-operationpath.yml");

        inclPkSuite =  new SidecarInvocationTestSuite(SidecarFilteringTest.class, "./filtering/inclusive-pk.yml");
        exclPkSuite =  new SidecarInvocationTestSuite(SidecarFilteringTest.class, "./filtering/exclusive-pk.yml");

        inclScopeSuite =  new SidecarInvocationTestSuite(SidecarFilteringTest.class, "./filtering/inclusive-scope.yml");
        exclScopeSuite =  new SidecarInvocationTestSuite(SidecarFilteringTest.class, "./filtering/exclusive-scope.yml");

        inclContextSuite =  new SidecarInvocationTestSuite(SidecarFilteringTest.class, "./filtering/inclusive-usercontext.yml");
        exclContextSuite =  new SidecarInvocationTestSuite(SidecarFilteringTest.class, "./filtering/exclusive-usercontext.yml");

        inclEavSuite =  new SidecarInvocationTestSuite(SidecarFilteringTest.class, "./filtering/inclusive-eav.yml");
        exclEavSuite =  new SidecarInvocationTestSuite(SidecarFilteringTest.class, "./filtering/exclusive-eav.yml");

        inclPkEavSuite =  new SidecarInvocationTestSuite(SidecarFilteringTest.class, "./filtering/inclusive-pkeav.yml");
        exclPkEavSuite =  new SidecarInvocationTestSuite(SidecarFilteringTest.class, "./filtering/exclusive-pkeav.yml");
    }

    @Test
    public void testPositiveSelectionOnSingleHttpHeaderWithSuppressedHeadersInput() {

        SidecarInvocationTestSuite mSuite = new SidecarInvocationTestSuite(getClass(), "./filtering/single-header-suppressed.yml");
        autoVerify(mSuite, "positive confirmation");
    }

    @Test
    public void testPositiveSelectionOnSingleHttpHeader() {
        SidecarInvocationTestCase rc = httpHeaderSuite.getCase("positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testPositiveFilteringOutOnSingleHttpHeader() {
        SidecarInvocationTestCase rc = httpHeaderSuite.getCase("filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testHttpVerbFilterInclusiveSelection() {
        SidecarInvocationTestCase rc = httpVerbSuite.getCase("positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testHttpVerbFilterInclusiveFilterOut() {
        SidecarInvocationTestCase rc = httpVerbSuite.getCase("filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testHttpVerbFilterExclusiveSelection() {
        SidecarInvocationTestCase rc = httpVerbExclSuite.getCase("positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testHttpVerbFilterExclusiveFilterOut() {
        SidecarInvocationTestCase rc = httpVerbExclSuite.getCase("filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testResourcePathFilterSelection() {
        SidecarInvocationTestCase rc = operationPathSuite.getCase("positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testResourcePathFilterFiltersOut() {
        SidecarInvocationTestCase rc = operationPathSuite.getCase("filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testResourcePathExclusiveFilterSelection() {
        SidecarInvocationTestCase rc = operationExclPathSuite.getCase("positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testResourcePathExclusiveFilterFiltersOut() {
        SidecarInvocationTestCase rc = operationExclPathSuite.getCase("filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testPackageKeyFilterSelection() {
        SidecarInvocationTestCase rc = inclPkSuite.getCase("positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testPackageKeyFilterFiltersOut() {
        SidecarInvocationTestCase rc = inclPkSuite.getCase("filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testPackageKeyExclusiveFilterSelection() {
        SidecarInvocationTestCase rc = exclPkSuite.getCase("positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testPackageKeyExclusiveFilterFiltersOut() {
        SidecarInvocationTestCase rc = exclPkSuite.getCase("filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testOAuthScopeFilterSelection() {
        SidecarInvocationTestCase rc = inclScopeSuite.getCase("positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }


    @Test
    public void testOAuthScopeFilterFiltersOut() {
        SidecarInvocationTestCase rc = inclScopeSuite.getCase("filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testOAuthScopeFilterFiltersOutOnNull() {
        SidecarInvocationTestCase rc = inclScopeSuite.getCase("filtered-out nulls confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testOAuthScopeExclusiveFilterSelection() {
        SidecarInvocationTestCase rc = exclScopeSuite.getCase("positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testOAuthScopeExclusiveFilterFiltersOut() {
        SidecarInvocationTestCase rc = exclScopeSuite.getCase("filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testOAuthScopeExclusiveFilterNullInclusion() {
        SidecarInvocationTestCase rc = exclScopeSuite.getCase("null inclusion confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    // ---------------------------------------
    // OAuth User Context filtering
    @Test
    public void testOAuthUserContextFilterSelection() {
        SidecarInvocationTestCase rc = inclContextSuite.getCase("positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }


    @Test
    public void testOAuthUserContextFilterFiltersOut() {
        SidecarInvocationTestCase rc = inclContextSuite.getCase("filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testOAuthUserContextFilterFiltersOutOnNull() {
        SidecarInvocationTestCase rc = inclContextSuite.getCase("filtered-out nulls confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testOAuthUserContextExclusiveFilterSelection() {
        SidecarInvocationTestCase rc = exclContextSuite.getCase("positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testOAuthUserContextExclusiveFilterFiltersOut() {
        SidecarInvocationTestCase rc = exclContextSuite.getCase("filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testOAuthUserContextExclusiveFilterNullInclusion() {
        SidecarInvocationTestCase rc = exclContextSuite.getCase("null inclusion confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    // -------------------------------------------------------------
    // Application EAV

    @Test
    public void testApplicationEAVFilterSelection() {
        SidecarInvocationTestCase rc = inclEavSuite.getCase("positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }


    @Test
    public void testApplicationEAVFilterFiltersOut() {
        SidecarInvocationTestCase rc = inclEavSuite.getCase("filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testApplicationEAVFilterFiltersOutOnNull() {
        SidecarInvocationTestCase rc = inclEavSuite.getCase("filtered-out nulls confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testApplicationEAVExclusiveFilterSelection() {
        SidecarInvocationTestCase rc = exclEavSuite.getCase("positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testApplicationEAVExclusiveFilterFiltersOut() {
        SidecarInvocationTestCase rc = exclEavSuite.getCase("filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testApplicationEAVExclusiveFilterNullInclusion() {
        SidecarInvocationTestCase rc = exclEavSuite.getCase("null inclusion confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    // ---------------------------------------
    // Package key EAVs

    @Test
    public void testPackageKeyEAVFilterSelection() {
        SidecarInvocationTestCase rc = inclPkEavSuite.getCase("positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }


    @Test
    public void testPackageKeyEAVFilterFiltersOut() {
        SidecarInvocationTestCase rc = inclPkEavSuite.getCase("filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testPackageKeyEAVFilterFiltersOutOnNull() {
        SidecarInvocationTestCase rc = inclPkEavSuite.getCase("filtered-out nulls confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testPackageKeyEAVExclusiveFilterSelection() {
        SidecarInvocationTestCase rc = exclPkEavSuite.getCase("positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testPackageKeyEAVExclusiveFilterFiltersOut() {
        SidecarInvocationTestCase rc = exclPkEavSuite.getCase("filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testPackageKeyEAVExclusiveFilterNullInclusion() {
        SidecarInvocationTestCase rc = exclPkEavSuite.getCase("null inclusion confirmation");
        assertNotNull(rc);

        verifyPreProcessorCase(rc);
    }

    @Test
    public void testMulitHeaderFilteringInclusive() {
        autoVerify(suite, "positive confirmation");
    }

    @Test
    public void testMulitHeaderFilteringFilteringOut() {
        autoVerify(suite, "filtered-out confirmation");
    }

}
