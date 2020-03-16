package com.airfranceklm.amt.sidecar.model;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SidecarInputTest {
    @Test
    public void testPojoAndBuilderEquality() {
        SidecarInput si = new SidecarInput();
        SidecarInput s2 = SidecarInput.buildSidecarInput().build();

        assertTrue(si.equals(s2));
        assertTrue(s2.equals(si));
    }
}
