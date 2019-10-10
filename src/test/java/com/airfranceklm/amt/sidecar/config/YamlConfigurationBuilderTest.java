package com.airfranceklm.amt.sidecar.config;

import org.junit.Test;

import java.util.Iterator;

import static com.airfranceklm.amt.sidecar.config.ConfigRequirement.Included;
import static com.airfranceklm.amt.sidecar.config.ConfigRequirement.Required;
import static com.airfranceklm.amt.sidecar.config.InputScopeExpansion.*;
import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.loadAllYamlDocuments;
import static junit.framework.Assert.*;

public class YamlConfigurationBuilderTest {


    @Test
    public void testLoadingSynchronicity() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/synchronicity.yml");

        // First check: event synchronicity.

        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertEquals(SidecarSynchronicity.Event, cfg.getSynchronicity());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertEquals(SidecarSynchronicity.RequestResponse, cfg.getSynchronicity());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertEquals(SidecarSynchronicity.NonBlockingEvent, cfg.getSynchronicity());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertEquals(SidecarSynchronicity.Event, cfg.getSynchronicity());
    }

    @Test
    public void testLoadingFailsafe() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/failsafe.yml");

        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.isFailsafe());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.isFailsafe());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.isFailsafe());
    }

    @Test
    public void testLoadingPreflightEnabled() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/preflightEnabled.yml");

        // First check: event synchronicity.

        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.demandsPreflightHandling());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());
    }

    @Test
    public void testLoadingIdempotentAware() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/idempotentAware.yml");

        // First check: event synchronicity.

        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.isIdempotentAware());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.isIdempotentAware());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.isIdempotentAware());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.isIdempotentAware());
    }

    @Test
    public void testLoadingStack() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/stack.yml");

        // First case: stack not mentioned.
        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertEquals("http", cfg.getStack());
        assertNull(cfg.getStackParamsWithoutNullChecks());

        // Second case: reference to the null object
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertEquals("http", cfg.getStack());
        assertNull(cfg.getStackParamsWithoutNullChecks());

        // Third case: reference to aws stack
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertEquals("aws", cfg.getStack());
        assertNull(cfg.getStackParamsWithoutNullChecks());

        // Fourth case: reference to aws stack
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertEquals("http", cfg.getStack());
        assertNotNull(cfg.getStackParamsWithoutNullChecks());
        assertEquals(1, cfg.getStackParams().size());
        assertEquals("http://localhost:8080", cfg.getStackParams().get("uri"));

        // Fifth  case: reference to aws stack
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertEquals("aws", cfg.getStack());
        assertNotNull(cfg.getStackParamsWithoutNullChecks());
        assertEquals(2, cfg.getStackParams().size());
        assertEquals("kk32", cfg.getStackParams().get("key"));
        assertEquals("gz234", cfg.getStackParams().get("functionARN"));
    }

    @Test
    public void testSidecarParams() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/sidecarParams.yml");

        // First case: stack not mentioned.
        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertNull(cfg.getSidecarParams());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertNotNull(cfg.getSidecarParams());
        assertEquals(3, cfg.getSidecarParams().size());
        assertEquals("this is string", cfg.getSidecarParams().get("p1"));
        assertEquals(234, cfg.getSidecarParams().get("p2"));
        assertEquals(true, cfg.getSidecarParams().get("p3"));
    }

    @Test
    public void testSidecarInputExpansion() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/sidecarInputExpansion.yml");

        // First case: stack not mentioned.
        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.needsNoExpansion());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.needsExpansionOf(RemoteAddress));

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.GrantType));

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.TokenScope));

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.Token));

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.FullToken));

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.RequestVerb));

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.Operation));

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.AllRequestHeaders));

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.RequestPayload));

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.Routing));

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.RelayParams));

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.AllResponseHeaders));

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.ResponsePayload));
    }

    @Test
    public void testApplicationEAVReading() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/applicationEAVs.yml");

        // First case: stack not mentioned.
        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.requiresNoApplicationEAVs());
        assertFalse(cfg.needsExpansionOf(InputScopeExpansion.ApplicationEAVs));

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.requiresNoApplicationEAVs());
        assertFalse(cfg.needsExpansionOf(InputScopeExpansion.ApplicationEAVs));

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.requiresNoApplicationEAVs());
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.ApplicationEAVs));

        assertTrue(cfg.requiresApplicationEAV("EAV1", Required));
        assertTrue(cfg.requiresApplicationEAV("EAV2", Required));
        assertTrue(cfg.requiresApplicationEAV("EAV3", Included));
        assertTrue(cfg.requiresApplicationEAV("EAV4", Included));
        assertTrue(cfg.requiresApplicationEAV("EAV5", Included));

    }

    @Test
    public void testPackageKeyEAVReading() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/packageKeyEAVs.yml");

        // First case: stack not mentioned.
        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.requiresNoPackageKeyEAVs());
        assertFalse(cfg.needsExpansionOf(InputScopeExpansion.PackageKeyEAVS));

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.requiresNoPackageKeyEAVs());
        assertFalse(cfg.needsExpansionOf(InputScopeExpansion.PackageKeyEAVS));

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.requiresNoPackageKeyEAVs());
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.PackageKeyEAVS));

        assertTrue(cfg.requiresPackageKeyEAVs("EAV1", Required));
        assertTrue(cfg.requiresPackageKeyEAVs("EAV2", Required));
        assertTrue(cfg.requiresPackageKeyEAVs("EAV3", Included));
        assertTrue(cfg.requiresPackageKeyEAVs("EAV4", Included));
        assertTrue(cfg.requiresPackageKeyEAVs("EAV5", Included));
    }

    @Test
    public void testSkipRequestHeaders() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/skipRequestHeaders.yml");

        // First case: Not defined.
        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.skipsRequestHeaders());
        assertFalse(cfg.needsExpansionOf(InputScopeExpansion.RequestHeaders));

        //Second  case: Defined as a null object.
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.skipsRequestHeaders());
        assertFalse(cfg.needsExpansionOf(InputScopeExpansion.RequestHeaders));

        // Third case: Defined correctly.
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.skipsRequestHeaders());
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.RequestHeaders));
        assertTrue(cfg.skipsRequestHeader("x-afklm-h1"));

        //Fourth case: List is malformed
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.skipsRequestHeaders());
        assertFalse(cfg.needsExpansionOf(InputScopeExpansion.RequestHeaders));
    }

    @Test
    public void testSkipResponseHeaders() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/skipResponseHeaders.yml");

        // First case: Not defined.
        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PostProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.skipsResponseHeaders());
        assertFalse(cfg.needsExpansionOf(InputScopeExpansion.ResponseHeaders));

        //Second  case: Defined as a null object.
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PostProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.skipsResponseHeaders());
        assertFalse(cfg.needsExpansionOf(InputScopeExpansion.ResponseHeaders));

        // Third case: Defined correctly.
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PostProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.skipsResponseHeaders());
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.ResponseHeaders));
        assertTrue(cfg.skipsResponseHeader("x-afklm-h1"));

        //Fourth case: List is malformed
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PostProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.skipsResponseHeaders());
        assertFalse(cfg.needsExpansionOf(InputScopeExpansion.ResponseHeaders));
    }

    @Test
    public void testIncludeResponseHeaders() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/includeResponseHeaders.yml");

        // First case: Not defined.
        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PostProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.includesRequestHeaders());
        assertFalse(cfg.needsExpansionOf(InputScopeExpansion.ResponseHeaders));

        //Second  case: Defined as a null object.
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PostProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.skipsResponseHeaders());
        assertFalse(cfg.needsExpansionOf(InputScopeExpansion.ResponseHeaders));

        // Third case: Defined correctly.
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PostProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.includesResponseHeaders());
        assertTrue(cfg.includesResponseHeader("x-afklm-h1"));
        assertTrue(cfg.includesResponseHeader("x-afklm-h2"));
        assertTrue(cfg.includesResponseHeader("X-AFKLM-H2"));

        //Fourth case: List is malformed
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PostProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.skipsResponseHeaders());
    }

    @Test
    public void testRequireRequestHeaders() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/requireRequestHeaders.yml");

        // First case: Not defined.
        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.requiresRequestHeaders());
        assertFalse(cfg.includesRequestHeaders());
        assertFalse(cfg.needsExpansionOf(InputScopeExpansion.RequestHeaders));

        // Second case: defined as null objects
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.requiresRequestHeaders());
        assertFalse(cfg.includesRequestHeaders());
        assertFalse(cfg.needsExpansionOf(InputScopeExpansion.RequestHeaders));

        // Third case: defined require only.
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.requiresRequestHeaders());
        assertFalse(cfg.includesRequestHeaders());
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.RequestHeaders));
        assertTrue(cfg.demandsRequestHeader("x-afklm-h1", Required));
        assertTrue(cfg.demandsRequestHeader("x-afklm-h2", Required));
        assertTrue(cfg.demandsRequestHeader("x-AFKLM-h2", Required));

        // Forth case: defined with incorrect object type
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.requiresRequestHeaders());
        assertFalse(cfg.includesRequestHeaders());
        assertFalse(cfg.needsExpansionOf(InputScopeExpansion.RequestHeaders));

        // Fifth case: defined include only.
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.requiresRequestHeaders());
        assertTrue(cfg.includesRequestHeaders());
        assertTrue(cfg.needsExpansionOf(InputScopeExpansion.RequestHeaders));
        assertTrue(cfg.demandsRequestHeader("x-afklm-h1", Included));
        assertTrue(cfg.demandsRequestHeader("x-AFKLM-h1", Included));
        assertTrue(cfg.demandsRequestHeader("x-AFKLM-h2", Included));
        assertTrue(cfg.demandsRequestHeader("x-afklm-h2", Included));

        // Sixth case: defined with incorrect include type
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.requiresRequestHeaders());
        assertFalse(cfg.includesRequestHeaders());
        assertFalse(cfg.needsExpansionOf(InputScopeExpansion.RequestHeaders));
    }

    @Test
    public void testPreflightHeaders() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/preflightHeaders.yml");

        // First case: Not defined.
        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());

        // Second case: Only null objects.
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());

        // Third case: defined require headers.
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.demandsPreflightHandling());
        assertTrue(cfg.expandsPreflightHeaders());
        assertTrue(cfg.demandsPreflightHeader("x-afklm-h1", Required));
        assertTrue(cfg.demandsPreflightHeader("x-AFKLM-h1", Required));
        assertTrue(cfg.demandsPreflightHeader("x-AFKLM-h2", Required));
        assertTrue(cfg.demandsPreflightHeader("x-afklm-h2", Required));

        // Fifth case: Defined with error
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());

        // Sixth case: defined include headers.
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.demandsPreflightHandling());
        assertTrue(cfg.expandsPreflightHeaders());
        assertTrue(cfg.demandsPreflightHeader("x-afklm-h1", Included));
        assertTrue(cfg.demandsPreflightHeader("x-AFKLM-h1", Included));
        assertTrue(cfg.demandsPreflightHeader("x-AFKLM-h2", Included));
        assertTrue(cfg.demandsPreflightHeader("x-afklm-h2", Included));

        // 7th case: Defined with error in include
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());
    }


    @Test
    public void testPreflightParameters() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/preflightParams.yml");

        // First case: Not defined.
        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.demandsPreflightHandling());
        assertEquals(2, cfg.getPreflightParams().size());
        assertEquals("this is a string", cfg.getPreflightParams().get("str"));
        assertEquals(150, cfg.getPreflightParams().get("number"));
    }

    @Test
    public void testPreflightEAV() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/preflightEAVs.yml");

        // First case: Not defined.
        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());

        // Second case: defined as null objects
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());

        // Third case: defined with inconsistent types
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());

        // Third case: defined with inconsistent types
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.demandsPreflightHandling());
        assertTrue(cfg.demandsPreflightApplicationEAV("EAV1", Required));
        assertTrue(cfg.demandsPreflightApplicationEAV("EAV2", Required));
        assertFalse(cfg.demandsPreflightApplicationEAV("EAV3", Required));

        assertTrue(cfg.demandsPreflightApplicationEAV("EAV3", Included));
        assertTrue(cfg.demandsPreflightApplicationEAV("EAV4", Included));
        assertTrue(cfg.demandsPreflightApplicationEAV("EAV5", Included));
    }

    @Test
    public void testPreflightPackageKeyEAV() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/preflightPackageKeyEAVs.yml");

        // First case: Not defined.
        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());

        // Second case: Defined as null objects
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());

        // Third case: Type collision
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());

        // Fourth case: correct definition.
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.demandsPreflightHandling());
        assertTrue(cfg.demandsPreflightPackageKeyEAV("EAV1", Required));
        assertTrue(cfg.demandsPreflightPackageKeyEAV("EAV2", Required));
        assertFalse(cfg.demandsPreflightPackageKeyEAV("EAV3", Required));
        assertTrue(cfg.demandsPreflightPackageKeyEAV("EAV3", Included));
        assertTrue(cfg.demandsPreflightPackageKeyEAV("EAV4", Included));
        assertTrue(cfg.demandsPreflightPackageKeyEAV("EAV5", Included));
    }

    @Test
    public void testPreflightInputExpansion() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/preflightInputExpansion.yml");

        // First case: Not defined.
        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());

        // Second case: Remote address
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.demandsPreflightHandling());
        assertTrue(cfg.needsPreflightExpansionOf(RemoteAddress));

        // Third case: Grant type
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.demandsPreflightHandling());
        assertTrue(cfg.needsPreflightExpansionOf(GrantType));

        // Fourth case: token scope
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.demandsPreflightHandling());
        assertTrue(cfg.needsPreflightExpansionOf(TokenScope));

        // Fifth case: full tokens
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.demandsPreflightHandling());
        assertTrue(cfg.needsPreflightExpansionOf(Token));

        // 6th case: full tokens
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.demandsPreflightHandling());
        assertTrue(cfg.needsPreflightExpansionOf(FullToken));

        // 7th case: verb
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.demandsPreflightHandling());
        assertTrue(cfg.needsPreflightExpansionOf(RequestVerb));

        // 8th case: operation. This is not supported for preflight.
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());
        assertFalse(cfg.needsPreflightExpansionOf(Operation));
        assertTrue(cfg.hasErrors());

        // 9th case: operation. All request headers
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.demandsPreflightHandling());
        assertTrue(cfg.needsPreflightExpansionOf(AllRequestHeaders));
        assertFalse(cfg.hasErrors());

        // 10th case: request payload. All request headers
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());
        assertFalse(cfg.needsPreflightExpansionOf(RequestPayload));
        assertTrue(cfg.hasErrors());

        // 11 case: request routing
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.demandsPreflightHandling());
        assertTrue(cfg.needsPreflightExpansionOf(Routing));
        assertFalse(cfg.hasErrors());

        // 12 case: request routing
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());
        assertFalse(cfg.needsPreflightExpansionOf(RelayParams));
        assertTrue(cfg.hasErrors());

        // 13 case: response headers
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());
        assertFalse(cfg.needsPreflightExpansionOf(AllRequestHeaders));
        assertTrue(cfg.hasErrors());

        // 14 case: response payload
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsPreflightHandling());
        assertFalse(cfg.needsPreflightExpansionOf(ResponsePayload));
        assertTrue(cfg.hasErrors());
    }

    @Test
    public void testSidecarTimeout() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/sidecarTimeout.yml");

        // First case: Not defined.
        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertEquals(3000L, cfg.getSidecarTimeout());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertEquals(450L, cfg.getSidecarTimeout());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertEquals(4000L, cfg.getSidecarTimeout());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertEquals(4200L, cfg.getSidecarTimeout());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertEquals(15000L, cfg.getSidecarTimeout());
    }

    @Test
    public void testStaticModification() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/staticModification.yml");

        // First case: Not defined.
        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertNull(cfg.getStaticModification());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertNotNull(cfg.getStaticModification());
        assertNotNull(cfg.getStaticModification().getModify());
        assertNotNull(cfg.getStaticModification().getModify().getChangeRoute());
        assertEquals("localhost-klm", cfg.getStaticModification().getModify().getChangeRoute().getHost());
    }

    @Test
    public void testLoadingScopeFilters() {
        Iterator<Object> obj = loadAllYamlDocuments(getClass(), "/yaml-local-config/scopeFilters.yml");

        // First case: Not defined.
        SidecarConfiguration cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsScopeFiltering());

        // Second case: Also, not defined completely.
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsScopeFiltering());

        // Third case: Completely wrongly defiend entries are skipped
        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertFalse(cfg.demandsScopeFiltering());

        cfg = YamlConfigurationBuilder.getSidecarConfiguration(SidecarInputPoint.PreProcessor,
                YamlConfigurationBuilder.nextYamlDocument(obj));
        assertTrue(cfg.demandsScopeFiltering());

        assertTrue(cfg.demandsScopeFilteringOn("requestHeader", "content-type", "ct", ".*json.*", true));
        assertTrue(cfg.demandsScopeFilteringOn("requestHeader", "doc-type", "ct1", ".*yaml.*", true));
        assertTrue(cfg.demandsScopeFilteringOn("requestHeader", "user-agent", "ct2", ".*java.*", false));
        assertTrue(cfg.demandsScopeFilteringOn("responseHeader", "user-agent", "ct3", ".*cpp.*", false));
    }

}
