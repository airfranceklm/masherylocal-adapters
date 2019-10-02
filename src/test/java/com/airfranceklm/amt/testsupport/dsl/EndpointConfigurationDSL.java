package com.airfranceklm.amt.testsupport.dsl;

import com.airfranceklm.amt.testsupport.EndpointData;

import java.util.HashMap;

public class EndpointConfigurationDSL {
    private EndpointData data;

    EndpointConfigurationDSL(EndpointData data) {
        this.data = data;
    }

    public EndpointConfigurationDSL identifyAs(String serviceId, String endpointId, String endpointName) {
        data.setServiceId(serviceId);
        data.setEndpointId(endpointId);
        data.setEndpointName(endpointName);
        return this;
    }

    public EndpointConfigurationDSL enpointUri(String uri) {
        data.setEndpointURI(uri);
        return this;
    }

    public MapConfigurationDSL preProcessor() {
        if (data.getPreProcessorConfiguration() == null) {
            data.setPreProcessorConfiguration(new HashMap<>());
        }
        return new MapConfigurationDSL(data.getPreProcessorConfiguration());
    }

    public MapConfigurationDSL postProcessor() {
        if (data.getPostProcessorConfiguration() == null) {
            data.setPostProcessorConfiguration(new HashMap<>());
        }
        return new MapConfigurationDSL(data.getPostProcessorConfiguration());
    }
}
