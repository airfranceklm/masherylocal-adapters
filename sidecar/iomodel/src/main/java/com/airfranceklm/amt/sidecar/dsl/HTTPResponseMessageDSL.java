package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.model.SidecarInputHTTPResponseMessage;

public class HTTPResponseMessageDSL extends HTTPRequestMessageDSL {
    SidecarInputHTTPResponseMessage data;

    public HTTPResponseMessageDSL(SidecarInputHTTPResponseMessage data) {
        super(data);
        this.data = data;
    }

    public HTTPResponseMessageDSL statusCode(int resonseCode) {
        data.setStatusCode(resonseCode);
        return this;
    }
}
