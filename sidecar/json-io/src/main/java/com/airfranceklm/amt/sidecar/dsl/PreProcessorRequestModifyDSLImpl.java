package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.model.json.JsonRequestModificationCommand;
import com.airfranceklm.amt.sidecar.model.json.JsonRequestRoutingChangeBean;

import static com.airfranceklm.amt.sidecar.CommonExpressions.allocOrGet;

class PreProcessorRequestModifyDSLImpl extends BaseModifyDSLImpl<JsonRequestModificationCommand>
    implements PreProcessorRequestModifyDSL {

    public PreProcessorRequestModifyDSLImpl(JsonRequestModificationCommand target) {
        super(target);
    }

    @Override
    public ChangeRouteDSL changeRoute() {
        return new ChangeRoutingDSLImpl(allocOrGet(target::getChangeRoute, target::setChangeRoute, JsonRequestRoutingChangeBean::new));
    }
}
