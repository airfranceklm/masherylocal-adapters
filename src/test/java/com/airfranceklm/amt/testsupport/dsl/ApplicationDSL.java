package com.airfranceklm.amt.testsupport.dsl;

import com.airfranceklm.amt.testsupport.ApplicationData;

import java.util.HashMap;

public class ApplicationDSL {
    private ApplicationData data;

    public ApplicationDSL(ApplicationData data) {
        this.data = data;
    }

    public ApplicationDSL name(String value) {
        data.setName(value);
        return this;
    }

    public ApplicationDSL withoutEAVs() {
        data.setExtendedAttributes(null);
        return this;
    }

    public ApplicationDSL withEAV(String eav, String value) {
        if (data.getExtendedAttributes() == null) {
            data.setExtendedAttributes(new HashMap<>());
        }
        data.getExtendedAttributes().put(eav, value);
        return this;
    }

    public ApplicationDSL dropEAV(String eav) {
        if (data.getExtendedAttributes() != null) {
            data.getExtendedAttributes().remove(eav);
        }
        return this;
    }
}
