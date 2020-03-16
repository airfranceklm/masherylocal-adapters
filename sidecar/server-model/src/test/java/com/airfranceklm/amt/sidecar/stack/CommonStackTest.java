package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.ProcessorServices;
import org.easymock.EasyMockSupport;
import org.easymock.TestSubject;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class CommonStackTest extends EasyMockSupport {
    @Test
    public void testCommonStack() {
        CommonStack cm = partialMockBuilder(CommonStack.class).createMock();
        ProcessorServices ps = createMock(ProcessorServices.class);

        replayAll();

        cm.useProcessorServices(ps);
        assertSame(ps, cm.getProcessorServices());
    }
}
