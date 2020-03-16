package com.airfranceklm.amt.sidecar.model.alcp.alg.hspav1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Structure for the high-secured input
 */
public class HighSecuritySidecarInput {

    @JsonProperty("m") @Getter @Setter
    private String requestMaterial;
    @JsonProperty("h") @Getter @Setter
    private String messageHeader;

    @JsonProperty("o") @Getter @Setter
    private String oneTimePassword;
}
