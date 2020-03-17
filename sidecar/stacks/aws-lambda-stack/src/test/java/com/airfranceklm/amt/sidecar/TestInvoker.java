package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import com.airfranceklm.amt.sidecar.model.SidecarSynchronicity;
import com.airfranceklm.amt.sidecar.stack.AWSLambdaConfiguration;
import com.airfranceklm.amt.sidecar.stack.AWSLambdaStack;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Shows the bare minimum to invoke a lambda function from the console.
 */
public class TestInvoker extends EasyMockSupport {

    private final String key = "TODO";
    private final String secret = "TODO";
    private final String functionName = "TODO";
    private final String region = "eu-west-1";

    AWSLambdaStack stackUnderTest;
    ProcessorServices processorServices;

    @Before
    public void setupStack() {
        processorServices = new JsonProcessorServices();

        stackUnderTest = new AWSLambdaStack();
        stackUnderTest.useProcessorServices(processorServices);
    }

    @After
    public void tearDown() {
        if (stackUnderTest != null) {
            stackUnderTest.shutdown();
        }
    }

    @Test @Ignore("Ignored for automatic run")
    public void testSampleEventInvocation() throws IOException {

        Map<String,String> map = new HashMap<>();
        map.put(AWSLambdaConfiguration.CFG_FUNCTION_ARN, functionName);
        map.put(AWSLambdaConfiguration.CFG_AWS_KEY, key);
        map.put(AWSLambdaConfiguration.CFG_AWS_SECRET, secret);
        map.put(AWSLambdaConfiguration.CFG_AWS_REGION, region);

        AWSLambdaConfiguration cfg = new AWSLambdaConfiguration(map, 3000L);

        SidecarInput si = new SidecarInput();
        si.setSynchronicity(SidecarSynchronicity.RequestResponse);
        si.setPoint(SidecarInputPoint.PreProcessor);
        si.setPackageKey("unit test");

        SidecarInvocationData sid = new SidecarInvocationData(si);

        SidecarPreProcessorOutput preOut = stackUnderTest.invoke(cfg, sid, SidecarPreProcessorOutput.class);
        System.out.println(JsonHelper.toPrettyJSON(preOut));
    }

    @Test @Ignore("Ignored for automatic run")
    public void testSampleEventInvocationCycle() throws IOException {

        Map<String,String> map = new HashMap<>();
        map.put(AWSLambdaConfiguration.CFG_FUNCTION_ARN, functionName);
        map.put(AWSLambdaConfiguration.CFG_AWS_KEY, key);
        map.put(AWSLambdaConfiguration.CFG_AWS_SECRET, secret);
        map.put(AWSLambdaConfiguration.CFG_AWS_REGION, region);

        AWSLambdaConfiguration cfg = new AWSLambdaConfiguration(map, 3000L);

        SidecarInput si = new SidecarInput();
        si.setSynchronicity(SidecarSynchronicity.RequestResponse);
        si.setPoint(SidecarInputPoint.PreProcessor);
        si.setPackageKey("unit test");

        SidecarInvocationData sid = new SidecarInvocationData(si);

        long start = System.currentTimeMillis();
        final int numTries = 100;

        for (int i = 0; i< numTries; i++) {
            stackUnderTest.invoke(cfg, sid, SidecarPreProcessorOutput.class);
        }

        System.out.println(String.format("Average per 100: %d msecs", (System.currentTimeMillis() - start)/ numTries));

    }
}
