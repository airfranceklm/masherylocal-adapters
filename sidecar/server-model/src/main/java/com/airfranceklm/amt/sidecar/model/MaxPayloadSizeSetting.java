package com.airfranceklm.amt.sidecar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Setting for the maximum size that the sidecar should receive.
 */
public class MaxPayloadSizeSetting {

    private static final long DEFAULT_MAX_SIZE = 50 * 2014;
    private static final MaxPayloadSizeExcessAction DEFAULT_ACTION = MaxPayloadSizeExcessAction.BlockSidecarCall;

    @Getter @Setter
    private long limit;
    @Getter @Setter
    private MaxPayloadSizeExcessAction action;

    public MaxPayloadSizeSetting() {
        this(DEFAULT_MAX_SIZE, DEFAULT_ACTION);
    }

    public MaxPayloadSizeSetting(long limit, MaxPayloadSizeExcessAction action) {
        this.limit = limit;
        this.action = action;
    }

    @JsonIgnore
    public boolean isDefault() {
        return limit != DEFAULT_MAX_SIZE && action != DEFAULT_ACTION;
    }

    public static MaxPayloadSizeSetting blockPayloadsExceeding(long size) {
        return new MaxPayloadSizeSetting(size, MaxPayloadSizeExcessAction.BlockSidecarCall);
    }

    public static MaxPayloadSizeSetting noopPayloadsExceeding(long size) {
        return new MaxPayloadSizeSetting(size, MaxPayloadSizeExcessAction.NoopSidecarCall);
    }

    public static MaxPayloadSizeSetting effectiveFrom(MaxPayloadSizeSetting configured) {
        if (configured == null) {
            return new MaxPayloadSizeSetting(DEFAULT_MAX_SIZE, DEFAULT_ACTION);
        } else if (configured.getLimit() > 0 && configured.getAction() != null) {
            return configured;
        } else {
            return new MaxPayloadSizeSetting(
                    configured.getLimit() > 0 ? configured.getLimit() : DEFAULT_MAX_SIZE
                    , configured.getAction() != null ? configured.getAction() : DEFAULT_ACTION
            );
        }
    }
}
