package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.ProcessorServices;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.JsonIO;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import com.airfranceklm.amt.sidecar.model.SidecarSynchronicity;
import com.airfranceklm.amt.sidecar.model.alcp.EncryptedMessage;
import com.airfranceklm.amt.sidecar.model.alcp.MasheryALCPSide;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertSame;

public class ALCPEnabledStackTest extends EasyMockSupport {

    SidecarPreProcessorOutput sppo;
    EncryptedMessage<Integer> em;
    JsonIO unm;
    SidecarInput si;
    SidecarInvocationData sid;
    ProcessorServices ps;
    SidecarStackConfiguration cfg;

    @Before
    public void setupMocks() {
        sppo = createMock(SidecarPreProcessorOutput.class);
        em = createMock(EncryptedMessage.class);
        unm = createMock(JsonIO.class);

        si = new SidecarInput();
        si.setSynchronicity(SidecarSynchronicity.RequestResponse);
        sid = new SidecarInvocationData(si);

        ps = createMock(ProcessorServices.class);
        cfg = createMock(SidecarStackConfiguration.class);
    }


    @Test
    public void testBidirectionalInvocation() throws IOException {

        MasheryALCPSide<Integer, Integer> alcpSide = createMock(MasheryALCPSide.class);
        expect(alcpSide.requiresRequestEncryption()).andReturn(true).anyTimes();
        expect(alcpSide.requiresResponseDecryption()).andReturn(true).anyTimes();
        expect(alcpSide.getSidecarResponseClass()).andReturn(Integer.class).once();
        expect(alcpSide.encrypt(same(si), anyObject(ProcessorServices.class))).andReturn(em).once();
        expect(alcpSide.decrypt(eq(2), anyObject(), eq(SidecarPreProcessorOutput.class))).andReturn(sppo).once();


        ALCPEnabledStack stack = partialMockBuilder(ALCPEnabledStack.class).createMock();
        expect(stack.doInvoke(eq(cfg)
                , anyObject(EncryptedMessage.class)
                , eq(Integer.class))).andReturn(2).once();

        stack.useProcessorServices(ps);
        sid.setApplicationLevelCallProtection(alcpSide);

        replayAll();

        SidecarPreProcessorOutput sppoOutput = stack.invoke(cfg, sid, SidecarPreProcessorOutput.class);
        assertSame(sppo, sppoOutput);

        verifyAll();
    }

    @Test
    public void testRequestOnlyEncryption() throws IOException {

        MasheryALCPSide<Integer, Integer> alcpSide = createMock(MasheryALCPSide.class);
        expect(alcpSide.requiresRequestEncryption()).andReturn(true).anyTimes();
        expect(alcpSide.requiresResponseDecryption()).andReturn(false).anyTimes();

        expect(alcpSide.encrypt(same(si)
                , anyObject(ProcessorServices.class)))
                .andReturn(em).once();


        ALCPEnabledStack stack = partialMockBuilder(ALCPEnabledStack.class).createMock();
        expect(stack.doInvoke(eq(cfg)
                , eq(em)
                , eq(SidecarPreProcessorOutput.class))).andReturn(sppo).once();

        stack.useProcessorServices(ps);
        sid.setApplicationLevelCallProtection(alcpSide);

        replayAll();

        SidecarPreProcessorOutput sppoOutput = stack.invoke(cfg, sid, SidecarPreProcessorOutput.class);
        assertSame(sppo, sppoOutput);

        verifyAll();
    }

    @Test
    public void testResponseOnlyDecryption() throws IOException {

        MasheryALCPSide<Integer, Integer> alcpSide = createMock(MasheryALCPSide.class);
        expect(alcpSide.requiresRequestEncryption()).andReturn(false).anyTimes();
        expect(alcpSide.requiresResponseDecryption()).andReturn(true).anyTimes();
        expect(alcpSide.getSidecarResponseClass()).andReturn(Integer.class).once();

        expect(alcpSide.decrypt(eq(2)
                , anyObject(ProcessorServices.class)
                , eq(SidecarPreProcessorOutput.class)))
                .andReturn(sppo).once();


        ALCPEnabledStack stack = partialMockBuilder(ALCPEnabledStack.class).createMock();
        expect(stack.doInvoke(eq(cfg)
                , eq(si)
                , eq(Integer.class))).andReturn(2).once();

        stack.useProcessorServices(ps);
        sid.setApplicationLevelCallProtection(alcpSide);

        replayAll();

        SidecarPreProcessorOutput sppoOutput = stack.invoke(cfg, sid, SidecarPreProcessorOutput.class);
        assertSame(sppo, sppoOutput);

        verifyAll();
    }

    @Test
    public void testDisabledProtection() throws IOException {

        MasheryALCPSide<Integer, Integer> alcpSide = createMock(MasheryALCPSide.class);
        expect(alcpSide.requiresRequestEncryption()).andReturn(false).anyTimes();
        expect(alcpSide.requiresResponseDecryption()).andReturn(false).anyTimes();

        ALCPEnabledStack stack = partialMockBuilder(ALCPEnabledStack.class).createMock();
        expect(stack.doInvoke(eq(cfg)
                , eq(si)
                , eq(SidecarPreProcessorOutput.class))).andReturn(sppo).once();

        stack.useProcessorServices(ps);
        sid.setApplicationLevelCallProtection(alcpSide);

        replayAll();

        SidecarPreProcessorOutput sppoOutput = stack.invoke(cfg, sid, SidecarPreProcessorOutput.class);
        assertSame(sppo, sppoOutput);

        verifyAll();
    }

    @Test
    public void testNoALCPAssigned() throws IOException {

        ALCPEnabledStack stack = partialMockBuilder(ALCPEnabledStack.class).createMock();
        expect(stack.doInvoke(eq(cfg)
                , eq(si)
                , eq(SidecarPreProcessorOutput.class))).andReturn(sppo).once();

        stack.useProcessorServices(ps);

        replayAll();

        SidecarPreProcessorOutput sppoOutput = stack.invoke(cfg, sid, SidecarPreProcessorOutput.class);
        assertSame(sppo, sppoOutput);

        verifyAll();
    }
}
