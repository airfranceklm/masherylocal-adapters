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

public class TokenScopeElementTest  extends MasheryProcessorTestSupport<MasheryProcessorTestCase> {
    private static BasicDSL dsl;
    private static BasicDSL emptyDsl;

    @BeforeClass
    public static void initDSL() {
        emptyDsl = BasicDSL.make();
        dsl = emptyDsl.duplicate();

        dsl.expr((tc) -> {
            buildAuthorizationContext(tc, (c) -> c.scope("p1 p2 p3"));
        });
    }

    @Test
    public void testBasicExtraction() throws DataElementException {
        PreProcessEvent ppe = createPreProcessMock(dsl.build());
        replayAll();

        ElementDemand cfg = new ElementDemand("tokenScope");
        DataElement<? super PreProcessEvent, ?> elem = StandardElementsFactory.createForPreProcessor(cfg);
        assertNotNull(elem);

        SidecarInvocationData sid = new SidecarInvocationData();
        DataElementRelevance der = elem.extract(ppe, sid);

        assertEquals(Invoke, der);
        assertNotNull(sid.getInput().getToken());
        assertEquals("p1 p2 p3", sid.getInput().getToken().getScope());
    }

    @Test
    public void testExtractionFromNullContext() throws DataElementException {
        PreProcessEvent ppe = createPreProcessMock(emptyDsl.build());
        replayAll();

        ElementDemand cfg = new ElementDemand("tokenScope");
        DataElement<? super PreProcessEvent, ?> elem = StandardElementsFactory.createForPreProcessor(cfg);
        assertNotNull(elem);

        SidecarInvocationData sid = new SidecarInvocationData();
        DataElementRelevance der = elem.extract(ppe, sid);

        assertEquals(Invoke, der);
        assertNull(sid.getInput().getToken());
    }
}
