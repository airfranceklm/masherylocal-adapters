package com.airfranceklm.amt.sidecar.config;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigSetting that = (ConfigSetting) o;
        return Objects.equals(token, that.token) &&
                required == that.required;
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, required);
    }
}
