package com.airfranceklm.amt.testsupport;

import java.util.Map;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.forDefinedString;

class RequestCaseDatum {
    String asIn;

    RequestCaseDatum() {}

    RequestCaseDatum(Map<String,Object> yaml) {
        buildFromYAML(yaml);
    }

    boolean needsCopyFromAnotherCase() {
        return asIn != null;
    }

    void buildFromYAML(Map<String,Object> yaml) {
        forDefinedString(yaml, "as in", this::setAsIn);
    }

    String getCaseToCopyFrom() {
        return asIn;
    }

    void setAsIn(String asIn) {
        this.asIn = asIn;
    }
}
