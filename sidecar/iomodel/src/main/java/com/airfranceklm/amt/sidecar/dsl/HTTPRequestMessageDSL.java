package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.model.SidecarInputHTTPMessage;

import java.util.HashMap;

public class HTTPRequestMessageDSL {
    SidecarInputHTTPMessage data;

    HTTPRequestMessageDSL(SidecarInputHTTPMessage data) {
        this.data = data;
    }

    public HTTPRequestMessageDSL header(String h, String v) {
        if (data.getHeaders() == null) {
            data.setHeaders(new HashMap<>());
        }
        data.getHeaders().put(h, v);

        return this;
    }

    public HTTPRequestMessageDSL noPayload() {
        payload(null);
        return this;
    }

    public HTTPRequestMessageDSL payload(String payload) {
        data.setPayload(payload);
        data.setPayloadLength(payload == null ? 0 : (long)payload.length());
        return this;
    }

    public HTTPRequestMessageDSL base64Encoded(boolean how) {
        data.setPayloadBase64Encoded(how);
        return this;
    }

    public HTTPRequestMessageDSL payloadLength(long i) {
        data.setPayloadLength(i);
        return this;
    }
}
