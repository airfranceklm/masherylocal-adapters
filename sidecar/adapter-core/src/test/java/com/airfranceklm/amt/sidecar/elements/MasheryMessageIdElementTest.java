package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.DSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCase;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.Invoke;
import static com.airfranceklm.amt.testsupport.BasicDSL.emptyDSL;
import static junit.framework.Assert.assertEquals;

public class MasheryMessageIdElementTest extends ElementTestCase<String> {

    private static DSL<MasheryProcessorTestCase> dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make().emptyClientRequest();
    }

    @Test
    public void testAllHeadersRequestAtPre() throws DataElementException {
        ElementDemand cfg = new ElementDemand("messageId");
        SidecarInvocationData sid = extractFromPreProcessorEvent(dsl, cfg, Invoke);

        assertEquals("unit-test-call-uuid", sid.getInput().getMasheryMessageId());
    }

    @Test
    public void testAllHeadersRequestAtPost() throws DataElementException {
        ElementDemand cfg = new ElementDemand("messageId");
        SidecarInvocationData sid = extractFromPostProcessorEvent(emptyDSL(), cfg, Invoke);

        assertEquals("unit-test-call-uuid", sid.getInput().getMasheryMessageId());
    }


}
