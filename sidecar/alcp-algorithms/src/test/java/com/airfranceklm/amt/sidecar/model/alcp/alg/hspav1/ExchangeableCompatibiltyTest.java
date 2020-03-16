package com.airfranceklm.amt.sidecar.model.alcp.alg.hspav1;

import com.airfranceklm.amt.sidecar.identity.CounterpartIdentity;
import com.airfranceklm.amt.sidecar.identity.SidecarIdentity;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import com.airfranceklm.amt.sidecar.model.alcp.*;
import com.airfranceklm.amt.sidecar.JsonHelper;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

@Ignore("Ignore for automatic tests")
public class ExchangeableCompatibiltyTest extends IdentityTestSupport {

    @Test
    public void testResponseDecryptionFromExchangeable() throws IOException {

        SidecarAuthenticationChannel sac = mockRequestChannel();
        SidecarIdentity sidecarSelfIdentity = mockSidecarIdentity();

        CounterpartIdentity masheryAsCP = mockClusterIdentityAsCounterpart();
        KnownMasheryIdentities kmi = mockStrictMasheryIdentities(masheryAsCP);
        replayAll();

        // The application usage
        // ------------------------------------------------------

        ALCPAlgorithmSpec spec = new ALCPAlgorithmSpec()
                .addParam(HighSecurityAlgorithmV1Options.GCMMode.name(), true)
                ;  // Bi-directional specification

        ALCPAlgorithm<HighSecuritySidecarInput, HighSecuritySidecarOutput> protocol =
                HighSecurityProtectionAlgorithmV1.fromSpec(spec);


        HighSecuritySidecarOutput encr = JsonHelper.parse(exchangeable("hspv1_sidecarToMash_gcm.json"), HighSecuritySidecarOutput.class);
        assertNotNull(encr);

        System.out.println(JsonHelper.toPrettyJSON(encr));

        // ----------------------------------------------

        // On the Mashery side, the following is the application code that is required
        // to retrieve the protected message.

        MasheryALCPSide<HighSecuritySidecarInput, HighSecuritySidecarOutput> mSide = protocol.getMasherySide(sac);
        SidecarPreProcessorOutput psr = mSide.decrypt(encr, JsonHelper.getDefaultUnmarshaller(), SidecarPreProcessorOutput.class);
        assertNotNull(psr);

//        assertEquals(psr, sppo);
        System.out.println(JsonHelper.toPrettyJSON(psr));
    }
}
