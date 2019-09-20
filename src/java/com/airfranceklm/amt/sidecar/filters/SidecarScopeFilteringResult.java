package com.airfranceklm.amt.sidecar.filters;

import java.util.HashMap;
import java.util.Map;

/**
 * Lambda filtering results. The purpose of this class is to ensure that the inclusion matching has effected
 * on the correct configuration parameter.
 */
public class SidecarScopeFilteringResult {
    private Map<String, Object> output = new HashMap<>();

    void add(String group, Object value) {
        output.put(group, value);
    }

    @SuppressWarnings(value = "unchecked")
    void add(String group, String label, Object filteredValue) {
        Map<String, Object> t = (Map<String,Object>)output.computeIfAbsent(group, k -> new HashMap<>());
        t.put(label, filteredValue);
    }

    public Map<String, Object> getFilteredParams() {
        return output;
    }
}
