package com.airfranceklm.amt.sidecar.config.afkl;

import com.airfranceklm.amt.sidecar.model.MaxPayloadSizeSetting;
import com.airfranceklm.amt.sidecar.model.PostProcessorSidecarConfiguration;

import static com.airfranceklm.amt.sidecar.config.KeySpecialization.CommonKey;
import static com.airfranceklm.amt.sidecar.CommonExpressions.splitStandardValueList;
import static com.airfranceklm.amt.sidecar.elements.ParameterizedStringElement.ResponseHeader;
import static com.airfranceklm.amt.sidecar.elements.ParameterizedStringElement.ResponseHeadersSkipping;
import static com.airfranceklm.amt.sidecar.model.MaxPayloadSizeExcessAction.BlockSidecarCall;
import static com.airfranceklm.amt.sidecar.model.SidecarSynchronicity.Event;

public class AFKLPostProcessor extends AFKLDialectBase<PostProcessorSidecarConfiguration> {

    public AFKLPostProcessor() {
        super();
        readPostProcessorConfiguration();

        supportCommon();
        supportSynchronicity();
        supportLimiters();

        addSimple(CFG_INCLUDE_RESPONSE_HEADERS, CommonKey, (cfg, m, v) -> cfg.demandAll(ResponseHeader.getElementName(), splitStandardValueList(v)));
        addSimple(CFG_SKIP_RESPONSE_HEADERS, CommonKey, (cfg, m, v) -> cfg.demandElement(ResponseHeadersSkipping.getElementName(), v));

        add(CFG_MAX_RESPONSE_PAYLOAD, CommonKey, (cfg, m, v) -> parsePayloadSize(cfg, m, v, cfg::maximumResponsePayloadSizeFrom));
        add(CFG_MAX_PAYLOAD, CommonKey, (cfg, m, v) -> {
            if (cfg.getMaximumRequestPayloadSize() != null && cfg.getMaximumRequestPayloadSize().isDefault()) {
                return parsePayloadSize(cfg, m, v, cfg::maximumRequestPayloadSizeFrom);
            } else {
                return 0;
            }

        });


    }

    @Override
    protected PostProcessorSidecarConfiguration create() {
        final PostProcessorSidecarConfiguration retVal = new PostProcessorSidecarConfiguration();
        retVal.setSynchronicity(Event);
        retVal.setMaximumRequestPayloadSize(new MaxPayloadSizeSetting(DEFAULT_MAX_PAYLOAD, BlockSidecarCall));
        retVal.setMaximumResponsePayloadSize(new MaxPayloadSizeSetting(DEFAULT_MAX_PAYLOAD, BlockSidecarCall));

        return retVal;
    }
}
