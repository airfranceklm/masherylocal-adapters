package com.airfranceklm.amt.sidecar.config.afklyaml;

import com.airfranceklm.amt.sidecar.dsl.DslMethod;
import com.airfranceklm.amt.sidecar.dsl.SidecarInputDSL;
import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.SidecarDescriptor;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarInstance;
import com.airfranceklm.amt.yaml.YamlHelper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.airfranceklm.amt.sidecar.JsonHelper.toMap;
import static com.airfranceklm.amt.sidecar.model.SidecarInputPoint.PreProcessor;
import static com.airfranceklm.amt.sidecar.model.SidecarInputPoint.Preflight;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class YAMLEndpointConfiguration {

    @Getter @Setter
    private String serviceId;
    @Getter @Setter
    private String endpointId;

    @JsonProperty("pre-processor") @Getter @Setter
    private YAMLPreProcessorPointConfiguration preProcessor;
    @JsonProperty("post-processor") @Getter @Setter
    private YAMLPostProcessorPointConfiguration postProcessor;

    public YAMLEndpointConfiguration() {
    }

    public YAMLEndpointConfiguration(String serviceId, String endpointId) {
        this.serviceId = serviceId;
        this.endpointId = endpointId;
    }


    public void sync() {
        if (getPreProcessor() != null) {
            applyEndpointIdentification(getPreProcessor().configuredPreflight());
            applyEndpointIdentification(getPreProcessor().configuredSidecar());
        }

        if (getPostProcessor() != null) {
            applyEndpointIdentification(getPostProcessor().configuredSidecar());
        }
    }

    private void applyEndpointIdentification(SidecarConfiguration sidecar) {
        if (sidecar != null) {
            sidecar.setServiceId(this.serviceId);
            sidecar.setEndpointId(this.endpointId);
        }
    }

    public YAMLPreProcessorPointConfiguration allocOrGetPreProcessor() {
        if (this.preProcessor == null) {
            this.preProcessor = new YAMLPreProcessorPointConfiguration();
        }
        return this.preProcessor;
    }

    @DslMethod
    public YAMLEndpointConfiguration preProcess(Consumer<YAMLPreProcessorPointConfiguration> script) {
        script.accept(allocOrGetPreProcessor());
        return this;
    }

    @DslMethod
    public YAMLEndpointConfiguration postProcess(Consumer<YAMLPostProcessorSidecarConfiguration> script) {
        if (postProcessor == null) {
            this.postProcessor = new YAMLPostProcessorPointConfiguration();
        }

        if (postProcessor.getSidecar() == null) {
            this.postProcessor.setSidecar(new YAMLPostProcessorSidecarConfiguration());
        }

        script.accept(this.postProcessor.getSidecar());
        return this;
    }

    @DslMethod
    public YAMLInMemoryStoreEntry forInput(Consumer<SidecarInputDSL> c) {
        SidecarInput si = new SidecarInput();
        si.setServiceId(Objects.requireNonNull(getServiceId(), "Service ID must be set before creating in-memory entries"));
        si.setEndpointId(Objects.requireNonNull(getEndpointId(), "Endpoint Id must be set before creating in-memory entries"));
        si.setPoint(PreProcessor);

        c.accept(new SidecarInputDSL(si));

        YAMLInMemoryStoreEntry entry = new YAMLInMemoryStoreEntry();
        entry.setHash(si.getInputChecksum());

        if (this.preProcessor == null) {
            this.preProcessor = new YAMLPreProcessorPointConfiguration();
        }
        this.preProcessor.allocOrGetInMemory().add(entry);

        return entry;
    }

    @DslMethod
    public YAMLEndpointConfiguration deployAt(String serviceId, String endpointId) {
        setServiceId(serviceId);
        setEndpointId(endpointId);
        return this;
    }

    public String asYaml() {
        return YamlHelper.yamlStringOf(toMap(this));
    }

    public void saveTo(File f) throws IOException {
        YamlHelper.saveAsYaml(toMap(this), f);
    }

    public YAMLEndpointConfiguration preFlight(@NonNull SidecarDescriptor sd, @NonNull String env) {
        preFlight(sd, env, null);
        return this;
    }

    public YAMLEndpointConfiguration preFlight(@NonNull SidecarDescriptor sd, @NonNull String env, Map<String,Object> params) {
        SidecarInstance sidecarInstance = sd.instanceFor(env, Preflight, params);

        preProcess((preCfg) -> preCfg.preflight((preflightCfg) -> {
            preflightCfg.setEnabled(true);
            preflightCfg.absorb(sidecarInstance);
        }));

        return this;
    }

    public YAMLEndpointConfiguration preProcess(@NonNull SidecarDescriptor sd, @NonNull String env) {
       return preProcess(sd, env, null);
    }

    public YAMLEndpointConfiguration preProcess(@NonNull SidecarDescriptor sd, @NonNull String env, Map<String,Object> params) {
        SidecarInstance sidecarInstance = sd.instanceFor(env, PreProcessor, params);

        preProcess((preCfg) -> preCfg.sidecar((preflightCfg) -> preflightCfg.absorb(sidecarInstance)));

        return this;
    }

    public YAMLEndpointConfiguration postProcess(@NonNull SidecarDescriptor sd, @NonNull String env) {
        return postProcess(sd, env, null);
    }

    public YAMLEndpointConfiguration postProcess(@NonNull SidecarDescriptor sd, @NonNull String env, Map<String,Object> params) {
        SidecarInstance si = sd.instanceFor(env, PreProcessor, params);

        postProcess((postSidecar) -> {
            postSidecar.absorb(si);
        });

        return this;
    }
}
