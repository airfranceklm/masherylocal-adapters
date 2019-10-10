package com.airfranceklm.amt.sidecar.impl.model;

import com.airfranceklm.amt.sidecar.model.SidecarOutputTerminationCommand;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SidecarOutputTerminationCommandImpl extends PayloadCarrierImpl implements SidecarOutputTerminationCommand {

    private Integer code;
    private String message;
    private Map<String,String> headers;

    @Override
    public boolean specifiesContentType() {
        return headers != null && headers.containsKey("content-type");
    }

    @Override
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public boolean containsOnlyNulls() {
        return super.containsOnlyNulls() &&
                this.message == null &&
                this.code == null &&
                (this.headers == null || this.headers.size() ==0);
    }
}
