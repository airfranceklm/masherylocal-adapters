package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.testsupport.dsl.APIOriginResponseDSL;
import com.airfranceklm.amt.testsupport.dsl.DSL;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SidecarTestDSL extends DSL<SidecarRequestCase> {

    private List<Consumer<SidecarInputDSL>> inputConfigurers;
    private List<Consumer<SidecarOutputDSL>> outputConfigurers;

    private List<Consumer<SidecarInputDSL>> preflightInputConfigurers;
    private List<Consumer<SidecarOutputDSL>> preflightOutputConfigurers;

    private String throwPreflightException;
    private String throwSidecarException;

    @Override
    protected SidecarRequestCase create() {
        return new SidecarRequestCase();
    }

    public void copy(SidecarTestDSL other) {
        super.copy(other);

        if (inputConfigurers != null) {
            other.inputConfigurers = new ArrayList<>();
            other.inputConfigurers.addAll(inputConfigurers);
        }

        if (outputConfigurers != null) {
            other.outputConfigurers = new ArrayList<>();
            other.outputConfigurers.addAll(outputConfigurers);
        }

        if (preflightInputConfigurers != null) {
            other.preflightInputConfigurers = new ArrayList<>();
            other.preflightInputConfigurers.addAll(preflightInputConfigurers);
        }

        if (preflightOutputConfigurers != null) {
            other.preflightOutputConfigurers = new ArrayList<>();
            other.preflightOutputConfigurers.addAll(preflightOutputConfigurers);
        }

        other.throwSidecarException = this.throwSidecarException;
        other.throwPreflightException = this.throwPreflightException;
    }

    @Override
    public SidecarRequestCase build() {
        final SidecarRequestCase retVal = super.build();

        retVal.preflightException = this.throwPreflightException;
        retVal.sidecarException = this.throwSidecarException;

        // ------------------------------------------------------
        // Sidecar input

        if (inputConfigurers != null) {
            SidecarInputDSL dsl = new SidecarInputDSL(retVal.getOrCreateSidecarInput());
            inputConfigurers.forEach(c -> c.accept(dsl));
        }

        if (outputConfigurers != null) {
            SidecarOutputDSL dsl = new SidecarOutputDSL(retVal.getOrCreateSidecarOutput());
            outputConfigurers.forEach(c -> c.accept(dsl));
        }

        // ------------------------------------------------------------
        // Pre-flight input

        if (preflightInputConfigurers != null) {
            SidecarInputDSL dsl = new SidecarInputDSL(retVal.getOrCreatePreflightInput());
            preflightInputConfigurers.forEach(c -> c.accept(dsl));
        }

        if (preflightOutputConfigurers != null) {
            SidecarOutputDSL dsl = new SidecarOutputDSL(retVal.getOrCreatePreflightOutput());
            preflightOutputConfigurers.forEach(c -> c.accept(dsl));
        }

        return retVal;
    }

    public SidecarTestDSL duplicate() {
        SidecarTestDSL retVal = new SidecarTestDSL();
        this.copy(retVal);

        return retVal;
    }

    public void configureSidecarInput(Consumer<SidecarInputDSL> c) {
        if (inputConfigurers == null) {
            inputConfigurers = new ArrayList<>();
        }
        inputConfigurers.add(c);
    }

    public void configureOutput(Consumer<SidecarOutputDSL> c) {
        if (outputConfigurers == null) {
            outputConfigurers = new ArrayList<>();
        }
        outputConfigurers.add(c);
    }

    public void configurePreflightInput(Consumer<SidecarInputDSL> c) {
        if (preflightInputConfigurers == null) {
            preflightInputConfigurers = new ArrayList<>();
        }
        preflightInputConfigurers.add(c);
    }

    public void configurePreflightOutput(Consumer<SidecarOutputDSL> c) {
        if (preflightOutputConfigurers == null) {
            preflightOutputConfigurers = new ArrayList<>();
        }
        preflightOutputConfigurers.add(c);
    }

    public void throwSidecarException(String msg) {
        this.throwSidecarException = msg;
    }

    public void throwPreflightException(String msg) {
        this.throwPreflightException = msg;
    }
}
