package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.model.*;

import java.util.HashMap;
import java.util.function.Consumer;

import static com.airfranceklm.amt.sidecar.model.SidecarInput.Accessor.*;

/**
 * Sidecar input definition DSL
 */
public class SidecarInputDSL {
    SidecarInput data;

    public SidecarInputDSL(SidecarInput data) {
        this.data = data;
    }

    public SidecarInputDSL synchronicity(SidecarSynchronicity se) {
        return dslOp(se, data::setSynchronicity);
    }

    public SidecarInputDSL unitTestMessageId() {
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

    public HTTPRequestMessageDSL request() {
        return new HTTPRequestMessageDSL(allocOrGetRequest(data));
    }

    public HTTPResponseMessageDSL response() {
        return new HTTPResponseMessageDSL(allocOrGetResponse(data));
    }

    public SidecarInputDSL request(Consumer<HTTPRequestMessageDSL> dsl) {
        dsl.accept(new HTTPRequestMessageDSL(allocOrGetRequest(data)));
        return this;
    }

    public SidecarInputDSL response(Consumer<HTTPResponseMessageDSL> dsl) {
        dsl.accept(new HTTPResponseMessageDSL(allocOrGetResponse(data)));
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
     * @return reference ot itself
     */
    public SidecarInputDSL emptyRequest() {
        data.setRequest(null);
        return this;
    }

    private <T> SidecarInputDSL dslOp(T value, Consumer<T> consumer) {
        if (consumer != null) {
            consumer.accept(value);
        }
        return this;
    }
}
