package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.impl.model.RequestModificationCommandImpl;
import com.airfranceklm.amt.sidecar.impl.model.RequestRoutingChangeBeanImpl;
import com.airfranceklm.amt.sidecar.impl.model.SidecarOutputTerminationCommandImpl;
import com.airfranceklm.amt.sidecar.impl.model.SidecarPreProcessorOutputImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Consumer;

public class SidecarPreProcessorDSL {
    private SidecarPreProcessorOutputImpl data;

    public SidecarPreProcessorDSL(SidecarPreProcessorOutputImpl data) {
        this.data = data;
    }


    public SidecarPreProcessorDSL addHeader(String h, String v) {
        if (getOrCreateModify().getAddHeaders() == null) {
            data.getModify().setAddHeaders(new HashMap<>());
        }
        data.getModify().getAddHeaders().put(h, v);

        return this;
    }

    public SidecarPreProcessorDSL dropHeaders(String... hv) {
        if (getOrCreateModify().getDropHeaders() == null) {
            data.getModify().setDropHeaders(new ArrayList<>());
        }

        Collections.addAll(data.getModify().getDropHeaders(), hv);
        return this;
    }

    public SidecarPreProcessorDSL withPayload(String payload) {
        getOrCreateModify().setPayload(payload);
        return this;
    }

    public ChangeRoutingDSL changeRouting() {
        if (getOrCreateModify().getChangeRoute() == null) {
            data.getModify().setChangeRoute(new RequestRoutingChangeBeanImpl());
        }
        return new ChangeRoutingDSL();
    }

    public SidecarPreProcessorDSL terminate(Consumer<TerminateDLS> c) {
        c.accept(new TerminateDLS());
        return this;
    }

    public TerminateDLS terminate() {
        return new TerminateDLS();
    }

    protected RequestModificationCommandImpl getOrCreateModify() {
        if (data.getModify() == null) {
            data.setModify(new RequestModificationCommandImpl());
        }
        return data.getModify();
    }
    protected SidecarOutputTerminationCommandImpl getOrCreateTerminate() {
        if (data.getTerminate() == null) {
            data.setTerminate(new SidecarOutputTerminationCommandImpl());
        }
        return data.getTerminate();
    }

    class ChangeRoutingDSL {
        ChangeRoutingDSL toHost(String host) {
            data.getModify().getChangeRoute().setHost(host);
            return this;
        }

        ChangeRoutingDSL toFile(String file) {
            data.getModify().getChangeRoute().setFile(file);
            return this;
        }

        ChangeRoutingDSL toVerb(String verb) {
            data.getModify().getChangeRoute().setHttpVerb(verb);
            return this;
        }

        ChangeRoutingDSL toURI(String uri) {
            data.getModify().getChangeRoute().setUri(uri);
            return this;
        }

        ChangeRoutingDSL toPort(int port) {
            data.getModify().getChangeRoute().setPort(port);
            return this;
        }
    }

    class TerminateDLS {
        TerminateDLS withCode(int code) {
            getOrCreateTerminate().setCode(code);
            return this;
        }

        TerminateDLS withMessage(String msg) {
            getOrCreateTerminate().setMessage(msg);
            return this;
        }
    }


}
