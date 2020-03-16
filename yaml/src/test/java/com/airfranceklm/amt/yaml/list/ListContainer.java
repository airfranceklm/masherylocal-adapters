package com.airfranceklm.amt.yaml.list;

import com.airfranceklm.amt.yaml.YamlBinding;
import com.airfranceklm.amt.yaml.YamlReadable;

import java.util.List;

@YamlReadable
public class ListContainer {
    private List<StringListBean> list;

    public List<StringListBean> getList() {
        return list;
    }

    @YamlBinding("objectList")
    public void setList(List<StringListBean> list) {
        this.list = list;
    }
}
