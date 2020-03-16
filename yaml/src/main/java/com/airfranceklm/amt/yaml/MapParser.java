package com.airfranceklm.amt.yaml;

import java.util.Map;

@FunctionalInterface
public interface MapParser<T> {
    T accept(Map<String,Object> objectToParse);
}
