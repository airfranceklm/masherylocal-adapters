package com.airfranceklm.amt.sidecar.model.alcp.alg.hspav1;

import com.airfranceklm.amt.sidecar.identity.CounterpartIdentity;
import com.airfranceklm.amt.sidecar.identity.SidecarIdentity;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import com.airfranceklm.amt.sidecar.model.alcp.*;
import com.airfranceklm.amt.sidecar.JsonHelper;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.airfranceklm.amt.sidecar.model.SidecarInput.Accessor.allocOrGetRequest;
import static com.airfranceklm.amt.sidecar.model.json.JsonPayloadCarrier.allocOrGetPassHeaders;
import static com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput.allocOrGetModify;
import static org.junit.Assert.*;

/**
 * Test class for testing the high security protocol
 */
public class HighSecurityProtocolGCMModeAPITest extends IdentityTestSupport {

    @Test
    public void testRequestEncryptDecrypt() throws IOException {

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


        // Here we build the sidecar input
        SidecarInput input = new SidecarInput();
        input.setEndpointId("endpointID");
        input.setServiceId("serviceId");
        input.setMasheryMessageId("unit-test-message-id");

        input.setPackageKey("adlkfjl3u4adkfjalkdjf");
        input.setPoint(SidecarInputPoint.PreProcessor);
        allocOrGetRequest(input).addHeader("authorization", "Custom");

        System.out.println(JsonHelper.toPrettyJSON(input));
        System.out.println("--------");

        MasheryALCPSide<HighSecuritySidecarInput, HighSecuritySidecarOutput> mSide = protocol.getMasherySide(sac);

        EncryptedMessage<HighSecuritySidecarInput> encr = mSide.encrypt(input, JsonHelper.getDefaultUnmarshaller());
        assertNotNull(encr);
        assertNotNull(encr.getPayload());
        assertNull(encr.getContext());

        final String encrJson = JsonHelper.toPrettyJSON(encr.getPayload());

        System.out.println(encrJson);
        File targetFile = exchangeable("hspv1_mashToSidecar_gcm.json");
        Files.write(targetFile.toPath(), encrJson.getBytes());

        // ----------------------------------------------

        // On the sidecar side, the following is the application code that is required
        // to retrieve the protected message.

        SidecarALCPSide<HighSecuritySidecarInput, HighSecuritySidecarOutput> sSide = protocol.getSidecarSide(sidecarSelfIdentity, kmi);
        ProtectedSidecarRequest psr = sSide.decrypt(encr, JsonHelper.getDefaultUnmarshaller());
        assertNotNull(psr);
        assertNotNull(psr.getInput());

        assertEquals(input, psr.getInput());
    }


    @Test
    public void testResponseEncryptDecrypt() throws IOException {

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


        // Here we build the sidecar input
        JsonSidecarPreProcessorOutput sppo = new JsonSidecarPreProcessorOutput();
        sppo.setUnchangedFor(300);
        allocOrGetPassHeaders(allocOrGetModify(sppo)).put("X-AFKLM-Channel", "Digital");

        System.out.println(JsonHelper.toPrettyJSON(sppo));
        System.out.println("-----------");

        SidecarALCPSide<HighSecuritySidecarInput, HighSecuritySidecarOutput> sSide = protocol.getSidecarSide(sidecarSelfIdentity, kmi);

        HighSecuritySidecarOutput encr = sSide.encrypt(sppo, JsonHelper.getDefaultUnmarshaller(), masheryAsCP);
        assertNotNull(encr);

        System.out.println(JsonHelper.toPrettyJSON(encr));

        // ----------------------------------------------

        // On the Mashery side, the following is the application code that is required
        // to retrieve the protected message.

        MasheryALCPSide<HighSecuritySidecarInput, HighSecuritySidecarOutput> mSide = protocol.getMasherySide(sac);
        SidecarPreProcessorOutput psr = mSide.decrypt(encr, JsonHelper.getDefaultUnmarshaller(), SidecarPreProcessorOutput.class);
        assertNotNull(psr);

        assertEquals(psr, sppo);
        System.out.println(JsonHelper.toPrettyJSON(psr));
    }

}
