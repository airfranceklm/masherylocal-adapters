package com.airfranceklm.amt.sidecar.elements;

public interface NormalizableDataElement<T> {
    void addNormalizer(DataElementNormalizer<T> norm);
}
