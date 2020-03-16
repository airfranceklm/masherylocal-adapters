package com.airfranceklm.amt.sidecar.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "demandStack")
@NoArgsConstructor
@AllArgsConstructor
public class StackDemand {
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    @Singular
    private Map<String, String> params;

    public StackDemand(String name) {
        this.name = name;
    }

    public StackDemand pushParam(String p, String value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(p, value);
        return this;
    }
}
