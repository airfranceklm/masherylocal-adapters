package com.airfranceklm.amt.testsupport.dsl;

import com.airfranceklm.amt.testsupport.TrafficManagerResponseData;

import java.util.HashMap;

public class TrafficManagerResponseDSL {
    private TrafficManagerResponseData data;

    public TrafficManagerResponseDSL(TrafficManagerResponseData data) {
        this.data = data;
    }

    public TrafficManagerResponseDSL expectCompleted(boolean how) {
        data.setExpectSetComplete(how);
        return this;
    }

    public TrafficManagerResponseDSL expectCompletedWithMessage(String msg) {
        data.setExpectSetCompleteWithMessage(msg);
        return this;
    }

    public TrafficManagerResponseDSL expectFailedWithMessage(String msg) {
        data.setExpectSetFailedWithMessage(msg);
        return this;
    }

    public TrafficManagerResponseDSL expectStatusCode(int code) {
        data.setExpectStatusCode(code);
        return this;
    }

    public TrafficManagerResponseDSL expectStatusMessage(String sMessage) {
        data.setExpectStatusMessage(sMessage);
        return this;
    }

    public TrafficManagerResponseDSL expectResponsePayload(String payload) {
        data.setExpectResponseBody(payload);
        return this;
    }

    public TrafficManagerResponseDSL expectHeader(String h, String value) {
        if (data.getResponseHeaders() == null) {
            data.setResponseHeaders(new HashMap<>());
        }
        data.getResponseHeaders().put(h, value);
        return this;
    }
}
