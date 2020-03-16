package com.airfranceklm.amt.testsupport;

import com.mashery.trafficmanager.debug.DebugContext;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import static com.airfranceklm.amt.testsupport.MasheryDebugContextInteractionModel.debugContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class MasheryDebugContextInteractionModelTest extends EasyMockSupport {

    @Test
    public void createBasicTest() {
        MasheryDebugContextInteractionModel mdl = debugContext()
                .entry("A", "B")
                .build();

        DebugContext debugCtx = mdl.mock(this);
        replayAll();

        assertEquals("B", debugCtx.getEntry("A"));
        assertNull(debugCtx.getEntry("missing"));

        debugCtx.logEntry("newEntry", "newValue");
        assertEquals("newValue", debugCtx.getEntry("newEntry"));

        verifyAll();
    }

    @Test
    public void createStrictMock() {
        MasheryDebugContextInteractionModel mdl = debugContext()
                .strict(true)
                .acceptAnyValues(true)
                .build();

        assertFalse(mdl.isLenient());

        DebugContext debugCtx = mdl.mock(this);
        replayAll();

        debugCtx.getEntry("A");
        debugCtx.logEntry("newEntry", "newValue");
        debugCtx.clearEntries();

        verifyAll();
    }

    @Test
    public void createMockWithExpectations() {
        MasheryDebugContextInteractionModel mdl = debugContext()
                .entry("A", "B")
                .entry("D", "E")
                .expectGet("A")
                .expectGet("B")
                .expectPut("C", "D")
                .expectRemove("D")
                .build();

        DebugContext debugCtx = mdl.mock(this);
        replayAll();

        assertEquals("B", debugCtx.getEntry("A"));
        assertNull(debugCtx.getEntry("B"));
        debugCtx.logEntry("C", "D");
        assertEquals("E", debugCtx.removeEntry("D"));

        verifyAll();
    }

    @Test
    public void testDeepClone() {
        MasheryDebugContextInteractionModel mdl = debugContext()
                .entry("A", "B")
                .entry("C", "D")
                .expectGet("E")
                .expectGet("F")
                .expectPut("G", "H")
                .expectPut("I", "J")
                .expectRemove("K")
                .expectRemove("L")
                .build();

        MasheryDebugContextInteractionModel n1= new MasheryDebugContextInteractionModel();
        assertThat(n1, is(not(mdl)));

        n1.deepCopy(mdl);
        assertEquals(n1, mdl);

        // Check that the copy was deep. We'll check only two collections,
        // since all collections are copied in exactly the same fashion.
        n1.getEntries().put("A", "b");
        assertThat(n1, is(not(mdl)));

        n1.getEntries().put("A", "B");
        assertEquals(n1, mdl);

        n1.getExpectGets().add("Z");
        assertThat(n1, is(not(mdl)));

        n1.getExpectGets().remove("Z");
        assertEquals(n1, mdl);
    }
}
