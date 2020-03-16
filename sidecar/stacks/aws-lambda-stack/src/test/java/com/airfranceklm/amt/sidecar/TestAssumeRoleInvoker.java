package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.identity.BasicCredentialsDescriptor;
import com.airfranceklm.amt.sidecar.identity.KeyIdentifier;
import com.airfranceklm.amt.sidecar.identity.PartyKeyDescriptor;
import com.airfranceklm.amt.sidecar.identity.ProcessorKeySet;
import com.airfranceklm.amt.sidecar.model.*;
import com.airfranceklm.amt.sidecar.stack.AWSLambdaConfiguration;
import com.airfranceklm.amt.sidecar.stack.AWSLambdaStack;
import com.airfranceklm.amt.sidecar.stack.SidecarStackConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.airfranceklm.amt.sidecar.identity.KeyIdentifier.typedKey;
import static org.junit.Assert.*;


/**
 * Shows the bare minimum to invoke a lambda function from the console.
 */
public class TestAssumeRoleInvoker {

    private final String key = "AKIASY5OEKMHBRWGALGT";
    private final String secret = "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";
    private final String functionName = "arn:aws:lambda:eu-west-1:434277372272:function:async-analytics-sidecar";
    private final String role = "arn:aws:iam::434277372272:role/AleksAccountToCloudOpsDemo";
    private final String region = "eu-west-1";

    AWSLambdaStack stackUnderTest;
    ProcessorServices processorServices;
    ProcessorKeySet pks;

    @Before
    public void setUp() {
        processorServices = new JsonProcessorServices();

        stackUnderTest = new AWSLambdaStack();
        stackUnderTest.useProcessorServices(processorServices);

        PartyKeyDescriptor awsKey = PartyKeyDescriptor
                .partyKeyBuilder()
                .keyIdentifier(typedKey(AWSLambdaStack.KEY_TYPE_AWS_IDENTITY))
                .passwordCredentials(new BasicCredentialsDescriptor(key, secret))
                .build();

        PartyKeyDescriptor externalId = PartyKeyDescriptor
                .partyKeyBuilder()
                .keyIdentifier(new KeyIdentifier("434277372272", AWSLambdaStack.KEY_TYPE_AWS_EXTERNAL_ID))
                .passwordCredentials(new BasicCredentialsDescriptor("KLM"))
                .build();

        PartyKeyDescriptor whiteListedAccnts = PartyKeyDescriptor
                .partyKeyBuilder()
                .keyIdentifier(new KeyIdentifier("434277372272", AWSLambdaStack.KEY_TYPE_AWS_WHITELISTED_ACCNTS))
                .build();

        PartyKeyDescriptor id = PartyKeyDescriptor
                .partyKeyBuilder()
                .keyIdentifier(new KeyIdentifier("unitTestIdentity", ProcessorKeySet.CLUSTER_IDENTITY_TYPE))
                .build();

        pks = ProcessorKeySet.buildProcessorKeySet()
                .key(awsKey)
                .key(whiteListedAccnts)
                .key(id)
                .key(externalId)
                .build();

        stackUnderTest.useAlcpIdentities(pks);
    }

    @Test @Ignore("Ignore during automatic runs")
    public void testAssumeRoleInvocation() throws IOException {
        Map<String,String> stackParams = new HashMap<>();
        stackParams.put(AWSLambdaConfiguration.CFG_FUNCTION_ARN, functionName);
        stackParams.put(AWSLambdaConfiguration.CFG_ASSUME_ROLE, role);
        stackParams.put(AWSLambdaConfiguration.CFG_AWS_REGION, region);

        PreProcessorSidecarConfiguration preCfg = new PreProcessorSidecarConfiguration();
        preCfg.allocOrGetStackDemand().setParams(stackParams);

        SidecarStackConfiguration cfg = stackUnderTest.configureFrom(preCfg);
        assertFalse(preCfg.hasErrors());

        SidecarInput si = new SidecarInput();
        si.setSynchronicity(SidecarSynchronicity.RequestResponse);
        si.setPoint(SidecarInputPoint.PreProcessor);
        si.setPackageKey("unit test");

        SidecarInvocationData sid = new SidecarInvocationData(si);

        SidecarPreProcessorOutput preOut = stackUnderTest.invoke(cfg, sid, SidecarPreProcessorOutput.class);
        System.out.println(JsonHelper.toPrettyJSON(preOut));
    }

    @Test @Ignore("Ignore during automatic runs")
    public void testAssumeRoleInvocationCycleTest() throws IOException {
        Map<String,String> stackParams = new HashMap<>();
        stackParams.put(AWSLambdaConfiguration.CFG_FUNCTION_ARN, functionName);
        stackParams.put(AWSLambdaConfiguration.CFG_ASSUME_ROLE, role);
        stackParams.put(AWSLambdaConfiguration.CFG_AWS_REGION, region);

        PreProcessorSidecarConfiguration preCfg = new PreProcessorSidecarConfiguration();
        preCfg.allocOrGetStackDemand().setParams(stackParams);

        SidecarStackConfiguration cfg = stackUnderTest.configureFrom(preCfg);
        assertFalse(preCfg.hasErrors());
        System.out.println("----------");

        SidecarInput si = new SidecarInput();
        si.setSynchronicity(SidecarSynchronicity.Event);
        si.setPoint(SidecarInputPoint.PreProcessor);
        si.setPackageKey("ljfdlksadjflkdsalkjj");
        si.setServiceId("lkafljsadnfldsajflkdsa");
        si.setEndpointId("poinfadshkhrwer");
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
