package com.airfranceklm.amt.yaml.map;

import com.airfranceklm.amt.yaml.YamlReceiver;

import java.util.Map;

public class MapContainer {
    private Map<String, MapContainedBean> contents;

    public Map<String, MapContainedBean> getContents() {
        return contents;
    }

    @YamlReceiver
    public void setContents(Map<String, MapContainedBean> contents) {
        this.contents = contents;
    }
}
