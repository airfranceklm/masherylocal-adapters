package com.airfranceklm.amt.testsupport;

import java.util.Map;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.forDefinedString;
import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.forDefinedStringMap;

public class EndpointData {
    String serviceId;
    String endpointId;
    String endpointName;
    Map<String,String> preProcessorConfiguration;
    Map<String,String> postProcessorConfiguration;
    String endpointURI;

    EndpointData() {
    }

    EndpointData(Map<String,Object> yaml) {
        this();
        buildFromYAML(yaml);
    }


    public void addPreProcessorConfiguration(Map<String,String> cfg) {
        preProcessorConfiguration.putAll(cfg);
    }

    void buildFromYAML(Map<String,Object> yaml) {
        forDefinedString(yaml, "service id", this::setServiceId);
        forDefinedString(yaml, "endpoint id", this::setEndpointId);
        forDefinedString(yaml, "endpoint name", this::setEndpointName);
        forDefinedString(yaml, "endpoint uri", this::setEndpointURI);

        forDefinedStringMap(yaml,  "pre-processor configuration", this::setPreProcessorConfiguration);
        forDefinedStringMap(yaml,  "post-processor configuration", this::setPostProcessorConfiguration);

    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public String getEndpointURI() {
        return endpointURI;
    }

    public void setPreProcessorConfiguration(Map<String, String> preProcessorConfiguration) {
        this.preProcessorConfiguration = preProcessorConfiguration;
    }

    public void setPostProcessorConfiguration(Map<String, String> postProcessorConfiguration) {
        this.postProcessorConfiguration = postProcessorConfiguration;
    }

    public Map<String, String> getPreProcessorConfiguration() {
        return preProcessorConfiguration;
    }

    public Map<String, String> getPostProcessorConfiguration() {
        return postProcessorConfiguration;
    }

    public void setEndpointURI(String endpointURI) {
        this.endpointURI = endpointURI;
    }


}
