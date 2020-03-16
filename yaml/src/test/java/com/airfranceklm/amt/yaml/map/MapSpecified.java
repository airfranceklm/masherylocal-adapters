package com.airfranceklm.amt.yaml.map;

import com.airfranceklm.amt.yaml.YamlBinding;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class MapSpecified {
    private Map<String,MapContainedBean> map;

    @JsonProperty("beans")
    public Map<String, MapContainedBean> getMap() {
        return map;
    }

    @YamlBinding("beans")
    public void setMap(Map<String, MapContainedBean> map) {
        this.map = map;
    }
}
