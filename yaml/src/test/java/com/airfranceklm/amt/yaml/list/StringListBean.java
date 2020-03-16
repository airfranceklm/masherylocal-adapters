package com.airfranceklm.amt.yaml.list;

import com.airfranceklm.amt.yaml.YamlBinding;
import com.airfranceklm.amt.yaml.YamlReadable;

import java.util.List;

@YamlReadable
public class StringListBean {
    private List<String> coll;

    public List<String> getColl() {
        return coll;
    }

    @YamlBinding("stringList")
    public void setColl(List<String> coll) {
        this.coll = coll;
    }
}
