package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.dsl.SidecarPostProcessorOutputDSL;
import com.airfranceklm.amt.sidecar.dsl.SidecarPostProcessorOutputDSLImpl;
import com.airfranceklm.amt.sidecar.dsl.SidecarPreProcessorOutputDSL;
import com.airfranceklm.amt.sidecar.dsl.SidecarPreProcessorOutputDSLImpl;
import com.airfranceklm.amt.sidecar.model.SidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of processor services using the I/O
 */
public class JsonProcessorServices implements ProcessorServices {
    private static JsonSidecarPreProcessorOutput preOutput = new JsonSidecarPreProcessorOutput();
    private static JsonSidecarPostProcessorOutput postOutput = new JsonSidecarPostProcessorOutput();

    @Override
    public byte[] toTransportOptimizedJSON(Object obj) throws IOException {
        return JsonHelper.toTransportOptimizedJSON(obj);
    }

    @Override
    public <T> T readTransportOptimizedJSON(byte[] body, Class<T> clazz) throws IOException {
        return JsonHelper.readTransportOptimizedJSON(body, clazz);
    }

    @Override
    public String stringify(Object obj) throws IOException {
        return JsonHelper.toJSON(obj);
    }

    @Override @SuppressWarnings("unchecked")
    public <T> T doNothing(Class<T> cls) {
        Objects.requireNonNull(cls);

        if (SidecarPreProcessorOutput.class.isAssignableFrom(cls)) {
            return (T)preOutput;
        } else if (SidecarPostProcessorOutput.class.isAssignableFrom(cls)) {
            return (T)postOutput;
        } else {
            throw new IllegalArgumentException(String.format("Unsupported class: %s", cls.getName()));
        }
    }

    @Override
    public String base64Stringify(Object objIn) {
        return JsonHelper.toBase64JSON(objIn);
    }

    @Override
    public Map<String, ?> toMap(Object obj) {
        return JsonHelper.toMap(obj);
    }

    @Override @SuppressWarnings("unchecked")
    public <T> T readJson(String rawJson, Class<T> cls) throws IOException {
        if (SidecarPreProcessorOutput.class.isAssignableFrom(cls)) {
            return (T)JsonHelper.parse(rawJson, JsonSidecarPreProcessorOutput.class);
        } else if (SidecarPostProcessorOutput.class.isAssignableFrom(cls)) {
            return (T)JsonHelper.parse(rawJson, JsonSidecarPostProcessorOutput.class);
        } else {
            return JsonHelper.parse(rawJson, cls);
        }
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
