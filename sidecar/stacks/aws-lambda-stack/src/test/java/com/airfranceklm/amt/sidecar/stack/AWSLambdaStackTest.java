package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.identity.KeyIdentifier;
import com.airfranceklm.amt.sidecar.identity.PartyKeyDescriptor;
import com.airfranceklm.amt.sidecar.identity.ProcessorKeySet;
import com.airfranceklm.amt.sidecar.model.BasicSidecarConfiguration;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class AWSLambdaStackTest {

    @Test
    public void testSettingTimeout() {
        AWSLambdaConfiguration cfg = new AWSLambdaConfiguration(null, -1);
        assertEquals(100L, cfg.getTimeout());

        cfg = new AWSLambdaConfiguration(null, 99);
        assertEquals(100L, cfg.getTimeout());

        cfg = new AWSLambdaConfiguration(null, 150);
        assertEquals(150L, cfg.getTimeout());
    }

    @Test
    public void testCheckAccountWhitelisting() {
        AWSLambdaStack stack = new AWSLambdaStack();

        PartyKeyDescriptor desc = PartyKeyDescriptor.partyKeyBuilder()
                .keyIdentifier(new KeyIdentifier("1,2;3|4", AWSLambdaStack.KEY_TYPE_AWS_WHITELISTED_ACCNTS))
                .build();

        ProcessorKeySet pks = ProcessorKeySet.buildProcessorKeySet()
                .key(desc)
                .build();

        stack.useAlcpIdentities(pks);

        BasicSidecarConfiguration cfg = new BasicSidecarConfiguration();

        stack.checkAccountWhiteListing(cfg, "1");
        assertFalse(cfg.hasErrors());

        stack.checkAccountWhiteListing(cfg, "2");
        assertFalse(cfg.hasErrors());

        stack.checkAccountWhiteListing(cfg, "3");
        assertFalse(cfg.hasErrors());

        stack.checkAccountWhiteListing(cfg, "4");
        assertFalse(cfg.hasErrors());

        stack.checkAccountWhiteListing(cfg, "5");
        assertTrue(cfg.hasErrors());
        assertTrue(cfg.getMessages().contains("Account '5' is not white-listed"));

    }

    @Test
    public void testNullDemandCreation() {
        AWSLambdaStack stack = new AWSLambdaStack();

        SidecarStackConfiguration ssc1 = stack.configureFrom(null);
        assertNotNull(ssc1);
        assertFalse(ssc1.isValid());

        SidecarStackConfiguration ssc2 = stack.configureFrom(new BasicSidecarConfiguration());
        assertNotNull(ssc2);
        assertFalse(ssc2.isValid());
    }

    @Test
    public void testFunctionNameMatcher() {
        AWSLambdaStack stack = new AWSLambdaStack();
        Matcher m = stack.matcherForFunctionName("arn:aws:lambda:eu-west-1:434277372272:function:async-analytics-sidecar");
        assertTrue(m.matches());
    }

}
