package com.airfranceklm.amt.sidecar.model.alcp.alg.hspav1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class HighSecuritySidecarOutput {

    @JsonProperty("p") @Getter @Setter
    private String responseMaterial;
    @JsonProperty("e") @Getter @Setter
    public String responseHeader;
    @JsonProperty("o") @Getter @Setter
    public String oneTimePassword;

    public void checkFields() throws MalformedHighSecuritySidecarOutput {
        if (this.getOneTimePassword() == null || this.getResponseHeader() == null || this.getResponseMaterial() == null) {
            throw new MalformedHighSecuritySidecarOutput("High-security input isn't correctly formatted");
        }
    }
}
