package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;

@FunctionalInterface
public interface DataElementNormalizer<T> {
    T apply(T t, SidecarInvocationData sid);

    default DataElementNormalizer<T> andThen(DataElementNormalizer<T> next) {
        return (T t, SidecarInvocationData sid) -> next.apply((apply(t, sid)), sid);
    }
}
