package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.testsupport.dsl.DSL;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SidecarTestDSL extends DSL<SidecarRequestCase> {

    private List<Consumer<SidecarInputDSL>> inputConfigurers;
    private List<Consumer<SidecarPreProcessorDSL>> outputConfigurers;

    private List<Consumer<SidecarInputDSL>> preflightInputConfigurers;
    private List<Consumer<SidecarPreProcessorDSL>> preflightOutputConfigurers;

    private String throwPreflightException;
    private String throwSidecarException;
    private boolean isPreProcessorCase = true;

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

        other.isPreProcessorCase = this.isPreProcessorCase;
        other.throwSidecarException = this.throwSidecarException;
        other.throwPreflightException = this.throwPreflightException;
    }

    @Override
    public SidecarRequestCase build() {
        final SidecarRequestCase retVal = super.build();
        retVal.setPreProcessorCase(this.isPreProcessorCase);

        retVal.preflightException = this.throwPreflightException;
        retVal.sidecarException = this.throwSidecarException;

        // ------------------------------------------------------
        // Sidecar input

        if (inputConfigurers != null) {
            SidecarInputDSL dsl = new SidecarInputDSL(retVal.getOrCreateSidecarInput());
            inputConfigurers.forEach(c -> c.accept(dsl));
        }

        if (outputConfigurers != null) {
            SidecarPreProcessorDSL dsl = new SidecarPreProcessorDSL(retVal.getOrCreateSidecarPreProcessorOutput());
            outputConfigurers.forEach(c -> c.accept(dsl));
        }

        // ------------------------------------------------------------
        // Pre-flight input

        if (preflightInputConfigurers != null) {
            SidecarInputDSL dsl = new SidecarInputDSL(retVal.getOrCreatePreflightInput());
            preflightInputConfigurers.forEach(c -> c.accept(dsl));
        }

        if (preflightOutputConfigurers != null) {
            SidecarPreProcessorDSL dsl = new SidecarPreProcessorDSL(retVal.getOrCreatePreflightOutput());
            preflightOutputConfigurers.forEach(c -> c.accept(dsl));
        }

        return retVal;
    }

    public SidecarTestDSL duplicate() {
        SidecarTestDSL retVal = new SidecarTestDSL();
        this.copy(retVal);

        return retVal;
    }

    public SidecarTestDSL preProcessorCase() {
        this.isPreProcessorCase = true;
        return this;
    }

    public SidecarTestDSL postProcessorCase() {
        this.isPreProcessorCase = false;
        return this;
    }

    public void configureSidecarInput(Consumer<SidecarInputDSL> c) {
        if (inputConfigurers == null) {
            inputConfigurers = new ArrayList<>();
        }
        inputConfigurers.add(c);
    }

    public void configureOutput(Consumer<SidecarPreProcessorDSL> c) {
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

    public void configurePreflightOutput(Consumer<SidecarPreProcessorDSL> c) {
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

    /**
     * Creates an instnace of this DSL.
     * @return empty instance of DSL class.
     */
    public static SidecarTestDSL make() {
        return new SidecarTestDSL();
    }
}
