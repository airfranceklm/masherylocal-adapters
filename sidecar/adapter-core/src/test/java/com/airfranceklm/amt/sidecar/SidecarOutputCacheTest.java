package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SidecarOutputCacheTest extends EasyMockSupport {

    @Test
    public void testStoreDuration() {
        Date refDate = JsonHelper.parseJSONDate("2019-10-10T12:23:34Z");
        Date storeExpiryDate = JsonHelper.parseJSONDate("2019-10-10T12:28:34Z");
        Date secondExpiryDate = JsonHelper.parseJSONDate("2019-10-10T12:33:34Z");

        Date expiryDate = JsonHelper.parseJSONDate("2019-10-10T12:53:34Z");

        replayAll();

        SidecarOutputCache soc = new SidecarOutputCache();
        soc.storeFor(createOutput(expiryDate), refDate.getTime(), TimeUnit.MINUTES, 5);
        assertEquals(expiryDate.getTime(),  soc.getExpiry());
        assertEquals(storeExpiryDate.getTime(), soc.getStoreExpiry());
        assertEquals(300, soc.getStorageDuration(refDate.getTime()));

        soc.extendStoreWith(TimeUnit.MINUTES, 5);
        assertEquals(secondExpiryDate.getTime(), soc.getStoreExpiry());

        verifyAll();
    }

    private JsonSidecarPreProcessorOutput createOutput(Date expiryDate) {
        JsonSidecarPreProcessorOutput op = new JsonSidecarPreProcessorOutput();
        op.setUnchangedUntil(expiryDate);
        return op;
    }

    @Test
    public void testNeedStorageRefresh() {
        Date refDate = JsonHelper.parseJSONDate("2019-10-10T14:00:00Z");
        Date beforeExp = JsonHelper.parseJSONDate("2019-10-10T14:04:44Z");
        Date triggerDate = JsonHelper.parseJSONDate("2019-10-10T14:04:57Z");

        Date expiryDate = JsonHelper.parseJSONDate("2019-10-10T14:53:34Z");

        SidecarOutputCache soc = new SidecarOutputCache();

        replayAll();

        soc.storeFor(createOutput(expiryDate), refDate.getTime(), TimeUnit.MINUTES, 5);

        // First time, we get shortly before the threshold timestamp.
        assertFalse(soc.needsStorageRefresh(beforeExp.getTime()));

        // Second time, we get a bit after the threshold timestamp.
        assertTrue(soc.needsStorageRefresh(triggerDate.getTime()));

        verifyAll();
    }
}
