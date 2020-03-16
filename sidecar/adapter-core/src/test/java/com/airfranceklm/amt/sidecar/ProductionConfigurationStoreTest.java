package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.elements.StandardElementsFactory;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;
import com.airfranceklm.amt.sidecar.stack.InMemoryStack;
import com.airfranceklm.amt.sidecar.stack.SidecarStacks;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.easymock.EasyMock.anyObject;

public class ProductionConfigurationStoreTest extends SidecarMockSupport {

    private SidecarProcessor processor;

    @Before
    public void init() {
        processor = new SidecarProcessor();
        processor.setSupportedElements(new StandardElementsFactory());

        SidecarStacks stacks = new SidecarStacks();
        stacks.addStack(InMemoryStack.STACK_NAME, new InMemoryStack());

        processor.setSidecarStacks(stacks);
        processor.setConfigStore(new ProductionConfigurationStore());
        processor.setLocalConfigurationProvider(new LocalDirectoryConfigurationProvider(new File("/the/config/root")));

        processor.setup();
    }

    @Test
    public void testBasicLoadingFromLocal() throws IOException {

        InputStream is = getClass().getResourceAsStream("/yaml-local-config.yaml");
        processor.getLocalConfigurationProvider().loadStream("sample-path", is);

        final ProductionConfigurationStore configStore = (ProductionConfigurationStore)processor.getConfigStore();

        TimeCacheable<PreProcessEvent, PreProcessorSidecarRuntime> preRunTime = configStore.getStoredPreProcessor("anEndpointId");
        assertNotNull(preRunTime);

        assertNotNull(preRunTime.get(null));
        final JsonSidecarPreProcessorOutput sMod = preRunTime.get(null).getStaticModification();

        assertNotNull(sMod);
        assertNotNull(sMod.getModify().getChangeRoute());
        assertEquals("docker.klm.com", sMod.getModify().getChangeRoute().getHost());

    }

    /*
    @Test
    public void testEqualityOfTheKey() {
        SidecarTestDSL dsl = SidecarTestDSL
                .make();

        SidecarRequestCase src = dsl.configureEndpointData((c) -> {
                    c.identifyAs("serviceId", "endpointId", "endpointName");
                }).build();

        PreProcessEvent ppe = createPreProcessorMock(src);
        // For this test, we don't need actual implementation.
        SidecarProcessor proc = createMock(SidecarProcessor.class);
        SidecarStack stack = createMock(SidecarStack.class);
        SidecarStackConfiguration cfgMock = createMock(SidecarStackConfiguration.class);

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
     */
}
