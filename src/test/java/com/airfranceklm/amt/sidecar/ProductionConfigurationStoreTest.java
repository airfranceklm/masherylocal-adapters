package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.config.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.input.SidecarInputBuilder;
import com.airfranceklm.amt.sidecar.stack.AFKLMSidecarStack;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;

public class ProductionConfigurationStoreTest extends AFKLMSidecarMockSupport {
    @Test
    public void testEqualityOfTheKey() {
        SidecarTestDSL dsl = SidecarTestDSL
                .make();

        SidecarRequestCase src = dsl.configureEndpointData((c) -> {
                    c.identifyAs("serviceId", "endpointId", "endpointName");
                }).build();

        PreProcessEvent ppe = createPreProcessorMock(src);
        // For this test, we don't need actual implementation.
        AFKLMSidecarProcessor proc = createMock(AFKLMSidecarProcessor.class);
        AFKLMSidecarStack stack = createMock(AFKLMSidecarStack.class);
        AFKLMSidecarStack.AFKLMSidecarStackConfiguration cfgMock = createMock(AFKLMSidecarStack.AFKLMSidecarStackConfiguration.class);

        expect(cfgMock.isValid()).andReturn(true);
        expect(stack.configureFrom(anyObject())).andReturn(cfgMock);
        expect(proc.getStackFor(anyObject())).andReturn(stack);

        replayAll();

        ProductionConfigurationStore store = new ProductionConfigurationStore();
        store.bindTo(proc);
        SidecarConfiguration cfg = store.getConfiguration(ppe);
        assertNotNull(cfg);

        SidecarInputBuilder<PreProcessEvent> builder = store.getPreProcessorInputBuilder(cfg);
        assertNotNull(builder);
    }
}
