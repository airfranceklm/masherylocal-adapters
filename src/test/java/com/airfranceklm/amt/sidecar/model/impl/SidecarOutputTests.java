package com.airfranceklm.amt.sidecar.model.impl;

import com.airfranceklm.amt.sidecar.impl.model.CallModificationCommandImpl;
import com.airfranceklm.amt.sidecar.impl.model.SidecarPreProcessorOutputImpl;
import org.junit.Test;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertTrue;

public class SidecarOutputTests {

    @Test
    public void testChecksForAddedContentType() {
        CallModificationCommandImpl output = new CallModificationCommandImpl();
        output.getOrCreateAddHeaders().put("Content-Type", "a");
        assertTrue(output.addsContentType());
    }
}
