package com.airfranceklm.amt.sidecar.config.afklyaml;

import com.airfranceklm.amt.sidecar.model.PostProcessorSidecarConfiguration;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class YAMLPostProcessorPointConfiguration  {

    private YAMLPostProcessorSidecarConfiguration sidecar;

    @JsonProperty("sidecar")
    public YAMLPostProcessorSidecarConfiguration getSidecar() {
        return sidecar;
    }

    public void setSidecar(YAMLPostProcessorSidecarConfiguration sidecar) {
        this.sidecar = sidecar;
    }

    public PostProcessorSidecarConfiguration configuredSidecar() {
        return sidecar == null ? null : sidecar.retrieve();
    }
}
