package com.airfranceklm.amt.sidecar.dsl;

public interface BaseCallModificationDSL extends PayloadCreatorDSL {

    BaseCallModificationDSL dropHeaders(String... s);

    BaseCallModificationDSL dropFragments(String... f);

    BaseCallModificationDSL passFragment(String path, Object fragment);
}
