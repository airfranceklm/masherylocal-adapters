package com.airfranceklm.amt.testsupport.mocks;

import com.mashery.http.ParamGroup;
import com.mashery.http.ParamIterator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ParamGroupImpl implements ParamGroup {

    private Map<String,String> contents;

    public ParamGroupImpl(Map<String, String> contents) {
        this.contents = contents != null ? contents : new HashMap<>();
    }

    @Override
    public boolean contains(String s) {
        return contents.containsKey(s);
    }

    @Override
    public String get(String s) {
        return contents.get(s);
    }

    @Override
    public ParamIterator iterator() {
        return null;
    }

}
