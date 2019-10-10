package com.airfranceklm.amt.sidecar.impl.model;

import com.airfranceklm.amt.sidecar.model.ResponseModificationCommand;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseModificationCommandImpl extends CallModificationCommandImpl implements ResponseModificationCommand {
    private Integer code;

    @Override
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public boolean containsOnlyNulls() {
        return super.containsOnlyNulls() && code == null;
    }
}
