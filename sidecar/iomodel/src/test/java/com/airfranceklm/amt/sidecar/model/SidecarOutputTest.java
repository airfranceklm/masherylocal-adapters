package com.airfranceklm.amt.sidecar.model;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import java.util.Date;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;

public class SidecarOutputTest extends EasyMockSupport {

    @Test
    public void testIsIdempotent() {
        final Date refDate = new Date();

        SidecarOutput<CallModificationCommand> s1 = mockInterface();
        expect(s1.getUnchangedUntil()).andReturn(refDate).anyTimes();
        expect(s1.getUnchangedFor()).andReturn(null).anyTimes();

        SidecarOutput<CallModificationCommand> s2 = mockInterface();
        expect(s2.getUnchangedUntil()).andReturn(null).anyTimes();
        expect(s2.getUnchangedFor()).andReturn(600).anyTimes();

        SidecarOutput<CallModificationCommand> s3 = mockInterface();
        expect(s3.getUnchangedUntil()).andReturn(null).anyTimes();
        expect(s3.getUnchangedFor()).andReturn(null).anyTimes();

        replayAll();

        // Let's check the idempotence settings.
        assertTrue(s1.idempotentAware());
        assertSame(refDate, s1.effectiveUnchangedUntil());

        assertTrue(s2.idempotentAware());
        Date refS2Date = s2.effectiveUnchangedUntil();
        assertNotNull(refS2Date);
        assertTrue(refS2Date.after(new Date()));

        assertFalse(s3.idempotentAware());
        assertNull(s3.effectiveUnchangedUntil());

        verifyAll();
    }

    private SidecarOutput<CallModificationCommand> mockInterface() {
        return partialMockBuilder(SidecarOutputImpl.class)
                .addMockedMethod("getUnchangedUntil")
                .addMockedMethod("getUnchangedFor")
                .createMock();
    }

    class SidecarOutputImpl implements SidecarOutput<CallModificationCommand> {
        @Override
        public Date getUnchangedUntil() {
            return null;
        }

        @Override
        public Integer getUnchangedFor() {
            return null;
        }

        @Override
        public TerminateCommand getTerminate() {
            return null;
        }

        @Override
        public CallModificationCommand getModify() {
            return null;
        }
    }

}
