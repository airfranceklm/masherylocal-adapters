package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildApplication;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildEndpoint;
import static junit.framework.Assert.assertNull;

public class EAVElementsTest extends ElementTestCase<String> {

    private static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc) -> {
            buildApplication(tc, (cfg) -> cfg.name("uni-test-app").eav("A", "B"));
        });
    }

    @Test
    public void testEAVExtraction() throws DataElementException {
        ElementDemand cfg = new ElementDemand("eav", "A");
        assertAcceptedForPreAndPost(dsl, cfg, "B", (input) -> input.getEavs().get("A"));
    }

    @Test
    public void testEAVExtractionOfMissing() throws DataElementException {
        ElementDemand cfg = new ElementDemand("eav", "B");
        SidecarInvocationData sid = extractFromPreProcessorEvent(dsl, cfg, DataElementRelevance.Invoke);
        assertNull(sid.getInput().getEavs());
    }
}
