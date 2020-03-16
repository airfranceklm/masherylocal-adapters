package com.airfranceklm.amt.testsupport;

import org.junit.Test;

import static org.junit.Assert.*;

public class MasheryProcessTestSuiteTest {
    @Test
    public void loadBasicSuite() {
        MasheryProcessorTestSuite<MasheryProcessorTestCase> suite = new MasheryProcessorTestSuite<>();
        suite.loadCasesFrom(getClass().getResourceAsStream("/testsuite/just-endpoint.yaml"));

        final MasheryEndpointModel mdl = suite.getEndpointModel();
        assertNotNull(mdl);
        assertEquals("YAML Endpoint", mdl.getEndpointName());

        assertEquals("yamlServiceId", mdl.getServiceId());
        assertEquals("yamlEndpointId", mdl.getEndpointId());

        assertEquals("https://api-unittest.airfranceklm.com/a/yaml/api", mdl.getEndpointURI());
        assertEquals("https://api.origin.unittest.klm.com/infra/yaml/api", mdl.getOriginURI());
    }

    @Test
    public void loadSingleCase() {
        MasheryProcessorTestSuite<MasheryProcessorTestCase> suite = new MasheryProcessorTestSuite<>();
        suite.loadCasesFrom(getClass().getResourceAsStream("/testsuite/single-case.yaml"));

        MasheryProcessorTestCase tc = suite.getCase("Extended scenario");
        assertNotNull(tc);
        assertEquals(TestCasePoint.PostProcessor, tc.getPoint());
        assertNotNull(tc.getClientRequest());

        APIClientRequestModel arcm = tc.getClientRequest();
        assertEquals("/", arcm.getResource());
        assertEquals("GET", arcm.getHttpVerb());
    }
}
