package com.airfranceklm.amt.sidecar.config;

/**
 * Setting for the maximum size that the sidecar should receive.
 */
public class MaxSizeSetting {
    private long maxSize;
    private MaxSizeComplianceRequirement compliance;

    public MaxSizeSetting(long maxSize, MaxSizeComplianceRequirement compliance) {
        this.maxSize = maxSize;
        this.compliance = compliance;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public MaxSizeComplianceRequirement getCompliance() {
        return compliance;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public void setCompliance(MaxSizeComplianceRequirement compliance) {
        this.compliance = compliance;
    }
}
