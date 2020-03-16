package com.airfranceklm.amt.sidecar.model;

import lombok.Getter;

import java.util.Objects;

public class MasheryProcessorPointReference {
    @Getter
    private String serviceId;
    @Getter
    private String endpointId;
    @Getter
    private SidecarInputPoint point;

    public MasheryProcessorPointReference(String serviceId, String endpointId, SidecarInputPoint point) {
        this.serviceId = serviceId;
        this.endpointId = endpointId;
        this.point = point;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MasheryProcessorPointReference that = (MasheryProcessorPointReference) o;
        return Objects.equals(serviceId, that.serviceId) &&
                Objects.equals(point, that.point) &&
                Objects.equals(endpointId, that.endpointId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, endpointId, point);
    }
}
