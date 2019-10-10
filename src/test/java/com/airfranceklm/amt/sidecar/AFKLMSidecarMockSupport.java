package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.impl.model.SidecarPostProcessorOutputImpl;
import com.airfranceklm.amt.sidecar.impl.model.SidecarPreProcessorOutputImpl;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.model.SidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import com.airfranceklm.amt.sidecar.stack.AFKLMSidecarStack;
import com.airfranceklm.amt.testsupport.RequestMockSupport;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import org.easymock.IArgumentMatcher;

import java.io.IOException;
import java.util.Objects;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.fail;

/**
 * Mock support that add processing of lambda inputs and outputs.
 */
public class AFKLMSidecarMockSupport extends RequestMockSupport<SidecarRequestCase> {

    static SidecarInvocationData containingInput(SidecarInput input) {
        reportMatcher(new SidecarInvocationDataMatcher(input));
        return null;
    }

    static class SidecarInvocationDataMatcher implements IArgumentMatcher {
        SidecarInput exp;

        SidecarInvocationDataMatcher(SidecarInput exp) {
            this.exp = exp;
        }

        @Override
        public boolean matches(Object o) {
            if (exp == null && o == null) return true;

            if (o instanceof SidecarInvocationData) {
                SidecarInvocationData sid = (SidecarInvocationData)o;
                final boolean equals = Objects.equals(exp, sid.getInput());
                if (!equals) {
                    System.out.println(exp.explainDifferenceFrom(sid.getInput()));
                }
                return equals;
            }
            return false;
        }

        @Override
        public void appendTo(StringBuffer stringBuffer) {
            stringBuffer.append("Sidecar input containing:\n  >").append(String.valueOf(exp))
                    .append("\n  > ");
        }
    }

    protected AFKLMSidecarProcessor createSidecarProcessor(RequestMockSupport<SidecarRequestCase>.TestContext tc) {

        SidecarRequestCase rc = tc.getRequestCase();

        // Transport will be called
        AFKLMSidecarStack.AFKLMSidecarStackConfiguration transportCfg = createMock(AFKLMSidecarStack.AFKLMSidecarStackConfiguration.class);
        expect(transportCfg.isValid()).andReturn(true).anyTimes();

        AFKLMSidecarStack mockTransport = createMock(AFKLMSidecarStack.class);
        expect(mockTransport.configureFrom(anyObject(SidecarConfiguration.class))).andReturn(transportCfg).anyTimes();

        try {
            if (rc.sidecarInput != null) {
                if (rc.isPreProcessorCase()) {
                    if (rc.sidecarException != null) {
                        expect(mockTransport
                                .invokeAtPreProcessor(anyObject(), containingInput(rc.sidecarInput), anyObject()))
                                .andThrow(new IOException(rc.sidecarException))
                                .once();
                    } else {
                        SidecarPreProcessorOutput output = rc.preProcessorOutput != null ? rc.preProcessorOutput :
                                new SidecarPreProcessorOutputImpl();

                        expect(mockTransport
                                .invokeAtPreProcessor(anyObject(), containingInput(rc.sidecarInput), anyObject()))
                                .andReturn(output)
                                .once();
                    }
                } else {
                    if (rc.sidecarException != null) {
                        expect(mockTransport
                                .invokeAtPostProcessor(anyObject(), containingInput(rc.sidecarInput), anyObject()))
                                .andThrow(new IOException(rc.sidecarException))
                                .once();
                    } else {
                        SidecarPostProcessorOutput output = rc.postProcessorOutput != null ? rc.postProcessorOutput :
                                new SidecarPostProcessorOutputImpl();

                        expect(mockTransport
                                .invokeAtPostProcessor(anyObject(), containingInput(rc.sidecarInput), anyObject()))
                                .andReturn(output)
                                .once();
                    }
                }
            }

            if (rc.preflightInput != null) {
                if (rc.preflightOutput != null) {
                    expect(mockTransport
                            .invokeAtPreProcessor(anyObject(), containingInput(rc.preflightInput), anyObject()))
                            .andReturn(rc.preflightOutput)
                            .once();
                } else if (rc.preflightException != null) {
                    expect(mockTransport
                            .invokeAtPreProcessor(anyObject(), containingInput(rc.preflightInput), anyObject()))
                            .andThrow(new IOException(rc.preflightException))
                            .once();
                } else {
                    expect(mockTransport
                            .invokeAtPreProcessor(anyObject(), containingInput(rc.preflightInput), anyObject()))
                            .andReturn(new SidecarPreProcessorOutputImpl())
                            .once();
                }
            }
        } catch (IOException ex) {
            fail("An I/O exception was thrown during the setup of the mocks; this should never happen.");
        }


        // Otherwise the transport shouldn't be called at all.
        AFKLMSidecarProcessor retVal = partialMockBuilder(AFKLMSidecarProcessor.class)
                .addMockedMethod("getStackFor")
                .createMock();
        expect(retVal.getStackFor(anyObject())).andReturn(mockTransport).anyTimes();

        retVal.useStore(new StatelessSidecarConfigurationStore());
        retVal.initIdempotentDependencies();

        return retVal;
    }

    /**
     * Verfies the handling of the event in the pre-processor.
     *
     * @param rc request case
     */
    protected void verifyPreProcessorRequestCase(SidecarRequestCase rc) {
        TestContext tc = createTestContextFrom(rc);

        PreProcessEvent ppe = createPreProcessorMock(tc);
        AFKLMSidecarProcessor processor = createSidecarProcessor(tc);

        replayAll();

        processor.handleEvent(ppe);
        verifyAll();
    }

    /**
     * Verfies the handling of the event in the pre-processor.
     *
     * @param rc request case
     */
    protected void verifyPostProcessorRequestCase(SidecarRequestCase rc) {
        TestContext tc = createTestContextFrom(rc);

        PostProcessEvent ppe = createPostProcessorMock(tc);
        AFKLMSidecarProcessor processor = createSidecarProcessor(tc);

        replayAll();

        processor.handleEvent(ppe);
        verifyAll();
    }
}
