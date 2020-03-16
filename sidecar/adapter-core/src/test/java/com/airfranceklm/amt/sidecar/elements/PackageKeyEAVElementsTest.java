package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildPackageKey;
import static junit.framework.Assert.assertNull;

public class PackageKeyEAVElementsTest extends ElementTestCase<String> {

    private static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc) -> buildPackageKey(tc, (cfg) -> cfg.eav("pkA", "pkB")));
    }

    @Test
    public void testPackgeKeyEAVExtraction() throws DataElementException {
        ElementDemand cfg = new ElementDemand("packageKeyEAV", "pkA");
        assertAcceptedForPreAndPost(dsl, cfg, "pkB", (input) -> input.getPackageKeyEAVs().get("pkA"));
    }

    @Test
    public void testPackgeKeyEAVEAVExtractionOfMissing() throws DataElementException {
        ElementDemand cfg = new ElementDemand("packageKeyEAV", "pkB");
        SidecarInvocationData sid = extractFromPreProcessorEvent(dsl, cfg, DataElementRelevance.Invoke);
        assertNull(sid.getInput().getPackageKeyEAVs());
    }
}
