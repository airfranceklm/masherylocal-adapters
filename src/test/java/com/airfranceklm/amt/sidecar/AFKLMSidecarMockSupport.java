package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.stack.AFKLMSidecarStack;
import com.airfranceklm.amt.testsupport.RequestCaseYAMLReader;
import com.airfranceklm.amt.testsupport.RequestMockSupport;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Mock support that add processing of lambda inputs and outputs.
 */
public class AFKLMSidecarMockSupport extends RequestMockSupport<SidecarRequestCase> {

    protected AFKLMSidecarProcessor createSidecarProcessor(RequestMockSupport<SidecarRequestCase>.TestContext tc)  {

        SidecarRequestCase rc = tc.getRequestCase();

        // Transport will be called
        AFKLMSidecarStack.AFKLMSidecarStackConfiguration transportCfg = createMock(AFKLMSidecarStack.AFKLMSidecarStackConfiguration.class);
        expect(transportCfg.isValid()).andReturn(true).anyTimes();

        AFKLMSidecarStack mockTransport = createMock(AFKLMSidecarStack.class);
        expect(mockTransport.configureFrom(anyObject(SidecarConfiguration.class))).andReturn(transportCfg).anyTimes();

        try {
            if (rc.sidecarInput != null) {
                if (rc.sidecarOutput != null) {
                    expect(mockTransport.invoke(anyObject(), eq(rc.sidecarInput))).andReturn(rc.sidecarOutput).once();
                } else if (rc.sidecarException != null){
                    expect(mockTransport.invoke(anyObject(), eq(rc.sidecarInput))).andThrow(new IOException(rc.sidecarException)).once();
                } else {
                    // When output hasn't been specifically configured, or when exception wasn't
                    // specified, then an empty do-nothing object will be returned.
                    expect(mockTransport.invoke(anyObject(), eq(rc.sidecarInput))).andReturn(new SidecarOutputImpl()).once();
                }
            }

            if (rc.preflightInput != null) {
                if (rc.preflightOutput != null) {
                    expect(mockTransport.invoke(anyObject(), eq(rc.preflightInput)))
                            .andReturn(rc.preflightOutput)
                            .once();
                } else if (rc.preflightException != null) {
                    expect(mockTransport.invoke(anyObject(), eq(rc.preflightInput)))
                            .andThrow(new IOException(rc.preflightException))
                            .once();
                } else {
                    expect(mockTransport.invoke(anyObject(), eq(rc.preflightInput)))
                            .andReturn(new SidecarOutputImpl())
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
