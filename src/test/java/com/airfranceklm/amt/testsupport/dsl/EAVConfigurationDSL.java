package com.airfranceklm.amt.testsupport.dsl;

import java.util.Map;

public class EAVConfigurationDSL {
    private Map<String,String> map;

    EAVConfigurationDSL(Map<String, String> map) {
        this.map = map;
    }

    public EAVConfigurationDSL eav(String s, String value) {
        map.put(s, value);
        return this;
    };

    public EAVConfigurationDSL drop(String s) {
        map.remove(s);
        return this;
    }
}
