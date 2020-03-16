package com.airfranceklm.amt.testsupport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(builderMethodName = "buildProcessorCase")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MasheryProcessorTestCase {

    protected String name;

    protected TestCasePoint point;

    @Getter @Setter @Singular("inherit")
    protected List<String> inherited;

    @JsonProperty("desc")
    protected String description;
    protected MasheryEndpointModel endpoint;

    @JsonProperty("api client request")
    protected APIClientRequestModel clientRequest;

    @JsonProperty("request to the api origin")
    protected APIOriginInteractionModel originInteraction;

    @JsonProperty("mashery response")
    protected APIClientResponseModel masheryResponse;

    @JsonProperty("debug context")
    protected MasheryDebugContextInteractionModel debugContextInteraction;

    @JsonProperty("mashery cache")
    protected MasheryCacheInteractionModel cacheInteraction;

    public boolean isPreProcessorCase() {
        return TestCasePoint.PreProcessor == point || point == null;
    }

    public void acceptVisitor(@NonNull TestModelVisitor v) {
        if (endpoint != null) {
            endpoint.acceptVisitor(v);
        }

        if (originInteraction != null) {
            originInteraction.acceptVisitor(v);
        }

        if (clientRequest != null) {
            clientRequest.acceptVisitor(v);
        }
        if (masheryResponse != null) {
            masheryResponse.acceptVisitor(v);
        }
        if (debugContextInteraction != null) {
            debugContextInteraction.acceptVisitor(v);
        }
        if (cacheInteraction != null) {
            cacheInteraction.acceptVisitor(v);
        }

        v.visit(this);
    }




}
