package com.airfranceklm.amt.testsupport;

import java.util.Map;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.forDefinedString;
import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.forDefinedStringMap;

public class ApplicationData extends RequestCaseDatum {
    String name;
    Map<String,String> extendedAttributes;

    ApplicationData() {
        super();
    }

    ApplicationData(Map<String, Object> yaml) {
        super(yaml);
    }

    @Override
    void buildFromYAML(Map<String, Object> yaml) {
        super.buildFromYAML(yaml);

        forDefinedString(yaml, "name", this::setName);
        forDefinedStringMap(yaml, "extended attributes", this::setExtendedAttributes);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExtendedAttributes(Map<String, String> extendedAttributes) {
        this.extendedAttributes = extendedAttributes;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getExtendedAttributes() {
        return extendedAttributes;
    }
}
