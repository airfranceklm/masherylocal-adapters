package com.airfranceklm.amt.sidecar.model.alcp;

import com.airfranceklm.amt.sidecar.model.SidecarSynchronicity;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EncryptedMessageTest {

    @Test
    public void testCreatingSyncMessage() {
        EncryptedMessage<Object> em = new EncryptedMessage<>(SidecarSynchronicity.RequestResponse, "ABC");
        assertEquals(SidecarSynchronicity.RequestResponse, em.getSynchronicity());
        assertEquals("ABC", em.getPayload());
    }

    @Test
    public void testGeneratorWithBuilderAPI() {
        EncryptedMessage<String> em = EncryptedMessage.<String>builder()
                .synchronicity(SidecarSynchronicity.RequestResponse)
                .payload("ABCD")
                .contextEntry("CE", "34")
                .build();

        assertEquals(SidecarSynchronicity.RequestResponse, em.getSynchronicity());
        assertEquals("ABCD", em.getPayload());
        assertEquals(1, em.getContext().size());
        assertEquals("34", em.getContext().get("CE"));
    }
}
