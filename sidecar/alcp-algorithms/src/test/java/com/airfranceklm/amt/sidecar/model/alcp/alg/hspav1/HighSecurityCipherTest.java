package com.airfranceklm.amt.sidecar.model.alcp.alg.hspav1;

import com.airfranceklm.amt.sidecar.identity.ClusterIdentity;
import com.airfranceklm.amt.sidecar.identity.CounterpartIdentity;
import com.airfranceklm.amt.sidecar.identity.SidecarIdentity;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.alcp.*;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.Assert.assertEquals;

public class HighSecurityCipherTest extends IdentityTestSupport {

    @Test
    public void testBasicSecurityEncryption() throws IOException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, IllegalMessageHeaderException, InvalidAlgorithmParameterException, BadPaddingException, MalformedHighSecuritySidecarInput, InvalidRequestMessageHeaderException, UnknownMasheryRequester {
        ClusterIdentity masherySelfIdent = mockClusterIdentity();
        CounterpartIdentity sidecarCounterIdent = mockSidecarIdentityAsCounterpart();

        SidecarIdentity sidecarIdent = mockSidecarIdentity();

        CounterpartIdentity mashIdent = mockClusterIdentityAsCounterpart();
        KnownMasheryIdentities mashIds = mockStrictMasheryIdentities(mashIdent);

        replayAll();

        SidecarInput input = new SidecarInput();
        input.setEndpointId("endpointID");
        input.setServiceId("serviceId");
        input.setMasheryMessageId("unit-test-message-id");

        /*
        HighSecurityInputCipher cipher = new HighSecurityInputCipher(masherySelfIdent, sidecarCounterIdent);
        HighSecuritySidecarInput hsi = cipher.encrypt(input);
        System.out.println(JsonHelper.toPrettyJSON(hsi));

        // Now, we need to decrypt this.
        SidecarHighSecurityInputDecipher decipher = new SidecarHighSecurityInputDecipher(sidecarIdent, mashIds);
        SidecarInput restored = decipher.decipher(hsi);

        assertEquals(input, restored);
         */
    }
}
