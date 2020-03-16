package com.airfranceklm.amt.sidecar.stack.elements;

import com.airfranceklm.amt.sidecar.elements.NumericElements;
import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.model.ElementFilterDemand;
import org.junit.Test;

import static com.airfranceklm.amt.sidecar.elements.DataElementFilterIntent.MatchScopes;
import static com.airfranceklm.amt.sidecar.model.ElementDemand.elem;
import static org.junit.Assert.*;

public class NumericElementsTest {

    @Test
    public void testDemandCreation() {
        ElementDemand ed = elem(NumericElements.ResponseCode).filtered((f) -> f.algorithm("ff"));

        assertEquals(NumericElements.ResponseCode.getElementName(), ed.getName());
        assertNull(ed.getParameter());
        assertNotNull(ed.getFilters());
        assertEquals(1, ed.getFilters().size());

        ElementFilterDemand efd = ed.getFilters().get(0);
        assertEquals(MatchScopes, efd.getIntent());
        assertEquals("ff", efd.getAlgorithm());
    }
}
