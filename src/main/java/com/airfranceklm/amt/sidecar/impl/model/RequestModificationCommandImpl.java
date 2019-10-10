package com.airfranceklm.amt.sidecar.impl.model;

import com.airfranceklm.amt.sidecar.model.RequestModificationCommand;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestModificationCommandImpl extends CallModificationCommandImpl implements RequestModificationCommand {

    private RequestRoutingChangeBeanImpl changeRoute;
    private Integer completeWithCode;

    @Override
    public RequestRoutingChangeBeanImpl getChangeRoute() {
        return changeRoute;
    }

    public void setChangeRoute(RequestRoutingChangeBeanImpl changeRoute) {
        this.changeRoute = changeRoute;
    }

    @Override
    public Integer getCompleteWithCode() {
        return completeWithCode;
    }

    public void setCompleteWithCode(Integer completeWithCode) {
        this.completeWithCode = completeWithCode;
    }

    public boolean containsNullsOnly() {
        return super.containsOnlyNulls() &&
                (changeRoute == null || changeRoute.containsOnlyNulls()) &&
                completeWithCode == null;
    }


}
