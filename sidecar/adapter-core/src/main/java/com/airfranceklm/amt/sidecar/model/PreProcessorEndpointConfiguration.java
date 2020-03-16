package com.airfranceklm.amt.sidecar.model;

import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;

/**
 * Preflight endpoint configuration, a collection of three different configuration for configuration purposes.
 * <p/>
 * A corresponding class doesn't exist, as it would only require a {@link PostProcessorSidecarConfiguration} for
 * doing the post-processing.
 */
public class PreProcessorEndpointConfiguration {

    private PreFlightSidecarConfiguration preflight;
    private PreProcessorSidecarConfiguration sidecar;

    private JsonSidecarPreProcessorOutput staticModification;

    public PreFlightSidecarConfiguration getPreflight() {
        return preflight;
    }

    public void setPreflight(PreFlightSidecarConfiguration preflight) {
        this.preflight = preflight;
    }

    public PreProcessorSidecarConfiguration getSidecar() {
        return sidecar;
    }

    public void setSidecar(PreProcessorSidecarConfiguration sidecar) {
        this.sidecar = sidecar;
    }

    public JsonSidecarPreProcessorOutput getStaticModification() {
        return staticModification;
    }

    public void setStaticModification(JsonSidecarPreProcessorOutput staticModification) {
        this.staticModification = staticModification;
    }
}
