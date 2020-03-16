package com.airfranceklm.amt.sidecar.model.alcp;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Factory to create the definition of
 */
public class ALCPAlgorithmFactory {

    private Map<String, Function<ALCPAlgorithmSpec, ALCPAlgorithm<?,?>>> factories;

    public ALCPAlgorithmFactory() {
        this.factories = new HashMap<>();
    }

    public void add(String algRef, Function<ALCPAlgorithmSpec, ALCPAlgorithm<?,?>> producer) {
        Objects.requireNonNull(algRef);
        Objects.requireNonNull(producer);

        this.factories.put(algRef, producer);
    }

    public ALCPAlgorithm<?,?> create(ALCPAlgorithmSpec spec) {
        Function<ALCPAlgorithmSpec, ALCPAlgorithm<?,?>> factoryMethod = factories.get(spec.getAlgorithmName());
        if (factoryMethod != null) {
            return factoryMethod.apply(spec);
        } else {
            // Reference is made to a non-existing ALCP algorithm; will ignore this for now.
            return null;
        }
    }
}
