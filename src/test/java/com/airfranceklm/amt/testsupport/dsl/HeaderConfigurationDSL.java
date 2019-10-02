package com.airfranceklm.amt.testsupport.dsl;

import java.util.Map;

public class HeaderConfigurationDSL {
    private Map<String,String> map;

    HeaderConfigurationDSL(Map<String, String> map) {
        this.map = map;
    }

    public HeaderConfigurationDSL header(String s, String value) {
        map.put(s, value);
        return this;
    };

    public HeaderConfigurationDSL drop(String s) {
        map.remove(s);
        return this;
    }
}
