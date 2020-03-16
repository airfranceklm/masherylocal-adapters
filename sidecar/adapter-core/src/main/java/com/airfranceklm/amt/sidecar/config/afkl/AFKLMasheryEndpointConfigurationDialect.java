package com.airfranceklm.amt.sidecar.config.afkl;

import com.airfranceklm.amt.sidecar.MasheryConfigurationReader;
import com.airfranceklm.amt.sidecar.MasheryEndpointConfigurationDialect;
import com.airfranceklm.amt.sidecar.model.*;
import lombok.NonNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Air France/KLM-specific Mashery configuration dialect.
 */
public class AFKLMasheryEndpointConfigurationDialect implements MasheryEndpointConfigurationDialect {
    @Override
    public MasheryConfigurationReader<PreFlightSidecarConfiguration> getPreflightReader() {
        return new AFKLPreflight();
    }

    @Override
    public MasheryConfigurationReader<PreProcessorSidecarConfiguration> getPreProcessorReader() {
        return new AFKLPreProcessor();
    }

    @Override
    public MasheryConfigurationReader<PostProcessorSidecarConfiguration> getPostProcessorReader() {
        return new AFKLPostProcessor();
    }

    @Override
    public String getName() {
        return "Air France/KLM Mashery Configuration Dialect";
    }

    @Override
    public Map<String, String> preProcessorConfig(@NonNull SidecarDescriptor desc, @NonNull String env, Map<String, Object> preFlightParams, Map<String, Object> sidecarParams) {
        Map<String,String> retVal = new LinkedHashMap<>();

        if (desc.deploysAt(SidecarInputPoint.Preflight)) {
            SidecarInstance si = desc.instanceFor(env, SidecarInputPoint.Preflight, preFlightParams);
            retVal.putAll(new AFKLPreflight().write(si, SidecarInputPoint.Preflight));
        }

        SidecarInstance si = desc.instanceFor(env, SidecarInputPoint.PreProcessor, sidecarParams);
        retVal.putAll(new AFKLPreProcessor().write(si, SidecarInputPoint.PreProcessor));
        return retVal;
    }

    @Override
    public Map<String, String> postProcessorConfig(SidecarDescriptor desc, String env, Map<String, Object> params) {
        SidecarInstance si = desc.instanceFor(env, SidecarInputPoint.PostProcessor, params);
        return new AFKLPostProcessor().write(si, SidecarInputPoint.PostProcessor);
    }
}
