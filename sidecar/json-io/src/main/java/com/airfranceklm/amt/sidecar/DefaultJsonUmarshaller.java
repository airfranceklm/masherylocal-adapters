package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.dsl.SidecarPostProcessorOutputDSL;
import com.airfranceklm.amt.sidecar.dsl.SidecarPostProcessorOutputDSLImpl;
import com.airfranceklm.amt.sidecar.dsl.SidecarPreProcessorOutputDSL;
import com.airfranceklm.amt.sidecar.dsl.SidecarPreProcessorOutputDSLImpl;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;

import java.io.IOException;

import static com.airfranceklm.amt.sidecar.JsonHelper.*;

class DefaultJsonUmarshaller implements JsonIO {

    @Override
    public <T> T readJson(String rawJson, Class<T> cls) throws IOException {
        return parse(rawJson, getUnmarshallingImplementation(cls));
    }

    @Override
    public byte[] toTransportOptimizedJSON(Object obj) throws IOException {
        return JsonHelper.toTransportOptimizedJSON(obj);
    }

    @Override
    public <T> T readTransportOptimizedJSON(byte[] body, Class<T> clazz) throws IOException {
        return JsonHelper.readTransportOptimizedJSON(body, getUnmarshallingImplementation(clazz));
    }

    @Override
    public SidecarPreProcessorOutputDSL atPreProcessorPoint() {
        return new SidecarPreProcessorOutputDSLImpl(new JsonSidecarPreProcessorOutput());
    }

    @Override
    public SidecarPostProcessorOutputDSL atPostProcessorPoint() {
        return new SidecarPostProcessorOutputDSLImpl(new JsonSidecarPostProcessorOutput());
    }
}
