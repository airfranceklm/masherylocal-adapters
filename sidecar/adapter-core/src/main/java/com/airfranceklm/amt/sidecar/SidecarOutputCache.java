package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@NoArgsConstructor
public class SidecarOutputCache implements Serializable {

    private static final long aboutExpiryAdvance = TimeUnit.SECONDS.toMillis(15);

    @Getter
    private SidecarPreProcessorOutput value;
    @Getter
    private long storeExpiry;
    @Getter
    private long expiry;

    public SidecarOutputCache(SidecarOutputCache another) {
        this(another.getValue(), another.getStoreExpiry(), another.getExpiry());
    }

    public SidecarOutputCache(SidecarPreProcessorOutput value, long now, TimeUnit unit, int retention) {
       this();
       storeFor(value, now, unit, retention);
    }

    /**
     * Store the <code>value</code> for duration computed from <code>unit</code> and <code>retention</code>
     * parameters, from the timestamp identified by <code>now</code>.
     * @param value value to store
     * @param now timestamp indicating the current timestamp
     * @param unit unit of retention
     * @param retention number of <code>unit</code>s in the retention period.
     */
    void storeFor(SidecarPreProcessorOutput value, long now, TimeUnit unit, int retention) {
        this.expiry = value.effectiveUnchangedUntil().getTime();
        this.storeExpiry = now; // extended with extendStoreWith() below.
        this.value = value;

        extendStoreWith(unit, retention);
    }

    /**
     * Store the given valid until a pre-calculated timestamp.
     * @param value object to store
     * @param pStoreExpiry storage expiry time.
     */
    void storeUntil(SidecarPreProcessorOutput value, Date pStoreExpiry) {
        this.expiry = value.getUnchangedUntil().getTime();
        this.storeExpiry = pStoreExpiry.getTime();
        this.value = value;
    }

    void extendStoreWith(TimeUnit tu, int advance) {
        storeExpiry = Math.min(storeExpiry + tu.toMillis(advance), expiry);
    }

    boolean needsStorageRefresh(long queryTs) {
        long chk = queryTs + aboutExpiryAdvance;
        return storeExpiry < chk && chk < expiry;
    }

    /**
     * Returns the duration of the storage.
     * @return
     */
    int getStorageDuration(long queryTs) {
        // Storage duration must be more than
        return Math.max(0, (int) TimeUnit.MILLISECONDS.toSeconds(storeExpiry - queryTs));
    }

    public boolean isValid(long timestamp) {
        return this.expiry >= timestamp;
    }

    /**
     * Checks if it makes sense to store this data in memory
     * @param queryTs timestamp of the reference moment
     * @return true if storage in cache is adviseable, false otherwise.
     */
    public boolean isStorable(long queryTs) {
        return queryTs <= expiry - aboutExpiryAdvance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SidecarOutputCache that = (SidecarOutputCache) o;
        return expiry == that.expiry &&
                storeExpiry == that.storeExpiry &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expiry, storeExpiry, value);
    }
}
