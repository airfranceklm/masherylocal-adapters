package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.testsupport.BasicDSL;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildPackageKey;
import static junit.framework.Assert.assertEquals;

public class PackageKeyElementTest extends ElementTestCase<String> {

    private static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc) -> buildPackageKey(tc, (cfg) -> cfg.packageKey("df53kf")));
    }

    @Test
    public void testAllHeadersRequestAtPre() throws DataElementException {
        ElementDemand cfg = new ElementDemand("packageKey");
        assertAcceptedForPreAndPost(dsl, cfg, "df53kf", SidecarInput::getPackageKey);
    }


}
