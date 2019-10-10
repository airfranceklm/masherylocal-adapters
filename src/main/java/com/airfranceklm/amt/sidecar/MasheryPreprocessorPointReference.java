package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.config.SidecarInputPoint;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

import java.util.Objects;

public class MasheryPreprocessorPointReference {
    private String serviceId;
    private String endpointId;
    private SidecarInputPoint point;

    MasheryPreprocessorPointReference(ProcessorEvent pe, SidecarInputPoint sip) {
        this(pe.getEndpoint().getAPI().getExternalID(),
                pe.getEndpoint().getExternalID(),
                sip);
    }

    public MasheryPreprocessorPointReference(SidecarConfiguration cfg) {
        this(cfg.getServiceId(),
                cfg.getEndpointId(),
                cfg.getPoint());
    }

    MasheryPreprocessorPointReference(String serviceId, String endpointId, SidecarInputPoint point) {
        this.serviceId = serviceId;
        this.endpointId = endpointId;
        this.point = point;
    }

    public String getServiceId() {
        return serviceId;
    }


    public String getEndpointId() {
        return endpointId;
    }

    public SidecarInputPoint getPoint() {
        return point;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MasheryPreprocessorPointReference that = (MasheryPreprocessorPointReference) o;
        return Objects.equals(serviceId, that.serviceId) &&
                Objects.equals(point, that.point) &&
                Objects.equals(endpointId, that.endpointId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, endpointId, point);
    }
}
