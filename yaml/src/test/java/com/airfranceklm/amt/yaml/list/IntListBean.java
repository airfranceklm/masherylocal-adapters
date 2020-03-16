package com.airfranceklm.amt.yaml.list;

import com.airfranceklm.amt.yaml.YamlBinding;

import java.util.List;

public class IntListBean {
    private List<Integer> list;

    public List<Integer> getList() {
        return list;
    }

    @YamlBinding("intList")
    public void setList(List<Integer> list) {
        this.list = list;
    }
}
