package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildPackageKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NormalizersTest extends ElementTestCase<String> {

    private static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc) -> buildPackageKey(tc, (cfg) -> cfg.eav("pkA", "Non-Normalized Value")));
    }

    @Test
    public void testPackageKeyEAVExtraction() throws DataElementException {
        ElementDemand cfg = new ElementDemand("packageKeyEAV", "pkA")
                .normalized(StandardNormalizers.Lowercase);

        PreProcessEvent preEvent = createPreProcessMock(dsl.build());
        replayAll();

        SidecarInvocationData sid = extractDataElement(preEvent, cfg, DataElementRelevance.Invoke);
        assertEquals("non-normalized value", sid.getInput().getPackageKeyEAVs().get("pkA"));
    }

    @Test
    public void testPackageKeyEAVExtractionWithSha256() throws DataElementException {
        ElementDemand cfg = new ElementDemand("packageKeyEAV", "pkA")
                .normalized(StandardNormalizers.Sha256);

        PreProcessEvent preEvent = createPreProcessMock(dsl.build());
        replayAll();

        SidecarInvocationData sid = extractDataElement(preEvent, cfg, DataElementRelevance.Invoke);
        assertNotNull(sid);
        assertNotNull(sid.getInput());
        assertNotNull(sid.getInput().getPackageKeyEAVs());
        assertEquals("a6fd36d3bf01cace98fb77032a7abac03c15d03293f3c86008af163b34fc4f6b", sid.getInput().getPackageKeyEAVs().get("pkA"));
    }

    @Test
    public void testPackageKeyEAVExtractionLowercasedWithSha256() throws DataElementException {
        ElementDemand cfg = new ElementDemand("packageKeyEAV", "pkA")
                .normalized(StandardNormalizers.Lowercase)
                .normalized(StandardNormalizers.Sha256);

        PreProcessEvent preEvent = createPreProcessMock(dsl.build());
        replayAll();

        SidecarInvocationData sid = extractDataElement(preEvent, cfg, DataElementRelevance.Invoke);
        assertNotNull(sid);
        assertNotNull(sid.getInput());
        assertNotNull(sid.getInput().getPackageKeyEAVs());
        assertEquals("ae58195275f4f4d067b08eb5b55cde7522fc6634b755785f555a857151c82198", sid.getInput().getPackageKeyEAVs().get("pkA"));
    }
}
