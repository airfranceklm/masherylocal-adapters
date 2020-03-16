package com.airfranceklm.amt.sidecar.model;

import java.io.Serializable;
import java.util.Map;

public interface SidecarPreProcessorOutput extends SidecarOutput<RequestModificationCommand> {

    Map<String, ?> getRelayParams();

    ReplyCommand getReply();

    Serializable createSerializableRelayParameters();

    boolean relaysMessageToPostprocessor();

    /**
     * Converts this object to the serializable form that can be loaded from the supplying stack.
     * @return instance that is suitable for caching.
     */
    SidecarPreProcessorOutput toSerializableForm();
}
