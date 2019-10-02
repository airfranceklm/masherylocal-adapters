package com.airfranceklm.amt.testsupport;

import java.util.Map;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.*;

/**
 * Package key data.
 */
public class PackageKeyData extends RequestCaseDatum {
    String packageKey;
    Map<String,String> packageKeyEAVs;
    ApplicationData application;

    PackageKeyData() {
        super();
    }

    PackageKeyData(Map<String, Object> yaml) {
        super(yaml);
    }

    @Override
    void buildFromYAML(Map<String, Object> yaml) {
        super.buildFromYAML(yaml);

        forDefinedString(yaml, "package key", this::setPackageKey);
        forDefinedStringMap(yaml, "extended attributes", this::setPackageKeyEAVs);

        forDefinedObjectMap(yaml, "application", this::createApplicationFromYaml);
    }

    void copyFrom(PackageKeyData another) {
        this.packageKey = another.packageKey;
        this.application = another.application;
        this.packageKeyEAVs = another.packageKeyEAVs;
    }

    public void setPackageKey(String packageKey) {
        this.packageKey = packageKey;
    }

    public void setPackageKeyEAVs(Map<String, String> packageKeyEAVs) {
        this.packageKeyEAVs = packageKeyEAVs;
    }

    void createApplicationFromYaml(Map<String,Object> yaml) {
        this.application = new ApplicationData(yaml);
    }

    public ApplicationData getOrCreateApplication() {
        if (application == null) {
            application = new ApplicationData();
        }
        return application;
    }

    public Map<String, String> getPackageKeyEAVs() {
        return packageKeyEAVs;
    }
}
