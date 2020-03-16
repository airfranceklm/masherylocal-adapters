package com.airfranceklm.amt.sidecar.model;

import lombok.*;

import java.util.Map;

@Data
@Builder
public class Sidecars {
    @Getter @Setter @Singular
    Map<String, SidecarDescriptor> sidecars;
}
