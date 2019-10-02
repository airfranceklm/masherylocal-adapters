package com.airfranceklm.amt.testsupport.dsl;

import java.util.Map;

public class MapConfigurationDSL {
    private Map<String,String> map;

    MapConfigurationDSL(Map<String, String> map) {
        this.map = map;
    }

    public MapConfigurationDSL param(String s, String value) {
        map.put(s, value);
        return this;
    };

    public MapConfigurationDSL drop(String s) {
        map.remove(s);
        return this;
    }
}
