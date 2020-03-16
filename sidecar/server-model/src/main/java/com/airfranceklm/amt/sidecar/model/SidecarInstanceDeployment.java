package com.airfranceklm.amt.sidecar.model;


import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SidecarInstanceDeployment {
    String environmentName;
    StackDemand stack;
}
