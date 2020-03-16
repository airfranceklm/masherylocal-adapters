package com.airfranceklm.amt.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Map;

public class TestBean {
    private String strValue;

    private boolean primitiveBoolean;
    private Boolean objectBoolean;

    private int primitiveInteger;
    private Integer objectInteger;

    private Map<String,String> stringMap;
    private Map<String,Object> objectMap;

    private String stringFromContext;

    private Date gmtTime;

    private NestedBean nestedBean;

    @JsonProperty("string")
    public String getStrValue() {
        return strValue;
    }

    @YamlBinding(value = "string")
    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    @JsonProperty("b1")
    public boolean isPrimitiveBoolean() {
        return primitiveBoolean;
    }

    @YamlBinding(value = "b1")
    public void setPrimitiveBoolean(boolean primitiveBoolean) {
        this.primitiveBoolean = primitiveBoolean;
    }

    @JsonProperty("b2")
    public Boolean getObjectBoolean() {
        return objectBoolean;
    }

    @YamlBinding(value = "b2")
    public void setObjectBoolean(Boolean objectBoolean) {
        this.objectBoolean = objectBoolean;
    }

    @JsonProperty("i1")
    public int getPrimitiveInteger() {
        return primitiveInteger;
    }

    @YamlBinding(value = "i1")
    public void setPrimitiveInteger(int primitiveInteger) {
        this.primitiveInteger = primitiveInteger;
    }

    @JsonProperty("i2")
    public Integer getObjectInteger() {
        return objectInteger;
    }

    @YamlBinding(value = "i2")
    public void setObjectInteger(Integer objectInteger) {
        this.objectInteger = objectInteger;
    }

    @JsonProperty("stringMap")
    public Map<String, String> getStringMap() {
        return stringMap;
    }

    @YamlBinding(value = "stringMap", collectionType = String.class)
    public void setStringMap(Map<String, String> stringMap) {
        this.stringMap = stringMap;
    }

    @JsonProperty("objectMap")
    public Map<String, Object> getObjectMap() {
        return objectMap;
    }

    @YamlBinding(value = "objectMap")
    public void setObjectMap(Map<String, Object> objectMap) {
        this.objectMap = objectMap;
    }

    @JsonProperty("strValue")
    public String getStringFromContext() {
        return stringFromContext;
    }

    @YamlBinding(value = "strValue", context = "testContext")
    public void setStringFromContext(String stringFromContext) {
        this.stringFromContext = stringFromContext;
    }


    @JsonProperty("gmtDate")
    public Date getGmtTime() {
        return gmtTime;
    }

    @YamlBinding(value = "gmtDate")
    public void setGmtTime(Date gmtTime) {
        this.gmtTime = gmtTime;
    }

    @JsonProperty("nestedBean")
    public NestedBean getNestedBean() {
        return nestedBean;
    }

    @YamlBinding(value = "nestedBean")
    public void setNestedBean(NestedBean nestedBean) {
        this.nestedBean = nestedBean;
    }
}
