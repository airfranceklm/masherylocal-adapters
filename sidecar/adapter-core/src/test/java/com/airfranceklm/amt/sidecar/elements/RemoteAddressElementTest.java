package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildClientRequest;


public class RemoteAddressElementTest extends ElementTestCase<String> {

    private static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc) -> {
            buildClientRequest(tc, cfg -> {
                cfg.remoteAddr("1.2.3.4");
            });
        });
    }

    @Test
    public void testBasicExtraction() throws DataElementException {
        ElementDemand cfg = new ElementDemand("remoteAddress");
        assertAcceptedForPreAndPost(dsl, cfg, "1.2.3.4", SidecarInput::getRemoteAddress);
    }

    @Test
    public void testExtractionOfNull() throws DataElementException {
        BasicDSL caseDSL = dsl.duplicate();
        caseDSL.expr((tc) -> buildClientRequest(tc, cfg -> cfg.remoteAddr(null)));

        ElementDemand cfg = new ElementDemand("remoteAddress");
        assertNullForPreAndPost(caseDSL, cfg, SidecarInput::getRemoteAddress);
    }


}
