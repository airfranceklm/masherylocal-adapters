package com.airfranceklm.amt.sidecar.model.impl;

import com.airfranceklm.amt.sidecar.model.json.JsonModificationCommandImpl;
import org.junit.Test;

import static com.airfranceklm.amt.sidecar.model.json.JsonPayloadCarrier.allocOrGetPassHeaders;
import static org.junit.Assert.assertTrue;

public class SidecarOutputTests {

    @Test
    public void testChecksForAddedContentType() {
        JsonModificationCommandImpl output = new JsonModificationCommandImpl();
        allocOrGetPassHeaders(output).put("Content-Type", "a");
        assertTrue(output.addsContentType());
    }
}
