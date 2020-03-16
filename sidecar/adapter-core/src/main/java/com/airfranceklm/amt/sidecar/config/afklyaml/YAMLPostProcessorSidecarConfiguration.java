package com.airfranceklm.amt.sidecar.config.afklyaml;

import com.airfranceklm.amt.sidecar.dsl.DslMethod;
import com.airfranceklm.amt.sidecar.model.MaxPayloadSizeSetting;
import com.airfranceklm.amt.sidecar.model.PostProcessorSidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.SidecarDescriptor;
import com.airfranceklm.amt.sidecar.model.SidecarInstance;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.function.Consumer;

import static com.airfranceklm.amt.sidecar.model.MaxPayloadSizeExcessAction.BlockSidecarCall;
import static com.airfranceklm.amt.sidecar.model.MaxPayloadSizeExcessAction.NoopSidecarCall;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class YAMLPostProcessorSidecarConfiguration extends YAMLCommonSidecarConfiguration<PostProcessorSidecarConfiguration> {

    public YAMLPostProcessorSidecarConfiguration() {
        super();
    }

    @Override
    protected PostProcessorSidecarConfiguration create() {
        final PostProcessorSidecarConfiguration retVal = new PostProcessorSidecarConfiguration();
        retVal.setMaximumResponsePayloadSize(new MaxPayloadSizeSetting(50 * 1024, BlockSidecarCall));
        retVal.setMaximumRequestPayloadSize(new MaxPayloadSizeSetting(50 * 1024, BlockSidecarCall));
        return retVal;

    }

    public MaxPayloadSizeSetting getMaximumResponsePayloadSize() {
        return getTarget().getMaximumResponsePayloadSize();
    }

    public void setMaximumResponsePayloadSize(MaxPayloadSizeSetting maximumResponsePayloadSize) {
        getTarget().setMaximumResponsePayloadSize(maximumResponsePayloadSize);
    }

    public YAMLPostProcessorSidecarConfiguration blockResponsePayloadsExceeding(long size) {
        setMaximumResponsePayloadSize(new MaxPayloadSizeSetting(size, BlockSidecarCall));
        return this;
    }

    public YAMLPostProcessorSidecarConfiguration noopForResponsePayloadsExceeding(long size) {
        setMaximumResponsePayloadSize(new MaxPayloadSizeSetting(size, NoopSidecarCall));
        return this;
    }

    @Override
    public void absorb(SidecarInstance desc) {
        super.absorb(desc);

        final MaxPayloadSizeSetting maxSize = desc.getSidecar().getMaxResponsePayload();
        if (maxSize != null) {
            setMaximumResponsePayloadSize(maxSize);
        }
    }
}
