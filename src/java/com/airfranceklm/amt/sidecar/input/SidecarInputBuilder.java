package com.airfranceklm.amt.sidecar.input;

import com.airfranceklm.amt.sidecar.SidecarInput;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.filters.SidecarScopeFilterGroup;
import com.airfranceklm.amt.sidecar.filters.SidecarScopeFilteringResult;
import com.airfranceklm.amt.sidecar.stack.AFKLMSidecarStack;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

import java.io.IOException;
import java.util.Set;

/**
 * Interface for the sidecar builder.
 * @param <T>
 */
public interface SidecarInputBuilder<T extends ProcessorEvent> {
    AFKLMSidecarStack.AFKLMSidecarStackConfiguration getStackConfiguration();

    boolean requiresScopeFiltering();

    boolean requiresPreconditionInspection();

    Set<SidecarScopeFilterGroup<?>> getScopeFilters();

    Set<EventInspector<T>> getConditionAssertions();

    AFKLMSidecarStack getStack();

    boolean supportsIdempotentCalls(SidecarInput input);

    SidecarInvocationData build(T ppe) throws IOException;

    SidecarInvocationData build(T ppe, SidecarScopeFilteringResult filterResult) throws IOException;
}
