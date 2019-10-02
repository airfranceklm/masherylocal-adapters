package com.airfranceklm.amt.testsupport.dsl;

import com.airfranceklm.amt.testsupport.RequestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.airfranceklm.amt.sidecar.AFKLMSidecarProcessor.formatCurtailedMessage;

/**
 * Capability to create the test cases programmatically in a clear sequence, using lambda syntax.
 * Using this class, the unit test can create a base scenario and then "extend" the scenario
 * by supplying the delta configuration, which makes it easier (at least, for the programmer)
 * to read.
 */
public abstract class DSL<T extends RequestCase> implements Cloneable {

    protected abstract T create();

    private List<Consumer<EndpointConfigurationDSL>> endpointDataConfigurers;
    private List<Consumer<APIClientRequestDSL>> apiClientRequestConfigurers;
    private List<Consumer<PackageKeyDSL>> packageKeyConfigurers;
    private List<Consumer<AuthorizationContextDataDSL>> authorizationContextConfigurers;
    private List<Consumer<APIOriginRequestDSL>> apiOriginRequestConfigurers;
    private List<Consumer<APIOriginResponseDSL>> apiOriginResponseConfigurers;
    private List<Consumer<TrafficManagerResponseDSL>> trafficManagerResponseConfigurers;

    public DSL() {
    }

    public T build() {
        T retVal = create();
        if (endpointDataConfigurers != null) {
            EndpointConfigurationDSL dsl = new EndpointConfigurationDSL(retVal.getOrCreateEndpointData());
            endpointDataConfigurers.forEach(c -> c.accept(dsl));
        }

        if (apiClientRequestConfigurers != null) {
            APIClientRequestDSL dsl = new APIClientRequestDSL(retVal.getOrCreateAPIClientRequestData());
            apiClientRequestConfigurers.forEach(c -> c.accept(dsl));
        }

        if (packageKeyConfigurers != null) {
            PackageKeyDSL dsl = new PackageKeyDSL(retVal.getOrCreatePackageKeyData());
            packageKeyConfigurers.forEach(c -> c.accept(dsl));
        }

        if (authorizationContextConfigurers != null) {
            AuthorizationContextDataDSL dsl = new AuthorizationContextDataDSL(retVal.getOrCreateAuthorizationContextData());
            authorizationContextConfigurers.forEach(c -> c.accept(dsl));
        }

        if (apiOriginRequestConfigurers != null) {
            APIOriginRequestDSL dsl = new APIOriginRequestDSL(retVal.getOrCreateAPIOriginRequestData());
            apiOriginRequestConfigurers.forEach(c -> c.accept(dsl));
        }

        if (apiOriginResponseConfigurers != null) {
            APIOriginResponseDSL dsl = new APIOriginResponseDSL(retVal.getOrCreateAPIOriginResponseData());
            apiOriginResponseConfigurers.forEach(c -> c.accept(dsl));
        }

        if (trafficManagerResponseConfigurers != null) {
            TrafficManagerResponseDSL dsl = new TrafficManagerResponseDSL(retVal.getOrCreateTrafficManagerResponseData());
            trafficManagerResponseConfigurers.forEach(c -> c.accept(dsl));
        }

        return retVal;
    }


    /**
     * Copies the data of the lambdas to another one.
     *
     * @param other another DSL instance to receive the values.
     */
    protected void copy(DSL<T> other) {
        if (this.endpointDataConfigurers != null) {
            other.endpointDataConfigurers = new ArrayList<>();
            other.endpointDataConfigurers.addAll(this.endpointDataConfigurers);
        }

        if (this.apiClientRequestConfigurers != null) {
            other.apiClientRequestConfigurers = new ArrayList<>();
            other.apiClientRequestConfigurers.addAll(this.apiClientRequestConfigurers);
        }

        if (this.packageKeyConfigurers != null) {
            other.packageKeyConfigurers = new ArrayList<>();
            other.packageKeyConfigurers.addAll(this.packageKeyConfigurers);
        }

        if (this.authorizationContextConfigurers != null) {
            other.authorizationContextConfigurers = new ArrayList<>();
            other.authorizationContextConfigurers.addAll(this.authorizationContextConfigurers);
        }

        if (this.apiOriginRequestConfigurers != null) {
            other.apiOriginRequestConfigurers = new ArrayList<>();
            other.apiOriginRequestConfigurers.addAll(this.apiOriginRequestConfigurers);
        }

        if (this.apiOriginResponseConfigurers != null) {
            other.apiOriginResponseConfigurers = new ArrayList<>();
            other.apiOriginResponseConfigurers.addAll(this.apiOriginResponseConfigurers);
        }

        if (this.trafficManagerResponseConfigurers != null) {
            other.trafficManagerResponseConfigurers = new ArrayList<>();
            other.trafficManagerResponseConfigurers.addAll(this.trafficManagerResponseConfigurers);
        }
    }

    //--------------------------------------------------------------------------
    // Configure endpoint

    public void configureEndpointData(Consumer<EndpointConfigurationDSL> c) {
        if (endpointDataConfigurers == null) {
            endpointDataConfigurers = new ArrayList<>();
        }
        endpointDataConfigurers.add(c);
    }

    public void configureAPIClientRequest(Consumer<APIClientRequestDSL> c) {
        if (apiClientRequestConfigurers == null) {
            apiClientRequestConfigurers = new ArrayList<>();
        }
        apiClientRequestConfigurers.add(c);
    }

    public void configurePackageKey(Consumer<PackageKeyDSL> c) {
        if (packageKeyConfigurers == null) {
            packageKeyConfigurers = new ArrayList<>();
        }
        packageKeyConfigurers.add(c);
    }

    public void configureAuthorizationContext(Consumer<AuthorizationContextDataDSL> c) {
        if (authorizationContextConfigurers == null) {
            authorizationContextConfigurers = new ArrayList<>();
        }
        authorizationContextConfigurers.add(c);
    }

    public void configureAPIOriginRequest(Consumer<APIOriginRequestDSL> c) {
        if (apiOriginRequestConfigurers == null) {
            apiOriginRequestConfigurers = new ArrayList<>();
        }
        apiOriginRequestConfigurers.add(c);
    }

    public void configureAPIOriginResponse(Consumer<APIOriginResponseDSL> c) {
        if (apiOriginResponseConfigurers == null) {
            apiOriginResponseConfigurers = new ArrayList<>();
        }
        apiOriginResponseConfigurers.add(c);
    }

    public void configureTrafficManager(Consumer<TrafficManagerResponseDSL> c) {
        if (trafficManagerResponseConfigurers == null) {
            trafficManagerResponseConfigurers = new ArrayList<>();
        }
        trafficManagerResponseConfigurers.add(c);
    }

    /**
     * The pre-processing should curtail with specified code and message
     *
     * @param code    code to use
     * @param message message to be rendered to the user.
     */
    public void expectCurtailingWith(int code, String message) {
        configureTrafficManager(cl -> {
            cl.expectStatusCode(code)
                    .expectHeader("content-type", "application/xml")
                    .expectResponsePayload(formatCurtailedMessage(message))
                    .expectCompleted(true);
        });
    }


}
