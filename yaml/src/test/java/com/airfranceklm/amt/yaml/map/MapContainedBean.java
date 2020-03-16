package com.airfranceklm.amt.yaml.map;

import com.airfranceklm.amt.yaml.YamlBinding;
import com.airfranceklm.amt.yaml.YamlReadable;
import com.fasterxml.jackson.annotation.JsonProperty;

@YamlReadable
public class MapContainedBean {
    String strValue;
    int intValue;


    @JsonProperty("str")
    public String getStrValue() {
        return strValue;
    }

    @YamlBinding("str")
    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    @JsonProperty("int")
    public int getIntValue() {
        return intValue;
    }

    @YamlBinding("int")
    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }
}
