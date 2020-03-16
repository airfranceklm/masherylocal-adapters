package com.airfranceklm.amt.sidecar.identity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicCredentialsDescriptor {
    @Getter
    @Setter
    @JsonProperty("k")
    private String key;

    @Getter @Setter @JsonProperty("sec")
    private String secret;

    public BasicCredentialsDescriptor(String key) {
        this.key = key;
    }

    public boolean fullyDefined() {
        return key != null && secret != null;
    }

    public boolean loginOnlyDefined() {
        return key != null;
    }
}
