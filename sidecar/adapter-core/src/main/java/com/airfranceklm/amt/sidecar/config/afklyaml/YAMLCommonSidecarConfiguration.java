package com.airfranceklm.amt.sidecar.config.afklyaml;

import com.airfranceklm.amt.sidecar.dsl.DslMethod;
import com.airfranceklm.amt.sidecar.model.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.airfranceklm.amt.sidecar.CommonExpressions.parseShortTimeInterval;

/**
 * The YAML facade
 */
public abstract class YAMLCommonSidecarConfiguration<SCType extends SidecarConfiguration> {
    private SCType target;

    public YAMLCommonSidecarConfiguration() {
        this.target = create();
    }

    protected abstract SCType create();

    public SCType retrieve() {
        return target;
    }

    SCType getTarget() {
        return target;
    }

    public YAMLCommonSidecarConfiguration<SCType> limitRequestSize(MaxPayloadSizeSetting setting) {
        setMaximumRequestPayloadSize(setting);
        return this;
    }

    public MaxPayloadSizeSetting getMaximumRequestPayloadSize() {
        return target.getMaximumRequestPayloadSize();
    }

    public void setMaximumRequestPayloadSize(MaxPayloadSizeSetting maximumRequestPayloadSize) {
        target.setMaximumRequestPayloadSize(maximumRequestPayloadSize);
    }

    public Map<String, Object> getParams() {
        return target.getSidecarParams();
    }

    public SidecarSynchronicity getSynchronicity() {
        return target.getSynchronicity();
    }

    public void setSynchronicity(SidecarSynchronicity synchronicity) {
        target.setSynchronicity(synchronicity);
    }

    public boolean isFailsafe() {
        return target.isFailsafe();
    }

    public void setFailsafe(boolean failsafe) {
        target.setFailsafe(failsafe);
    }

    public StackDemand getStack() {
        return target.getStack();
    }

    public void setStack(StackDemand stack) {
        target.setStack(stack);
    }

    public void setParams(Map<String, Object> sidecarParams) {
        target.setSidecarParams(sidecarParams);
    }

    public List<ElementDemand> getElements() {
        return target.getElements();
    }

    public void setElements(List<ElementDemand> elements) {
        target.setElements(elements);
    }

    public String getTimeout() {
        return String.valueOf(target.getTimeout());
    }

    public void setTimeout(String value) {
        final Integer timeout = parseShortTimeInterval(value);
        if (timeout != null) {
            this.target.setTimeout(timeout);
        }
    }

    public ALCPConfiguration getAlcp() {
        return target.getAlcpConfiguration();
    }

    public void setAlcp(ALCPConfiguration alcpConfiguration) {
        target.setAlcpConfiguration(alcpConfiguration);
    }

    /**
     * Configures the application-level call protection settings.
     */
    @DslMethod
    public YAMLCommonSidecarConfiguration<SCType> alcp(Consumer<ALCPConfiguration.ALCPConfigurationBuilder> c) {
        ALCPConfiguration.ALCPConfigurationBuilder cfg = ALCPConfiguration.demandALCP();

        Objects.requireNonNull(c, "Configuration lambda for ALCP is required").accept(cfg);
        target.setAlcpConfiguration(cfg.build());

        return this;
    }

    @DslMethod
    public YAMLCommonSidecarConfiguration<SCType> param(String p, Object v) {
        getTarget().addSidecarParameter(p, v);
        return this;
    }

    @DslMethod
    public YAMLCommonSidecarConfiguration<SCType> synchronous() {
        setSynchronicity(SidecarSynchronicity.RequestResponse);
        return this;
    }

    @DslMethod
    public YAMLCommonSidecarConfiguration<SCType> asynchronous() {
        setSynchronicity(SidecarSynchronicity.Event);
        return this;
    }

    @DslMethod
    public YAMLCommonSidecarConfiguration<SCType> synchronicity(SidecarSynchronicity sync) {
        setSynchronicity(sync);
        return this;
    }

    @DslMethod
    public YAMLCommonSidecarConfiguration<SCType> failsafe() {
        setFailsafe(true);
        return this;
    }

    @DslMethod
    public YAMLCommonSidecarConfiguration<SCType> surefire() {
        setFailsafe(false);
        return this;
    }

    @DslMethod
    public YAMLCommonSidecarConfiguration<SCType> timeout(String str) {
        setTimeout(str);
        return this;
    }

    @DslMethod
    public YAMLCommonSidecarConfiguration<SCType> stack(String stackName) {
        setStack(new StackDemand(stackName));
        return this;
    }

    @DslMethod
    public YAMLCommonSidecarConfiguration<SCType> stack(Consumer<StackDemand.StackDemandBuilder> c) {
        final StackDemand.StackDemandBuilder b = StackDemand.demandStack();

        Objects.requireNonNull(c).accept(b);

        target.setStack(b.build());
        return this;
    }


    @DslMethod
    public YAMLCommonSidecarConfiguration<SCType> alcp(String alg) {
        return alcp(alg, null);
    }

    @DslMethod
    public YAMLCommonSidecarConfiguration<SCType> alcp(String alg, Consumer<ALCPConfiguration.ALCPConfigurationBuilder> c) {
        ALCPConfiguration.ALCPConfigurationBuilder cfg = ALCPConfiguration
                .demandALCP()
                .algorithm(alg);

        if (c != null) {
            c.accept(cfg);
        }

        setAlcp(cfg.build());
        return this;
    }

    @DslMethod
    public YAMLCommonSidecarConfiguration<SCType> demandElement(String elemName) {
        return demandElement(elemName, null);
    }

    @DslMethod
    public YAMLCommonSidecarConfiguration<SCType> demandElement(ElementDemand elemDemand) {
        getTarget().addElement(elemDemand);
        return this;
    }

    @DslMethod
    public YAMLCommonSidecarConfiguration<SCType> demandElement(String elemName, String parameter) {
        getTarget().addElement(new ElementDemand(elemName, parameter));
        return this;
    }

    @DslMethod
    public YAMLCommonSidecarConfiguration<SCType> demandElement(Consumer<ElementDemand.ElementDemandBuilder> c) {
        ElementDemand.ElementDemandBuilder ed = ElementDemand
                .builder();

        Objects.requireNonNull(c).accept(ed);

        getTarget().addElement(ed.build());
        return this;
    }

    public void absorb(SidecarInstance inst) {
        SidecarDescriptor desc = Objects.requireNonNull(inst.getSidecar());

        setStack(Objects.requireNonNull(inst.getDeployment()).getStack());
        setParams(inst.getParams());
        setAlcp(desc.getAlcp());
        setElements(desc.getElements());
        setTimeout(String.valueOf(desc.getTimeout()));
        setSynchronicity(desc.getSynchronicity());

        if (desc.getMaxRequestPayload() != null) {
            setMaximumRequestPayloadSize(desc.getMaxRequestPayload());
        }

    }

}
