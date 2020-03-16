package com.airfranceklm.amt.sidecar.dsl;

public interface ChangeRouteDSL {
    ChangeRouteDSL toHost(String host);

    ChangeRouteDSL toFile(String file);

    ChangeRouteDSL toVerb(String verb);

    ChangeRouteDSL toURI(String uri);

    ChangeRouteDSL toPort(int port);
}
