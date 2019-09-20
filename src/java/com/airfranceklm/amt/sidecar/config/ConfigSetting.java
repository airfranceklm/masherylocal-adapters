package com.airfranceklm.amt.sidecar.config;

public class ConfigSetting {
    private String token;
    private ConfigRequirement required;

    ConfigSetting(String token, ConfigRequirement required) {
        this.token = token;
        this.required = required;
    }

    public String getToken() {
        return token;
    }

    public ConfigRequirement getRequired() {
        return required;
    }
}
