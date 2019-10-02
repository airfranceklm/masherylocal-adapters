package com.airfranceklm.amt.sidecar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SidecarOutputDSL {
    private SidecarOutputImpl data;

    public SidecarOutputDSL(SidecarOutputImpl data) {
        this.data = data;
    }

    public SidecarOutputDSL withCode(int code) {
        data.setCode(code);
        return this;
    }

    public SidecarOutputDSL addHeader(String h, String v) {
        if (data.getAddHeaders() == null) {
            data.setAddHeaders(new HashMap<>());
        }
        data.getAddHeaders().put(h, v);

        return this;
    }

    public SidecarOutputDSL dropHeaders(String... hv) {
        if (data.getDropHeaders() == null) {
            data.setDropHeaders(new ArrayList<>());
        }

        Collections.addAll(data.getDropHeaders(), hv);
        return this;
    }

    public SidecarOutputDSL withPayload(String payload) {
        data.setPayload(payload);
        return this;
    }

    public ChangeRoutingDSL changeRouting() {
        if (data.getChangeRoute() == null) {
            data.setChangeRoute(new SidecarOutputRouting());
        }
        return new ChangeRoutingDSL();
    }

    class ChangeRoutingDSL {
        ChangeRoutingDSL toHost(String host) {
            data.getChangeRoute().setHost(host);
            return this;
        }

        ChangeRoutingDSL toFile(String file) {
            data.getChangeRoute().setFile(file);
            return this;
        }

        ChangeRoutingDSL toVerb(String verb) {
            data.getChangeRoute().setHttpVerb(verb);
            return this;
        }

        ChangeRoutingDSL toURI(String uri) {
            data.getChangeRoute().setUri(uri);
            return this;
        }

        ChangeRoutingDSL toPort(int port) {
            data.getChangeRoute().setPort(port);
            return this;
        }
    }


}
