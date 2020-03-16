package com.airfranceklm.amt.sidecar.model;

import com.airfranceklm.amt.sidecar.elements.DataElementFilterIntent;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "demandElementFilter")
public class ElementFilterDemand {
    @Getter @Setter
    private String algorithm;
    @Getter @Setter @Builder.Default
    private DataElementFilterIntent intent = DataElementFilterIntent.MatchScopes;
    @Getter @Setter
    private String expression;
    @Getter @Setter
    private boolean negate;
    @Getter @Setter
    private String label;

    public ElementFilterDemand(String alg, DataElementFilterIntent intent) {
        this(alg, null, intent);
    }

    public ElementFilterDemand(String algorithm, String expression, DataElementFilterIntent intent) {
        this.algorithm = algorithm;
        this.intent = intent;
        this.expression = expression;
    }

    public ElementFilterDemand(String algorithm, String expression, DataElementFilterIntent intent, String label) {
        this(algorithm, expression, intent);
        this.label = label;
    }
}
