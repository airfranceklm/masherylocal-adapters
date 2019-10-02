package com.airfranceklm.amt.sidecar;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SidecarOutputTests {

    @Test
    public void testChecksForAddedContentType() {
        SidecarOutputImpl output = new SidecarOutputImpl();
        output.addHeader("Content-Type", "a");
        assertTrue(output.addsContentType());
    }
}
