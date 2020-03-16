package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.SidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import com.airfranceklm.amt.sidecar.stack.SidecarStack;

import java.io.IOException;
import java.util.Map;

/**
 * Processor services available to {@link SidecarStack} stacks, builders, etc.
 */
public interface ProcessorServices extends JsonIO {

    String stringify(Object obj) throws IOException;

    /**
     * Return "do-nothing" response for the specified response type
     * @param cls class
     * @param <T> parameter expected by the code
     * @return Instance corresponding to {@link SidecarPreProcessorOutput} or {@link SidecarPostProcessorOutput},
     * otherwise a {@link IllegalArgumentException} will be thrown.
     */
    <T> T doNothing(Class<T> cls);


    /**
     * Produces the Base64-encoded version of the
     * @param objIn input object
     * @return Base-64 rendition of the passed object.
     */
    String base64Stringify(Object objIn);

    Map<String,?> toMap(Object obj);
}
