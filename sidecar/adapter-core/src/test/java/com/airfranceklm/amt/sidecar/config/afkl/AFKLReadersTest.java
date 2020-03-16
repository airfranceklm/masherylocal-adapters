package com.airfranceklm.amt.sidecar.config.afkl;

import com.airfranceklm.amt.sidecar.model.PreFlightSidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.PreProcessorSidecarConfiguration;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AFKLReadersTest {
    @Test
    public void testCreatingCorrectNumberOfElements() {
        Map<String,String> map= new HashMap<>();
        map.put("elements", "messageId|packageKey");
        map.put("require-request-headers", "authorization");
        map.put("require-eav", "Public_Key");
        map.put("synchronicity", "request-response");

        AFKLPreProcessor preReader = new AFKLPreProcessor();
        PreProcessorSidecarConfiguration preCfg = preReader.read(map);

        assertNotNull(preCfg);
        assertNotNull(preCfg.getElements());
        assertEquals(4, preCfg.getElements().size());

        AFKLPreflight preflightReader = new AFKLPreflight();
        PreFlightSidecarConfiguration preflightCfg = preflightReader.read(map);

        assertNotNull(preflightCfg);
        assertNull(preflightCfg.getEnabled());
        // We should have no elements indicated for this.
        assertNull(preflightCfg.getElements());
        assertFalse(preflightCfg.preflightDemanded());

    }
}
