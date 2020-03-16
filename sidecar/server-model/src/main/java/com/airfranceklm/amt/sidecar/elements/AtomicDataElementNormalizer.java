package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;

@FunctionalInterface
public interface AtomicDataElementNormalizer<T> {
    T apply(T t);

    default DataElementNormalizer<T> normalize() {
        return (T t, SidecarInvocationData sid) -> apply(t);
    }
}
