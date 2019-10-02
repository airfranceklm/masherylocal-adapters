package com.airfranceklm.amt.sidecar;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class AFKLMSidecarFilteringTest extends AFKLMSidecarMockSupport {

    private static AFKLMSidecarCaseYAMLReader reader;

    @BeforeClass
    public static void initReader() {
        reader = new AFKLMSidecarCaseYAMLReader("./filtering-test.yml");
    }

    @Test
    public void testPositiveSelectionOnSingleHttpHeaderWithSuppressedHeadersInput() {
        SidecarRequestCase rc = reader.getRequestCase("Filtering on a single HTTP Headers with headers suppressed", "positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testPositiveSelectionOnSingleHttpHeader() {
        SidecarRequestCase rc = reader.getRequestCase("Filtering on a single HTTP Headers", "positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testPositiveFilteringOutOnSingleHttpHeader() {
        SidecarRequestCase rc = reader.getRequestCase("Filtering on a single HTTP Headers", "filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testHttpVerbFilterInclusiveSelection() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive Filtering on a HTTP verb", "positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testHttpVerbFilterInclusiveFilterOut() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive Filtering on a HTTP verb", "filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testHttpVerbFilterExclusiveSelection() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive Filtering on a HTTP verb", "positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testHttpVerbFilterExclusiveFilterOut() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive Filtering on a HTTP verb", "filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testResourcePathFilterSelection() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive filtering on a operation path", "positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testResourcePathFilterFiltersOut() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive filtering on a operation path", "filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testResourcePathExclusiveFilterSelection() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive filtering on a operation path", "positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testResourcePathExclusiveFilterFiltersOut() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive filtering on a operation path", "filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testPackageKeyFilterSelection() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive filtering on a package key", "positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testPackageKeyFilterFiltersOut() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive filtering on a package key", "filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testPackageKeyExclusiveFilterSelection() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive filtering on a package key", "positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testPackageKeyExclusiveFilterFiltersOut() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive filtering on a package key", "filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testOAuthScopeFilterSelection() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive filtering on scope", "positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }


    @Test
    public void testOAuthScopeFilterFiltersOut() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive filtering on scope", "filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testOAuthScopeFilterFiltersOutOnNull() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive filtering on scope", "filtered-out nulls confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testOAuthScopeExclusiveFilterSelection() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive filtering on a scope", "positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testOAuthScopeExclusiveFilterFiltersOut() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive filtering on a scope", "filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testOAuthScopeExclusiveFilterNullInclusion() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive filtering on a scope", "null inclusion confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    // ---------------------------------------
    // OAuth User Context filtering
    @Test
    public void testOAuthUserContextFilterSelection() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive filtering on userContext", "positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }


    @Test
    public void testOAuthUserContextFilterFiltersOut() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive filtering on userContext", "filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testOAuthUserContextFilterFiltersOutOnNull() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive filtering on userContext", "filtered-out nulls confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testOAuthUserContextExclusiveFilterSelection() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive filtering on a userContext", "positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testOAuthUserContextExclusiveFilterFiltersOut() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive filtering on a userContext", "filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testOAuthUserContextExclusiveFilterNullInclusion() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive filtering on a userContext", "null inclusion confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    // -------------------------------------------------------------
    // Application EAV

    @Test
    public void testApplicationEAVFilterSelection() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive filtering on eav", "positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }


    @Test
    public void testApplicationEAVFilterFiltersOut() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive filtering on eav", "filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testApplicationEAVFilterFiltersOutOnNull() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive filtering on eav", "filtered-out nulls confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testApplicationEAVExclusiveFilterSelection() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive filtering on a eav", "positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testApplicationEAVExclusiveFilterFiltersOut() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive filtering on a eav", "filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testApplicationEAVExclusiveFilterNullInclusion() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive filtering on a eav", "null inclusion confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    // ---------------------------------------
    // Package key EAVs

    @Test
    public void testPackageKeyEAVFilterSelection() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive filtering on packageKeyEAV", "positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }


    @Test
    public void testPackageKeyEAVFilterFiltersOut() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive filtering on packageKeyEAV", "filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testPackageKeyEAVFilterFiltersOutOnNull() {
        SidecarRequestCase rc = reader.getRequestCase("Inclusive filtering on packageKeyEAV", "filtered-out nulls confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testPackageKeyEAVExclusiveFilterSelection() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive filtering on a packageKeyEAV", "positive confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testPackageKeyEAVExclusiveFilterFiltersOut() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive filtering on a packageKeyEAV", "filtered-out confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

    @Test
    public void testPackageKeyEAVExclusiveFilterNullInclusion() {
        SidecarRequestCase rc = reader.getRequestCase("Exclusive filtering on a packageKeyEAV", "null inclusion confirmation");
        assertNotNull(rc);

        verifyPreProcessorRequestCase(rc);
    }

}
