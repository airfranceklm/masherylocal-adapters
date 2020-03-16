package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.SidecarInvocationData;

import java.util.Objects;

/**
 * Chainable functional interface for the extraction of the data that relies on the {@link SidecarInvocationData}.
 * @param <TIn> input object ot the extractor chain
 * @param <TOut> output object of the extractor chain.
 */
@FunctionalInterface
public interface ChainableExtractor<TIn, TOut> {

    TOut extract(TIn input, SidecarInvocationData data);

    /**
     * Chains another extractor at the end of this chain.
     * @param after extractor the be c
     * @param <V> type of the object returned after extraction chain.
     * @return
     */
    default <V> ChainableExtractor<TIn, V> andThen(ChainableExtractor<? super TOut, V> after) {
        Objects.requireNonNull(after);
        return (TIn v, SidecarInvocationData data) -> after.extract(extract(v, data), data);
    }
}
