package com.airfranceklm.amt.sidecar.config.afklyaml;

import com.airfranceklm.amt.sidecar.dsl.DslMethod;
import com.airfranceklm.amt.sidecar.dsl.SidecarPreProcessorOutputDSL;
import com.airfranceklm.amt.sidecar.dsl.SidecarPreProcessorOutputDSLImpl;
import com.airfranceklm.amt.sidecar.model.PreFlightSidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.PreProcessorSidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class YAMLPreProcessorPointConfiguration {

    @Getter @Setter @JsonProperty("in-memory store")
    private List<YAMLInMemoryStoreEntry> inMemory;
    @Getter @Setter
    private YAMLPreflightSidecarConfiguration preflight;
    @Getter @Setter
    private YAMLPreProcessorSidecarConfiguration sidecar;

    @Getter @Setter
    private JsonSidecarPreProcessorOutput staticModification;


    public List<YAMLInMemoryStoreEntry> allocOrGetInMemory() {
        if (inMemory == null) {
            inMemory = new ArrayList<>();
        }
        return inMemory;
    }

    public PreProcessorSidecarConfiguration configuredSidecar() {
        return sidecar == null ? null : sidecar.retrieve();
    }

    public PreFlightSidecarConfiguration configuredPreflight() {
        return preflight == null ? null : preflight.retrieve();
    }

    @DslMethod
    public YAMLPreProcessorPointConfiguration preflight(Consumer<YAMLPreflightSidecarConfiguration> script) {
        if (this.preflight == null) {
            this.preflight = new YAMLPreflightSidecarConfiguration();
        }

        script.accept(this.preflight);
        return this;
    }

    public YAMLPreProcessorSidecarConfiguration allocOrGetSidecar() {
        if (this.sidecar == null) {
            this.sidecar = new YAMLPreProcessorSidecarConfiguration();
        }
        return this.sidecar;
    }

    @DslMethod
    public YAMLPreProcessorPointConfiguration sidecar(Consumer<YAMLPreProcessorSidecarConfiguration> script) {
        script.accept(allocOrGetSidecar());
        return this;
    }

    @DslMethod
    public YAMLPreProcessorPointConfiguration staticModification(Consumer<SidecarPreProcessorOutputDSL> script) {
        this.staticModification = new JsonSidecarPreProcessorOutput();
        script.accept(new SidecarPreProcessorOutputDSLImpl(this.staticModification));
        return this;
    }
}
