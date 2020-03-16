package com.airfranceklm.amt.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NestedBean {
    String value;

    @JsonProperty("nestedValue")
    public String getValue() {
        return value;
    }

    @YamlBinding(value = "nestedValue")
    public void setValue(String value) {
        this.value = value;
    }
}
