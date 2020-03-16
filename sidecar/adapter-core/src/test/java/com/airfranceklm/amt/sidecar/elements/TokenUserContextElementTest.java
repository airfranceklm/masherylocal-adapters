package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCase;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestSupport;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.Invoke;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildAuthorizationContext;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TokenUserContextElementTest extends MasheryProcessorTestSupport<MasheryProcessorTestCase> {
    private static BasicDSL emptyDSL;
    private static BasicDSL dsl;

    @BeforeClass
    public static void initDSL() {
        emptyDSL = BasicDSL.make();
        dsl = emptyDSL.duplicate();

        dsl.expr((tc) -> buildAuthorizationContext(tc, (c) -> c.userContext("uc1 uc2 uc3")));
    }

    @Test
    public void testBasicExtraction() throws DataElementException {
        PreProcessEvent ppe = createPreProcessMock(dsl.build());
        replayAll();

        ElementDemand cfg = new ElementDemand("userContext");
        DataElement<? super PreProcessEvent, ?> elem = StandardElementsFactory.createForPreProcessor(cfg);
        assertNotNull(elem);

        SidecarInvocationData sid = new SidecarInvocationData();
        DataElementRelevance der = elem.extract(ppe, sid);

        assertEquals(Invoke, der);
        assertNotNull(sid.getInput().getToken());
        assertEquals("uc1 uc2 uc3", sid.getInput().getToken().getUserContext());
    }

    @Test
    public void testExtractionFromNullContext() throws DataElementException {
        PreProcessEvent ppe = createPreProcessMock(emptyDSL.build());
        replayAll();

        ElementDemand cfg = new ElementDemand("userContext");
        DataElement<? super PreProcessEvent, ?> elem = StandardElementsFactory.createForPreProcessor(cfg);
        assertNotNull(elem);

        SidecarInvocationData sid = new SidecarInvocationData();
        DataElementRelevance der = elem.extract(ppe, sid);

        assertEquals(Invoke, der);
        assertNull(sid.getInput().getToken());
    }
}
