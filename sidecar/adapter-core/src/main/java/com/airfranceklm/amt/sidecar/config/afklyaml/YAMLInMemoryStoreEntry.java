package com.airfranceklm.amt.sidecar.config.afklyaml;

import com.airfranceklm.amt.sidecar.dsl.SidecarPreProcessorOutputDSL;
import com.airfranceklm.amt.sidecar.dsl.SidecarPreProcessorOutputDSLImpl;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

public class YAMLInMemoryStoreEntry {

    @Getter @Setter
    private String hash;
    @Getter @Setter
    private JsonSidecarPreProcessorOutput output;

    public void replyWith(Consumer<SidecarPreProcessorOutputDSL> c) {
        this.output = new JsonSidecarPreProcessorOutput();
        c.accept(new SidecarPreProcessorOutputDSLImpl(this.output));
    }
}
