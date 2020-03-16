package com.airfranceklm.amt.sidecar.localConfig;

import com.airfranceklm.amt.sidecar.ConfigurationStoreHelper;
import com.airfranceklm.amt.sidecar.PreProcessorSidecarRuntime;
import com.airfranceklm.amt.sidecar.SidecarProcessor;
import com.airfranceklm.amt.sidecar.config.afklyaml.YAMLEndpointConfiguration;
import com.airfranceklm.amt.sidecar.elements.StandardElementsFactory;
import com.airfranceklm.amt.sidecar.stack.EchoStack;
import com.airfranceklm.amt.sidecar.stack.SidecarStacks;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocalConfigurationTest {

    private static SidecarProcessor proc;
    private static StandardElementsFactory elementsFactory;

    @BeforeClass
    public static void init() {
        proc = new SidecarProcessor();
        SidecarStacks stacks = new SidecarStacks();
        stacks.addStack(EchoStack.STACK_NAME, new EchoStack());

        proc.setSidecarStacks(stacks);

        elementsFactory = new StandardElementsFactory();
    }

    @Test
    public void testLoadingSimpleConfiguration() {
        YAMLEndpointConfiguration cfg = new YAMLEndpointConfiguration();
        cfg.setServiceId("3qa89rdc7ca2zc98cbqyxwfw");
        cfg.setEndpointId("3sxfx7w3zwqjb64h87shrsbj");

        cfg.preProcess((preProc) -> {
            preProc.sidecar((sc) -> {
                sc.stack("echo");
                sc.param("tenantSecret", "superSecret");

                sc.demandElement("packageKey")
                        .demandElement("requestHeader", "content-type");
            });
        });

        PreProcessorSidecarRuntime rt = ConfigurationStoreHelper.buildPreProcessorSidecarRuntime(
                proc, elementsFactory, null,
                cfg.getPreProcessor().getSidecar().retrieve(),
                null
        );

        assertFalse(rt.demandsPreflightHandling());
        assertTrue(rt.demandsSidecarHandling());

        assertFalse(rt.getPreProcessBuilder().getConfiguration().hasErrors());

    }
}
