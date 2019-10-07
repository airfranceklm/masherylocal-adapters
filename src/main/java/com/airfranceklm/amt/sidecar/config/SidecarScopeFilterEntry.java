package com.airfranceklm.amt.sidecar.config;

import java.util.Objects;

public class SidecarScopeFilterEntry {
    private boolean inclusive = true;
    private String group;
    private String param;
    private String label;
    private String value;

    public SidecarScopeFilterEntry(String group, String param, boolean inclusive) {
        this(group, param, null, inclusive);
    }

    public SidecarScopeFilterEntry(String group, String param, String value, boolean inclusive) {
        this(group, param, null, value, inclusive);
    }

    public SidecarScopeFilterEntry(String group, String param, String label, String value, boolean inclusive) {
        this.inclusive = inclusive;
        this.group = group;
        this.param = param;
        this.label = label;
        this.value = value;
    }

    public boolean isInclusive() {
        return inclusive;
    }

    public String getGroup() {
        return group;
    }

    public String getParam() {
        return param;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SidecarScopeFilterEntry that = (SidecarScopeFilterEntry) o;
        return inclusive == that.inclusive &&
                Objects.equals(group, that.group) &&
                Objects.equals(param, that.param) &&
                Objects.equals(label, that.label) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inclusive, group, param, label, value);
    }
}
