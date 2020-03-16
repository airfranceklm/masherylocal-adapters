package com.airfranceklm.amt.sidecar.model;

import com.airfranceklm.amt.sidecar.model.MaxPayloadSizeSetting;
import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.SidecarInputPoint;
import lombok.Getter;
import lombok.Setter;

import static com.airfranceklm.amt.sidecar.model.SidecarInputPoint.PostProcessor;

/**
 * Post-processor configuration
 */
public class PostProcessorSidecarConfiguration extends SidecarConfiguration {

    @Getter
    @Setter
    private MaxPayloadSizeSetting maximumResponsePayloadSize;

    public void maximumResponsePayloadSizeFrom(Long size, MaxPayloadSizeExcessAction action) {
        maximumResponsePayloadSize = new MaxPayloadSizeSetting(size, action);
    }

    @Override
    public SidecarInputPoint getPoint() {
        return PostProcessor;
    }
}
