package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.SidecarOutput;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SidecarOutputCache implements Serializable {

    private static final long aboutExpiryAdvance = TimeUnit.SECONDS.toMillis(15);

    private Date expiry;
    private Date storeExpiry;
    private SidecarPreProcessorOutput value;

    SidecarOutputCache() {
    }

    public SidecarOutputCache(SidecarPreProcessorOutput value, TimeUnit unit, int retention) {
        this.expiry = value.getUnchangedUntil();
        this.storeExpiry = new Date();
        this.value = value;

        extendStoreWith(unit, retention);
    }

    public Date getExpiry() {
        return expiry;
    }

    public Date getStoreExpiry() {
        return storeExpiry;
    }

    public SidecarPreProcessorOutput getValue() {
        return value;
    }

    void extendStoreWith(TimeUnit tu, int advance) {
        storeExpiry.setTime(Math.max(storeExpiry.getTime() + tu.toMillis(advance), expiry.getTime()));
    }

    /**
     * Checks if the value in storage is about to expire.
     * @return true if about to expire, false otherwise.
     */
    boolean needsStorageRefresh() {
        long chk = System.currentTimeMillis() + aboutExpiryAdvance;
        return storeExpiry.getTime() < chk && chk < expiry.getTime();
    }

    /**
     * Returns the duration of the storage.
     * @return
     */
    int getStorageDuration() {
        return (int)TimeUnit.MILLISECONDS.toSeconds(storeExpiry.getTime() - System.currentTimeMillis());
    }
}
