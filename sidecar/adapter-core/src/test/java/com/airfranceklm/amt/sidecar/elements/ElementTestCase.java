package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.model.*;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.DSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCase;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestSupport;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;

import java.util.function.Function;

import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.Invoke;
import static org.junit.Assert.*;

public class ElementTestCase<T> extends MasheryProcessorTestSupport<MasheryProcessorTestCase> {

    protected void assertNullForPreAndPost(DSL<MasheryProcessorTestCase> dsl, ElementDemand cfg, Function<SidecarInput, T> locator) throws DataElementException {
        final MasheryProcessorTestCase rCase = dsl.build();

        PreProcessEvent preEvent = createPreProcessMock(rCase);
        PostProcessEvent postEvent = createPostProcessMock(rCase);
        replayAll();

        validateNullAtPre(preEvent, cfg,  locator);
        validateNullAtPost(postEvent, cfg,  locator);
    }

    protected void validateNullAtPre(PreProcessEvent preEvent, ElementDemand cfg, Function<SidecarInput, T> locator) throws DataElementException {

        SidecarInvocationData sid = extractDataElement(preEvent, cfg, Invoke);
        assertNull("Check null in pre-processor", locator.apply(sid.getInput()));
    }

    protected SidecarInvocationData extractDataElement(PreProcessEvent preEvent, ElementDemand cfg, DataElementRelevance expRelevance) throws DataElementException {
        DataElement<? super PreProcessEvent, ?> elem = StandardElementsFactory.createForPreProcessor(cfg);
        assertNotNull(elem);

        SidecarInvocationData sid = new SidecarInvocationData();
        DataElementRelevance der = elem.extract(preEvent, sid);

        assertEquals(expRelevance, der);
        return sid;
    }

    protected void validateNullAtPost(PostProcessEvent postEvent, ElementDemand cfg, Function<SidecarInput, T> locator) throws DataElementException {
        SidecarInvocationData sid = extractDataElement(postEvent, cfg, Invoke);
        assertNull("Check for null in post-processor", locator.apply(sid.getInput()));
    }

    protected void assertAcceptedForPre(DSL<MasheryProcessorTestCase> dsl, ElementDemand cfg, T expectedValue, Function<SidecarInput, T> locator) throws DataElementException {
        final MasheryProcessorTestCase rCase = dsl.build();

        PreProcessEvent preEvent = createPreProcessMock(rCase);
        replayAll();
        doAssertAcceptedAtPre(preEvent, cfg, expectedValue, locator);
    }

    protected void assertAcceptedForPost(DSL<MasheryProcessorTestCase> dsl, ElementDemand cfg, T expectedValue, Function<SidecarInput, T> locator) throws DataElementException {
        final MasheryProcessorTestCase rCase = dsl.build();

        PostProcessEvent postEvent = createPostProcessMock(rCase);
        replayAll();
        doAssertAcceptedAtPost(postEvent, cfg, expectedValue, locator);
    }


    protected void assertAcceptedForPreAndPost(DSL<MasheryProcessorTestCase> dsl, ElementDemand cfg, T expectedValue, Function<SidecarInput, T> locator) throws DataElementException {
        final MasheryProcessorTestCase rCase = dsl.build();

        PreProcessEvent preEvent = createPreProcessMock(rCase);
        PostProcessEvent postEvent = createPostProcessMock(rCase);
        replayAll();

        doAssertAcceptedAtPre(preEvent, cfg, expectedValue, locator);
        doAssertAcceptedAtPost(postEvent, cfg, expectedValue, locator);
    }

    private void doAssertAcceptedAtPre(PreProcessEvent preEvent, ElementDemand cfg, T expectedValue, Function<SidecarInput, T> locator) throws DataElementException {
        SidecarInvocationData sid = extractDataElement(preEvent, cfg, Invoke);
        assertEquals("Check for availability in pre-processor", expectedValue, locator.apply(sid.getInput()));
    }

    private void doAssertAcceptedAtPost(PostProcessEvent postEvent, ElementDemand cfg, T expectedValue, Function<SidecarInput, T> locator) throws DataElementException {

        SidecarInvocationData sid = extractDataElement(postEvent, cfg, Invoke);
        assertEquals("Check for availability in post-processor", expectedValue, locator.apply(sid.getInput()));
    }

    protected SidecarInvocationData extractFromPreProcessorEvent(DSL<MasheryProcessorTestCase> dsl, ElementDemand cfg, DataElementRelevance expRelevance) throws DataElementException {
        PreProcessEvent preEvent = createPreProcessMock(dsl.build());
        replayAll();

        return extractDataElement(preEvent, cfg, expRelevance);
    }

    protected SidecarInvocationData extractFromPostProcessorEvent(DSL<MasheryProcessorTestCase> dsl, ElementDemand cfg, DataElementRelevance expRelevance) throws DataElementException {
        PostProcessEvent postEvent = createPostProcessMock(dsl.build());
        replayAll();

        return extractDataElement(postEvent, cfg, expRelevance);
    }

    private SidecarInvocationData extractDataElement(PostProcessEvent postEvent, ElementDemand cfg, DataElementRelevance expRelevance) throws DataElementException {
        DataElement<? super PostProcessEvent, ?> elem = StandardElementsFactory.createForPostProcessor(cfg);
        assertNotNull(elem);

        SidecarInvocationData sid = new SidecarInvocationData();
        DataElementRelevance der = elem.extract(postEvent, sid);

        assertEquals(expRelevance, der);
        return sid;
    }
}
