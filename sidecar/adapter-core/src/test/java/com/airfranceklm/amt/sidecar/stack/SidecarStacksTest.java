package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.model.PreProcessorSidecarConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class SidecarStacksTest {

    static SidecarStacks stacks;

    @BeforeClass
    public static void initStacks() {
        stacks = new SidecarStacks();
    }

    @Test
    public void testCallingNullConfigurations() {
        assertNull(stacks.getStackFor(null));

        PreProcessorSidecarConfiguration cfg = new PreProcessorSidecarConfiguration();
        assertNull(stacks.getStackFor(cfg));
    }

}
