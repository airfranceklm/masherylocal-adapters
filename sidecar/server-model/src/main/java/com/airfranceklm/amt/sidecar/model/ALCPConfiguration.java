package com.airfranceklm.amt.sidecar.model;

import com.airfranceklm.amt.sidecar.model.alcp.AlgorithmActivation;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Application-level call protection configuration
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "demandALCP")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ALCPConfiguration {

    @Getter @Setter private String algorithm;
    @Getter @Setter private AlgorithmActivation activation;
    @Getter @Setter @Singular @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String,Object> params;

    @Getter @Setter private String sidecarIdentityRef;
    @Getter @Setter private String publicKey;
    @Getter @Setter private String passwordSalt;
    @Getter @Setter private String symmetricKey;

    public ALCPConfiguration(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Helper method to see if the configuration defines encryption parameters explicitly as opposed to
     * the definition-by-reference with {@link #getSidecarIdentityRef()} method.
     * @return true if either of the fields has been defined, false otherwise.
     */
    public boolean definesIdentityExplicitly() {
        return publicKey != null || passwordSalt != null || symmetricKey != null;
    }

    public void addParam(String key, Object v) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, v);
    }
}
