package com.airfranceklm.amt.testsupport.dsl;

import com.airfranceklm.amt.testsupport.ApplicationData;
import com.airfranceklm.amt.testsupport.PackageKeyData;

import java.util.HashMap;

public class PackageKeyDSL {
    private PackageKeyData data;

    public PackageKeyDSL(PackageKeyData data) {
        this.data = data;
    }

    public PackageKeyDSL key(String value) {
        data.setPackageKey(value);
        return this;
    }

    public PackageKeyDSL withoutEAVs() {
        data.setPackageKeyEAVs(null);
        return this;
    }

    public PackageKeyDSL withEAV(String eav, String value) {
        if (data.getPackageKeyEAVs() == null) {
            data.setPackageKeyEAVs(new HashMap<>());
        }
        data.getPackageKeyEAVs().put(eav, value);
        return this;
    }

    public PackageKeyDSL dropEAV(String eav) {
        if (data.getPackageKeyEAVs() != null) {
            data.getPackageKeyEAVs().remove(eav);
        }
        return this;
    }

    public ApplicationDSL application(String appName) {
        ApplicationDSL retVal = new ApplicationDSL(data.getOrCreateApplication());
        retVal.name(appName);

        return retVal;
    }
}
