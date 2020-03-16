package com.airfranceklm.amt.sidecar.config.afkl;

import com.airfranceklm.amt.sidecar.config.KeySpecialization;
import com.airfranceklm.amt.sidecar.model.MaxPayloadSizeSetting;
import com.airfranceklm.amt.sidecar.model.PreFlightSidecarConfiguration;

import static com.airfranceklm.amt.sidecar.model.MaxPayloadSizeExcessAction.BlockSidecarCall;
import static com.airfranceklm.amt.sidecar.model.SidecarSynchronicity.RequestResponse;

public class AFKLPreflight
        extends AFKLDialectBase<PreFlightSidecarConfiguration> {

    private static final String CFG_ENABLED = "enabled";

    public AFKLPreflight() {
        super("preflight-");

        supportCommon();
        add(CFG_ENABLED, KeySpecialization.ScopedKey, (cfg, m, v) -> {
            Boolean b = standardBooleanLexeme(v);
            if (b != null) {
                cfg.setEnabled(b);
                return 0;
            } else {
                yieldParseMessage(String.format("Lexeme '%s' is not a valid boolean expression", v));
                return 1;
            }
        });
    }

    @Override
    protected PreFlightSidecarConfiguration create() {
        final PreFlightSidecarConfiguration retVal = new PreFlightSidecarConfiguration();
        retVal.setSynchronicity(RequestResponse);
        retVal.setMaximumRequestPayloadSize(new MaxPayloadSizeSetting(DEFAULT_MAX_PAYLOAD, BlockSidecarCall));

        return retVal;
    }


}
