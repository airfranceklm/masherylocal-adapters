package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;
import com.airfranceklm.amt.sidecar.stack.SidecarStack;
import com.airfranceklm.amt.sidecar.stack.SidecarStackConfiguration;
import com.airfranceklm.amt.sidecar.stack.SidecarStacks;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestSupport;
import com.airfranceklm.amt.testsupport.TestCasePoint;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import lombok.NonNull;
import org.easymock.IArgumentMatcher;

import java.io.IOException;
import java.util.Objects;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Mock support that add processing of lambda inputs and outputs.
 */
public class SidecarMockSupport extends MasheryProcessorTestSupport<SidecarInvocationTestCase> {

    static SidecarProcessorDefaults unitTestDefaults = SidecarProcessorDefaults.unitTest();

    private static SidecarInvocationData containingInput(SidecarInput input) {
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
                SidecarInvocationData sid = (SidecarInvocationData) o;
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
            stringBuffer.append("Sidecar input containing:\n  >").append(exp)
                    .append("\n  > ");
        }
    }

    protected void autoVerify(@NonNull  SidecarInvocationTestSuite suite, @NonNull String caseName) {
        SidecarInvocationTestCase tc = suite.getCase(caseName);
        assertNotNull(tc);

        if (tc.isPreProcessorCase()) {
            verifyPreProcessorCase(tc);
        } else {
            verifyPostProcessorCase(tc);
        }
    }


    protected SidecarProcessor createSidecarProcessor(SidecarInvocationTestCase rc) {

        // Transport will be called
        SidecarStackConfiguration transportCfg = createMock(SidecarStackConfiguration.class);
        expect(transportCfg.isValid()).andReturn(true).anyTimes();

        SidecarStack mockTransport = createMock(SidecarStack.class);
        expect(mockTransport.configureFrom(anyObject(SidecarConfiguration.class))).andReturn(transportCfg).anyTimes();

        try {
            // If this is a pre-processor case, only the respective objects will be setup.
            if (rc.isPreProcessorCase()) {
                if (rc.getPreProcessorInteraction() != null) {

                    if (rc.getPreProcessorException() != null) {
                        expect(mockTransport
                                .invoke(anyObject(), containingInput(rc.getPreProcessorInput()), eq(SidecarPreProcessorOutput.class)))
                                .andThrow(new IOException(rc.getPreProcessorException()))
                                .once();
                    } else {
                        SidecarPreProcessorOutput output = rc.getPreProcessorOutput() != null ? rc.getPreProcessorOutput() :
                                new JsonSidecarPreProcessorOutput();

                        expect(mockTransport
                                .invoke(anyObject(), containingInput(rc.getPreProcessorInput()), eq(SidecarPreProcessorOutput.class)))
                                .andReturn(output)
                                .once();
                    }
                }

                if (rc.getPreflightInteraction() != null) {
                    if (rc.getPreflightOutput() != null) {
                        expect(mockTransport
                                .invoke(anyObject(), containingInput(rc.getPreflightInput()), eq(SidecarPreProcessorOutput.class)))
                                .andReturn(rc.getPreflightOutput())
                                .once();
                    } else if (rc.getPreflightException() != null) {
                        expect(mockTransport
                                .invoke(anyObject(), containingInput(rc.getPreflightInput()), eq(SidecarPreProcessorOutput.class)))
                                .andThrow(new IOException(rc.getPreflightException()))
                                .once();
                    } else {
                        expect(mockTransport
                                .invoke(anyObject(), containingInput(rc.getPreflightInput()), eq(SidecarPreProcessorOutput.class)))
                                .andReturn(new JsonSidecarPreProcessorOutput())
                                .once();
                    }
                }
            } else {
                if (rc.getPostProcessorInteraction() != null) {
                    if (rc.getPostProcessorException() != null) {
                        expect(mockTransport
                                .invoke(anyObject(), containingInput(rc.getPostProcessorInput()), eq(SidecarPostProcessorOutput.class)))
                                .andThrow(new IOException(rc.getPostProcessorException()))
                                .once();
                    } else {
                        SidecarPostProcessorOutput output = rc.getPostProcessorOutput() != null ? rc.getPostProcessorOutput() :
                                new JsonSidecarPostProcessorOutput();

                        expect(mockTransport
                                .invoke(anyObject(), containingInput(rc.getPostProcessorInput()), eq(SidecarPostProcessorOutput.class)))
                                .andReturn(output)
                                .once();
                    }
                }
            }

        } catch (IOException ex) {
            fail("An I/O exception was thrown during the setup of the mocks; this should never happen.");
        }


        // Otherwise the transport shouldn't be called at all.
        SidecarProcessor retVal = partialMockBuilder(SidecarProcessor.class)
                .addMockedMethod("getStackFor")
                .createMock();
        expect(retVal.getStackFor(anyObject())).andReturn(mockTransport).anyTimes();

        unitTestDefaults.apply(retVal);
        retVal.setup();

        return retVal;
    }

    /**
     * Verifies the pre-processor case executed against this stack, where only one stack will be available.
     *
     * @param rc        Request case instance
     * @param execStack execution stack.
     */
    protected void verifyPreProcessorCase(SidecarInvocationTestCase rc, SidecarStack execStack) {
        PreProcessEvent ppe = createPreProcessMock(rc);
        replayAll();

        SidecarProcessor processor = createProcessorWithStack(execStack);

        try {
            processor.handleEvent(ppe);
        } finally {
            processor.stop();
        }

        verifyAll();
    }

    /**
     * Verifies the post-processor case executed against this stack, where only one stack will be available.
     *
     * @param rc        Request case instance
     * @param execStack execution stack.
     */
    protected void verifyPostProcessorCase(SidecarInvocationTestCase rc, SidecarStack execStack) {
        PostProcessEvent ppe = createPostProcessMock(rc);
        replayAll();

        SidecarProcessor processor = createProcessorWithStack(execStack);

        try {
            processor.handleEvent(ppe);
        } finally {
            processor.stop();
        }

        verifyAll();
    }

    private SidecarProcessor createProcessorWithStack(SidecarStack execStack) {
        SidecarProcessor processor = unitTestDefaults.build();

        SidecarStacks stacks = new SidecarStacks();
        stacks.addStack(execStack);
        processor.setSidecarStacks(stacks);

        processor.setup();

        return processor;
    }

    /**
     * Verfies the handling of the event in the pre-processor.
     *
     * @param rc request case
     */
    protected void verifyPreProcessorCase(@NonNull SidecarInvocationTestCase rc) {

        PreProcessEvent ppe = createPreProcessMock(rc);
        SidecarProcessor processor = createSidecarProcessor(rc);
        replayAll();

        try {
            processor.handleEvent(ppe);
            verifyAll();
        } catch (AssertionError ex) {
            System.out.println("==== Pre-Processor Sidecar Case Failure ======");
            printFailureHandlingInstructions(rc);
            throw ex;
        }
    }

    private void printFailureHandlingInstructions(@NonNull SidecarInvocationTestCase rc) {
        rc.dump();
        System.out.println("-------------------------------------------------");
        System.out.println("Instructions:");
        System.out.println("0. Review the case type: are you running pre-processor dataset with post-processor event?");
        System.out.println("1. Review the setup (did you supply all objects?)");
        System.out.println("2. Review DSL sequencing");
        System.out.println("3. Review code under test");
        System.out.println("---------------------------------------------------");
    }

    /**
     * Verfies the handling of the event in the pre-processor.
     *
     * @param rc request case
     */
    protected void verifyPostProcessorCase(SidecarInvocationTestCase rc) {
        PostProcessEvent ppe = createPostProcessMock(rc);
        SidecarProcessor processor = createSidecarProcessor(rc);

        replayAll();

        try {
            processor.handleEvent(ppe);
            verifyAll();
        } catch (AssertionError ex) {
            System.out.println("==== Post-Processor Sidecar Case Failure ======");
            printFailureHandlingInstructions(rc);
            throw ex;
        }
    }
}
