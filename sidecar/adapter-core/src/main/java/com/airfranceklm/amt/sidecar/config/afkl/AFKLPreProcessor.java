package com.airfranceklm.amt.sidecar.config.afkl;

import com.airfranceklm.amt.sidecar.config.KeySpecialization;
import com.airfranceklm.amt.sidecar.model.MaxPayloadSizeSetting;
import com.airfranceklm.amt.sidecar.model.PreProcessorSidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.SidecarInstance;

import java.util.Map;

import static com.airfranceklm.amt.sidecar.model.MaxPayloadSizeExcessAction.BlockSidecarCall;
import static com.airfranceklm.amt.sidecar.model.SidecarSynchronicity.Event;

public class AFKLPreProcessor extends AFKLDialectBase<PreProcessorSidecarConfiguration> {

    static final String CFG_ENABLE_ROUTING_POSTPROCESSING = "postprocess-after-routing-change";

    public AFKLPreProcessor() {
        super();

        supportCommon();
        supportSynchronicity();
        supportLimiters();

        add(CFG_ENABLE_ROUTING_POSTPROCESSING, KeySpecialization.CommonKey, (cfg, m, v) -> {
            Boolean b = standardBooleanLexeme(v);
            if (b != null) {
                cfg.setPostProcessOnRouteChange(b);
                return 0;
            } else {
                yieldParseMessage(String.format("Lexeme '%s' is not a valid expression", v));
                return 1;
            }
        });
    }

    @Override
    protected PreProcessorSidecarConfiguration create() {
        final PreProcessorSidecarConfiguration retVal = new PreProcessorSidecarConfiguration();
        retVal.setSynchronicity(Event);
        retVal.setMaximumRequestPayloadSize(new MaxPayloadSizeSetting(DEFAULT_MAX_PAYLOAD, BlockSidecarCall));

        return retVal;
    }
}
