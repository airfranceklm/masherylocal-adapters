package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;
import com.airfranceklm.amt.testsupport.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

import static com.airfranceklm.amt.yaml.YamlHelper.yamlStringOf;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SidecarInvocationTestCase extends MasheryProcessorTestCase {

    public SidecarInvocationTestCase() {
        super();
    }

    @JsonProperty("sidecar preflight") @Setter
    private SidecarPreProcessorMockData preflightInteraction;
    @JsonProperty("sidecar pre-processor") @Setter
    private SidecarPreProcessorMockData preProcessorInteraction;
    @JsonProperty("sidecar post-processor") @Setter
    private SidecarPostProcessorMockData postProcessorInteraction;


    @Builder(builderMethodName = "buildSidecarInvocationCase")
    public SidecarInvocationTestCase(String name
            , TestCasePoint point
            , @Singular("inheritFeature") List<String> inherited
            , String description
            , MasheryEndpointModel endpoint
            , APIClientRequestModel clientRequest
            , APIOriginInteractionModel originInteraction
            , APIClientResponseModel masheryResponse
            , MasheryDebugContextInteractionModel debugContextInteraction
            , MasheryCacheInteractionModel cacheInteraction
            , SidecarInteractions interactions
            , SidecarPreProcessorMockData preflightInteraction
            , SidecarPreProcessorMockData preProcessorInteraction
            , SidecarPostProcessorMockData postProcessorInteraction) {
        super(name, point, inherited, description, endpoint, clientRequest, originInteraction, masheryResponse, debugContextInteraction, cacheInteraction);
        this.preflightInteraction = preflightInteraction;
        this.preProcessorInteraction = preProcessorInteraction;
        this.postProcessorInteraction = postProcessorInteraction;
    }

    public SidecarInput allocOrGetPreProcessorInput() {
        return allocOrGetPreProcessorInput(true);
    }

    @JsonIgnore
    public SidecarInput getPreProcessorInput() {
        return allocOrGetPreProcessorInput(false);
    }

    public SidecarInput allocOrGetPreProcessorInput(boolean doCreate) {
        if (preProcessorInteraction == null && !doCreate) return null;
        return preProcessInteraction().ensureInputObject(doCreate);
    }

    public SidecarInput allocOrGetPostProcessorInput() {
        return allocOrGetPostProcessorInput(true);
    }

    @JsonIgnore
    public SidecarInput getPostProcessorInput() {
        return allocOrGetPostProcessorInput(false);
    }

    public SidecarInput allocOrGetPostProcessorInput(boolean doCreate) {
        return postProcessInteraction().ensureInputObject(doCreate);
    }

    @JsonIgnore
    public SidecarInput getPreflightInput() {
        return allocOrGetPreflightInput(false);
    }

    public SidecarInput allocOrGetPreflightInput() {
        return allocOrGetPreflightInput(true);
    }

    public SidecarInput allocOrGetPreflightInput(boolean doCreate) {
        return preflightInteraction().ensureInputObject(doCreate);
    }

    public JsonSidecarPreProcessorOutput allocOrGetPreProcessorOutput() {
        return allocOrGetPreProcessorOutput(true);
    }

    @JsonIgnore
    public JsonSidecarPreProcessorOutput getPreProcessorOutput() {
        return allocOrGetPreProcessorOutput(false);
    }


    public JsonSidecarPreProcessorOutput allocOrGetPreProcessorOutput(boolean doCreate) {
        if (preProcessorInteraction == null && !doCreate) return null;
        return preProcessInteraction().ensureOutputObject(doCreate);
    }

    private SidecarPreProcessorMockData preProcessInteraction() {
        if (preProcessorInteraction == null) {
            preProcessorInteraction = new SidecarPreProcessorMockData();
        }
        return preProcessorInteraction;
    }

    public JsonSidecarPostProcessorOutput allocOrGetPostProcessorOutput() {
        return allocOrGetPostProcessorOutput(true);
    }

    @JsonIgnore
    public JsonSidecarPostProcessorOutput getPostProcessorOutput() {
        return allocOrGetPostProcessorOutput(false);
    }

    public JsonSidecarPostProcessorOutput allocOrGetPostProcessorOutput(boolean doCreate) {
        return postProcessInteraction().ensureOutputObject(doCreate);
    }

    private SidecarPostProcessorMockData postProcessInteraction() {
        if (postProcessorInteraction == null) {
            postProcessorInteraction = new SidecarPostProcessorMockData();
        }
        return postProcessorInteraction;
    }

    public JsonSidecarPreProcessorOutput allocOrGetPreflightOutput() {
        return allocOrGetPreflightOutput(true);
    }

    @JsonIgnore
    public JsonSidecarPreProcessorOutput getPreflightOutput() {
        return allocOrGetPreflightOutput(false);
    }

    public JsonSidecarPreProcessorOutput allocOrGetPreflightOutput(boolean doCreate) {
        if (preflightInteraction == null && !doCreate) return null;

        return preflightInteraction().ensureOutputObject(doCreate);
    }

    private SidecarPreProcessorMockData preflightInteraction() {
        if (preflightInteraction == null) {
            preflightInteraction = new SidecarPreProcessorMockData();
        }
        return preflightInteraction;
    }

    @JsonIgnore
    public String getPreflightException() {
        return preflightInteraction != null ? preflightInteraction.getThrowException() : null;
    }

    @JsonIgnore
    public String getPreProcessorException() {
        return preProcessorInteraction != null ? preProcessorInteraction.getThrowException() : null;
    }

    @JsonIgnore
    public String getPostProcessorException() {
        return postProcessorInteraction != null ? postProcessorInteraction.getThrowException() : null;
    }

    public void dump() {
        System.out.println(yamlStringOf(JsonHelper.convert(this, Map.class)));
    }

    public void setPreflightOutput(JsonSidecarPreProcessorOutput v) {
        preflightInteraction().setOutput(v);
    }

    public void setPreProcessorInput(SidecarInput si) {
        preProcessInteraction().setInput(si);
    }

    public void setPreflightInput(SidecarInput si) {
        preflightInteraction().setInput(si);
    }

    public void setPostProcessorInput(SidecarInput si) {
        postProcessInteraction().setInput(si);
    }
}
