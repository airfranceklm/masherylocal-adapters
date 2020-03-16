package com.airfranceklm.amt.sidecar.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Map;
import java.util.Objects;

@Data
@Builder
public class SidecarInstance {
    private SidecarDescriptor sidecar;
    private Map<String, Object> params;
    private SidecarInstanceDeployment deployment;

    public void validate(@NonNull SidecarInputPoint sip) {
        Objects.requireNonNull(deployment, "Deployment required");
        Objects.requireNonNull(sidecar, "Sidecar instance required");

        if (sidecar.getParams() != null) {
            for (SidecarParam p : sidecar.getParams()) {
                if (p.appliesAt(sip)) {
                    if (p.isRequired() && !params.containsKey(p.getParamName())) {
                        throw new IllegalArgumentException(String.format("Parameter %s is required for this sidecar", p.getParamName()));
                    }

                    Object o = params.get(p.getParamName());
                    if (!o.getClass().isAssignableFrom(p.getParameterClass())) {
                        throw new IllegalArgumentException(String.format("Parameter %s requried %s class, but %s is supplied"
                                , p.getParamName()
                                , p.getParameterClass().getName()
                                , o.getClass().getName()));
                    }
                }
            }
        }
    }


}
