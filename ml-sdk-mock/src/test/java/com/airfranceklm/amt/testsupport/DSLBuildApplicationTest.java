package com.airfranceklm.amt.testsupport;

import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import org.junit.Test;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildApplication;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildClientRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DSLBuildApplicationTest extends MasheryProcessorTestSupport<MasheryProcessorTestCase> {

    @Test
    public void testCreateApplicationOnly() {
        DSL<MasheryProcessorTestCase> dsl = BasicDSL.make().expr((tc) -> {
            buildApplication(tc, (cfg) -> cfg.name("uni-test-app").eav("A", "B"));
        });

        PreProcessEvent ppe = createPreProcessMock(dsl.build());
        replayAll();

        assertNotNull(ppe);
        assertNotNull(ppe.getKey());
        assertNotNull(ppe.getKey().getApplication());
        assertEquals("uni-test-app", ppe.getKey().getApplication().getName());
        assertEquals("B", ppe.getKey().getApplication().getExtendedAttributes().getValue("A"));

        verifyAll();
    }
}
