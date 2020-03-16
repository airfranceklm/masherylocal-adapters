package com.airfranceklm.amt.sidecar.builders;

import com.airfranceklm.amt.sidecar.model.PreProcessorSidecarConfiguration;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.DSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCase;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestSupport;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.sidecar.model.SidecarInputPoint.PreProcessor;
import static com.airfranceklm.amt.sidecar.model.SidecarSynchronicity.RequestResponse;
import static org.junit.Assert.*;

public class AbstractSidecarInputBuilderTest extends MasheryProcessorTestSupport<MasheryProcessorTestCase> {

    private static DSL<MasheryProcessorTestCase> caseDSL;

    private PreProcessEvent preProcessEvent;

    @BeforeClass
    public static void initDSL() {
        caseDSL = BasicDSL.make().identifyEndpoint();
    }

    @Before
    public void createMocks() {
        preProcessEvent = createPreProcessMock(caseDSL.build());
    }

    @Test
    public void testInitializeCommonWithNullSynchronicity() {
        AbstractSidecarInputBuilder<PreProcessEvent, PreProcessorSidecarConfiguration>
                target = partialMockBuilder(AbstractSidecarInputBuilder.class)
                .createMock();

        replayAll();

        SidecarInvocationData sid = new SidecarInvocationData();
        target.useConfiguration(new PreProcessorSidecarConfiguration());
        target.initializeCommon(sid, preProcessEvent, PreProcessor);

        assertNotNull(sid.getInput());
        assertNull(sid.getInput().getParams());
        assertEquals(RequestResponse, sid.getInput().getSynchronicity());
    }

}
