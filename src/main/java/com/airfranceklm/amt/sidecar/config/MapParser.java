package com.airfranceklm.amt.sidecar.config;

import java.util.Map;

@FunctionalInterface
public interface MapParser<T> {
    T accept(Map<String,Object> objectToParse);
}
