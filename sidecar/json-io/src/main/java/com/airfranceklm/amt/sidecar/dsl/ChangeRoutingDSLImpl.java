package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.model.json.JsonRequestRoutingChangeBean;

class ChangeRoutingDSLImpl implements ChangeRouteDSL {

    private JsonRequestRoutingChangeBean bean;

    public ChangeRoutingDSLImpl(JsonRequestRoutingChangeBean bean) {
        this.bean = bean;
    }

    @Override
    public ChangeRoutingDSLImpl toHost(String host) {
        bean.setHost(host);
        return this;
    }

    @Override
    public ChangeRoutingDSLImpl toFile(String file) {
        bean.setFile(file);
        return this;
    }

    @Override
    public ChangeRoutingDSLImpl toVerb(String verb) {
        bean.setHttpVerb(verb);
        return this;
    }

    @Override
    public ChangeRoutingDSLImpl toURI(String uri) {
        bean.setUri(uri);
        return this;
    }

    @Override
    public ChangeRoutingDSLImpl toPort(int port) {
        bean.setPort(port);
        return this;
    }
}
