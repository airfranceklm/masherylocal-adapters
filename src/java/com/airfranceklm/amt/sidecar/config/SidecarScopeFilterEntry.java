package com.airfranceklm.amt.sidecar.config;

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
}
