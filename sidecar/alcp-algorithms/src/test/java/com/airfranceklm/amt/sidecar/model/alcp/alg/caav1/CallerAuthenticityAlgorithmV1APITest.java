package com.airfranceklm.amt.sidecar.model.alcp.alg.caav1;

import com.airfranceklm.amt.sidecar.JsonHelper;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.model.alcp.*;
import com.airfranceklm.amt.sidecar.model.alcp.alg.caav1.CallerAuthenticityAlgorithmV1.ResponseType;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

public class CallerAuthenticityAlgorithmV1APITest extends IdentityTestSupport {

    @Test
    public void testEncryptionAndDecryptionAPI() throws IOException {
        SidecarAuthenticationChannel sac = mockIdentityOnlyRequestChannel();
        KnownMasheryIdentities kmi = mockStrictMasheryIdentities(mockClusterIdentityAsCounterpart());

        replayAll();

        ALCPAlgorithm<SidecarInput, ResponseType> alg = CallerAuthenticityAlgorithmV1.fromSpec(null);
        MasheryALCPSide<SidecarInput, ResponseType> masherySide = alg.getMasherySide(sac);

        SidecarInput si = new SidecarInput();
        si.setMasheryMessageId("TestId");
        si.setPoint(SidecarInputPoint.PreProcessor);
        si.setPackageKey("SampleKey");

        EncryptedMessage<SidecarInput> em = masherySide.encrypt(si, JsonHelper.getDefaultUnmarshaller());

        assertNotNull(em);
        assertSame(si, em.getPayload());

        assertNotNull(em.getContext());
        assertEquals(1, em.getContext().size());
        assertNotNull(em.getContext().get(CallerAuthenticityAlgorithmV1.AUTH_CONTEXT_NAME));

        System.out.println(JsonHelper.toPrettyJSON(si));
        System.out.println(em.getContext().get(CallerAuthenticityAlgorithmV1.AUTH_CONTEXT_NAME));

        // ------------------------------------------------
        // Invocation at the sidecar side

        SidecarALCPSide<SidecarInput,ResponseType> sidecarSide = alg.getSidecarSide(null, kmi);
        ProtectedSidecarRequest psr = sidecarSide.decrypt(em, JsonHelper.getDefaultUnmarshaller());
        assertNotNull(psr);
        assertSame(si, psr.getInput());
        assertNotNull(psr.getCaller());

        verifyAll();
    }
}
