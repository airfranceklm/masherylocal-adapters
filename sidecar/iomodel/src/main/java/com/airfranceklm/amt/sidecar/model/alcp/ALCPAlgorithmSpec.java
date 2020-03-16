package com.airfranceklm.amt.sidecar.model.alcp;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Application-level call protection protocol specification.
 */
public class ALCPAlgorithmSpec {
    @Getter
    private String algorithmName;
    @Getter
    private Map<String, Object> params;
    @Getter
    private AlgorithmActivation activation;

    public ALCPAlgorithmSpec(String algorithmName, Map<String, Object> params, AlgorithmActivation activation) {
        this.algorithmName = algorithmName;
        this.params = params;
        this.activation = activation;
    }

    public ALCPAlgorithmSpec(String algorithmName) {
        this();
        this.algorithmName = algorithmName;
    }

    public ALCPAlgorithmSpec() {
        this.activation = AlgorithmActivation.Bidirectional;
    }

    public ALCPAlgorithmSpec(Map<String, Object> params) {
        this();
        this.params = params;
    }

    public <T> T getParam(String p) {
        if (params != null) {
            return (T)params.get(p);
        } else {
            return null;
        }
    }

    public ALCPAlgorithmSpec activeFor(AlgorithmActivation actv) {
        this.activation = actv;
        return this;
    }

    public ALCPAlgorithmSpec addParam(String param, Object value) {
        if (params == null) {
            params = new HashMap<>();
        }

        params.put(param, value);
        return this;
    }
}
