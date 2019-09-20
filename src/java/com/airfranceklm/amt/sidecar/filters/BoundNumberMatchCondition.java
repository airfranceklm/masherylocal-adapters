package com.airfranceklm.amt.sidecar.filters;

public class BoundNumberMatchCondition extends SidecarScopeMatchCondition<Long> {
    private long size;

    public BoundNumberMatchCondition(String label, long size) {
        super(label);
        this.size = size;
    }

    @Override
    public boolean match(Long value) {
        return value != null && value <= size;
    }
}
