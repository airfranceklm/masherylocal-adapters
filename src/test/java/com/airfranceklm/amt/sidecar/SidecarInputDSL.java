package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.config.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.config.SidecarSynchronicity;

import java.util.HashMap;
import java.util.function.Consumer;

public class SidecarInputDSL {
    SidecarInput data;

    public SidecarInputDSL(SidecarInput data) {
        this.data = data;
    }

    public SidecarInputDSL synchronicity(SidecarSynchronicity se) {
        return dslOp(se, data::setSynchronicity);
    }

    public SidecarInputDSL withUnitTestMessageId() {
        data.setMasheryMessageId("unit-test-call-uuid");
        return this;
    }

    public SidecarInputDSL point(SidecarInputPoint point) {
        return dslOp(point, data::setPoint);
    }

    public SidecarInputDSL packageKey(String key) {
        return dslOp(key, data::setPackageKey);
    }

    public SidecarInputDSL serviceId(String serviceId) {
        return dslOp(serviceId, data::setServiceId);
    }

    public SidecarInputDSL endpointId(String endpointId) {
        return dslOp(endpointId, data::setEndpointId);
    }

    public HTTPInputMessageDSL request() {
        return new HTTPInputMessageDSL(data.getOrCreateRequest());
    }

    public HTTPResponseMessageDSL response() {
        return new HTTPResponseMessageDSL(data.getOrCreateResponse());
    }

    public SidecarInputDSL request(Consumer<HTTPInputMessageDSL> dsl) {
        dsl.accept(new HTTPInputMessageDSL(data.getOrCreateRequest()));
        return this;
    }

    public SidecarInputDSL response(Consumer<HTTPResponseMessageDSL> dsl) {
        dsl.accept(new HTTPResponseMessageDSL(data.getOrCreateResponse()));
        return this;
    }


    public SidecarInputDSL param(String key, Object value) {
        if (data.getParams() == null) {
            data.setParams(new HashMap<>());
        }
        data.getParams().put(key, value);
        return this;
    }

    /**
     * Removes input.
     * @return
     */
    public SidecarInputDSL withEmptyRequest() {
        data.setRequest(null);
        return this;
    }


    public class HTTPInputMessageDSL {
        SidecarInputHTTPMessage data;

        HTTPInputMessageDSL(SidecarInputHTTPMessage data) {
            this.data = data;
        }

        public HTTPInputMessageDSL withHeader(String h, String v) {
            if (data.getHeaders() == null) {
                data.setHeaders(new HashMap<>());
            }
            data.getHeaders().put(h, v);

            return this;
        }

        public HTTPInputMessageDSL withoutPayload() {
            withPayload(null);
            return this;
        }

        public HTTPInputMessageDSL withPayload(String payload) {
            data.setPayload(payload);
            data.setPayloadLength(payload == null ? 0 : payload.length());
            return this;
        }

        public HTTPInputMessageDSL base64Encoded(boolean how) {
            data.setPayloadBase64Encoded(how);
            return this;
        }

        public HTTPInputMessageDSL withPayloadLength(long i) {
            data.setPayloadLength(i);
            return this;
        }
    }

    public class HTTPResponseMessageDSL extends HTTPInputMessageDSL {
        SidecarInputHTTPResponseMessage data;

        public HTTPResponseMessageDSL(SidecarInputHTTPResponseMessage data) {
            super(data);
            this.data = data;
        }

        public HTTPResponseMessageDSL withCode(int resonseCode) {
            data.setResponseCode(resonseCode);
            return this;
        }
    }

    private <T> SidecarInputDSL dslOp(T value, Consumer<T> consumer) {
        if (consumer != null) {
            consumer.accept(value);
        }
        return this;
    }
}
