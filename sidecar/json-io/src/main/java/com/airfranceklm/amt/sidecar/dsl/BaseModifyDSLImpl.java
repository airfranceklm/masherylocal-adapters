package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.model.json.JsonModificationCommandImpl;

import java.util.Collections;

import static com.airfranceklm.amt.sidecar.model.json.JsonModificationCommandImpl.*;

class BaseModifyDSLImpl<T extends JsonModificationCommandImpl>
        extends PayloadCreatorDSLImpl<T>
        implements BaseCallModificationDSL {

    public BaseModifyDSLImpl(T target) {
        super(target);
    }

    @Override
    public BaseModifyDSLImpl<T> dropHeaders(String... s) {
        Collections.addAll(allocOrGetDropHeaders(target), s);
        return this;
    }

    @Override
    public BaseModifyDSLImpl<T> dropFragments(String... f) {
        Collections.addAll(allocOrGetDropFragments(target), f);
        return this;
    }

    @Override
    public BaseModifyDSLImpl<T> passFragment(String path, Object fragment) {
        allocOrGetPassFragments(target).put(path, fragment);
        return this;
    }
}
