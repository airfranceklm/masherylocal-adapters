package com.airfranceklm.amt.sidecar.config.afklyaml;

import com.airfranceklm.amt.sidecar.model.MaxPayloadSizeSetting;
import com.airfranceklm.amt.sidecar.model.PreProcessorSidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.SidecarDescriptor;
import com.airfranceklm.amt.sidecar.model.SidecarInstance;
import com.fasterxml.jackson.annotation.JsonInclude;

import static com.airfranceklm.amt.sidecar.model.MaxPayloadSizeExcessAction.BlockSidecarCall;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class YAMLPreProcessorSidecarConfiguration extends YAMLCommonSidecarConfiguration<PreProcessorSidecarConfiguration> {

    public YAMLPreProcessorSidecarConfiguration() {
        super();
    }

    @Override
    protected PreProcessorSidecarConfiguration create() {
        final PreProcessorSidecarConfiguration retVal = new PreProcessorSidecarConfiguration();
        retVal.setMaximumRequestPayloadSize(new MaxPayloadSizeSetting(50 * 1024, BlockSidecarCall));
        return retVal;
    }

    public void setIdempotentAware(boolean how) {
        getTarget().setIdempotentAware(how);
    }

    public boolean isIdempotentAware() {
        return getTarget().isIdempotentAware();
    }

    public YAMLPreProcessorSidecarConfiguration withIdempotentSupport() {
        setIdempotentAware(false);
        return this;
    }

    public YAMLPreProcessorSidecarConfiguration withoutIdempotentSupport() {
        setIdempotentAware(false);
        return this;
    }

    @Override
    public void absorb(SidecarInstance desc) {
        super.absorb(desc);
        setIdempotentAware(desc.getSidecar().isIdempotentAware());
    }
}
