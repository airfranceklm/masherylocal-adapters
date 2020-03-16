package com.airfranceklm.amt.sidecar.config.afklyaml;

import com.airfranceklm.amt.sidecar.dsl.DslMethod;
import com.airfranceklm.amt.sidecar.model.MaxPayloadSizeSetting;
import com.airfranceklm.amt.sidecar.model.PreFlightSidecarConfiguration;
import com.fasterxml.jackson.annotation.JsonInclude;

import static com.airfranceklm.amt.sidecar.model.MaxPayloadSizeExcessAction.BlockSidecarCall;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class YAMLPreflightSidecarConfiguration extends YAMLCommonSidecarConfiguration<PreFlightSidecarConfiguration> {

    public YAMLPreflightSidecarConfiguration() {
        super();
    }

    @Override
    protected PreFlightSidecarConfiguration create() {
        final PreFlightSidecarConfiguration retVal = new PreFlightSidecarConfiguration();
        retVal.setMaximumRequestPayloadSize(new MaxPayloadSizeSetting(50 * 1024, BlockSidecarCall));
        return retVal;
    }

    public Boolean getEnabled() {
        return getTarget().getEnabled();
    }

    public void setEnabled(Boolean enabled) {
        getTarget().setEnabled(enabled);
    }

    @DslMethod
    public YAMLPreflightSidecarConfiguration preflightEnabled() {
        setEnabled(true);
        return this;
    }
}
