package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;

@FunctionalInterface
public interface FilterMatchReporter<T> {
    void report(String label, T value, SidecarInvocationData sid);
}
