package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.dsl.SidecarPostProcessorOutputDSL;
import com.airfranceklm.amt.sidecar.dsl.SidecarPreProcessorOutputDSL;
import com.airfranceklm.amt.sidecar.model.SidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * JSON Input/Output serializer/deserializer that decouples components from the concrete I/O objects implemented
 * by the adapter core part itself.
 */
public interface JsonIO {
    /**
     * Reads object that was serialized. The purpose of this method is to hide from the stack the need to know the concrete
     * implementations of {@link SidecarPreProcessorOutput} and {@link SidecarPostProcessorOutput}
     * (or other adapter-specific implementations) within the adapter.
     * @param rawJson JSON to be processed
     * @param cls class
     * @param <T> Type ot be returned
     * @return instance of the class, otherwise an I/O exception
     */
    <T> T readJson(String rawJson, Class<T> cls) throws IOException;

    /**
     * Converts an object to a transport-optimized JSON. Large string serializations are automatically
     * GZip-compressed. This method should be used in the context where the application is implementing network
     * protocol directly. In contexts where GZip is transparently applied by the network communication stack
     * (such as e.g. HTTP), the application of GZip compression will have no benefits.
     * @param obj object object to compress
     * @return bytes of the resulting compression.
     * @throws IOException if conversion is not possible.
     */
    byte[] toTransportOptimizedJSON(Object obj) throws IOException;

    /**
     * Deserializes object that was previously serialized with {@link #toTransportOptimizedJSON(Object)}.
     *
     * @param body bytes presenting the transport buffer
     * @param clazz expected return type
     * @param <T> Type of the expected class
     * @return instance of the object, if un-marshalling succeeds
     * @throws IOException if un-marshalling cannot be performed.
     */
    <T> T readTransportOptimizedJSON(byte[] body, Class<T> clazz) throws IOException;

    default SidecarPreProcessorOutput preProcessorOutput(Consumer<SidecarPreProcessorOutputDSL> c) {
        SidecarPreProcessorOutputDSL dsl = atPreProcessorPoint();
        c.accept(dsl);
        return dsl.output();
    }

    default SidecarPostProcessorOutput postProcessorOutput(Consumer<SidecarPostProcessorOutputDSL> c) {
        SidecarPostProcessorOutputDSL dsl = atPostProcessorPoint();
        c.accept(dsl);
        return dsl.output();
    }

    SidecarPreProcessorOutputDSL atPreProcessorPoint();
    SidecarPostProcessorOutputDSL atPostProcessorPoint();
}
