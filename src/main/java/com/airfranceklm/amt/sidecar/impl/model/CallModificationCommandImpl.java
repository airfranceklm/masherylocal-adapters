package com.airfranceklm.amt.sidecar.impl.model;

import com.airfranceklm.amt.sidecar.model.CallModificationCommand;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CallModificationCommandImpl extends PayloadCarrierImpl implements CallModificationCommand {

    private TreeMap<String,String> addHeaders;
    private List<String> dropHeaders;

    /**
     * Checks the type safety of the loaded maps.
     * @throws IOException if the classes will not be compatible with the annotations of the enclosing maps.
     */
    public void checkTypeSafety() throws IOException {
        if (addHeaders != null) {
            for (Map.Entry<?, ?> entry : addHeaders.entrySet()) {
                if (entry.getValue() != null && !(entry.getValue() instanceof String)) {
                    throw new IOException(String.format("AddHeader Value for key %s (%s) is not String, but %s.",
                            entry.getKey(), entry.getValue(), entry.getValue().getClass().getName()));
                }
            }
        }

        if (dropHeaders != null) {
            for (Object entry : dropHeaders) {
                if (entry != null && !(entry instanceof String)) {
                    throw new IOException(String.format("DropHeaders Value %s is not String, but %s.",
                            entry, entry.getClass().getName()));
                }
            }
        }
    }

    @Override
    public boolean addsContentType() {
        return addHeaders != null && addHeaders.containsKey("content-type");
    }

    public Map<String, String> getAddHeaders() {
        return addHeaders;
    }

    public void setAddHeaders(Map<String, String> addHeaders) {
        if (addHeaders != null) {
            getOrCreateAddHeaders().putAll(addHeaders);
        }
    }

    @JsonIgnore
    public Map<String,String> getOrCreateAddHeaders() {
        if (this.addHeaders == null) {
            this.addHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        }
        return this.addHeaders;
    }

    @Override
    public List<String> getDropHeaders() {
        return dropHeaders;
    }

    public void setDropHeaders(List<String> dropHeaders) {
        this.dropHeaders = dropHeaders;
    }

    @Override
    protected boolean containsOnlyNulls() {
        return super.containsOnlyNulls() &&
                (dropHeaders == null || dropHeaders.size() == 0) &&
                (addHeaders == null || addHeaders.size() == 0);
    }
}
