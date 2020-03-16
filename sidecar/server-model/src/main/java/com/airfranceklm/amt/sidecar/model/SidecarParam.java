package com.airfranceklm.amt.sidecar.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class SidecarParam {
    String paramName;
    @Builder.Default
    Class<?> parameterClass = String.class;

    @Singular
    List<SidecarInputPoint> points;

    @Builder.Default
    boolean required = false;

    public boolean appliesAt(SidecarInputPoint sip) {
        return points == null || points.contains(sip);
    }
}
