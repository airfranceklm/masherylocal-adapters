package com.airfranceklm.amt.sidecar.model;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Descriptor of the function, facilitating managing the sidecar using the infrastructure-as-code.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SidecarDescriptor {

    @Getter @Setter
    String name;

    @Getter @Setter
    ALCPConfiguration alcp;
    @Getter @Setter @Singular
    List<ElementDemand> elements;

    @Getter @Setter @Singular
    List<SidecarParam> params;

    @Getter @Setter @Builder.Default
    long timeout = 3000L;

    @Getter @Setter @Builder.Default
    boolean idempotentAware = false;

    @Getter @Setter @Builder.Default
    SidecarSynchronicity synchronicity = SidecarSynchronicity.RequestResponse;

    @Getter @Setter  @Singular
    List<SidecarInputPoint> points;

    @Getter @Setter
    MaxPayloadSizeSetting maxRequestPayload;
    @Getter @Setter
    MaxPayloadSizeSetting maxResponsePayload;

    @Getter @Setter @Singular
    List<SidecarInstanceDeployment> deployments;

    public SidecarInstance instanceFor(@NonNull  String env, SidecarInputPoint sip) {
        return instanceFor(env, sip, null);
    }

    public SidecarInstance instanceFor(@NonNull  String env, SidecarInputPoint sip, Map<String,Object> params) {
        if (!deploysAt(sip)) {
            throw new IllegalArgumentException("This function doesn't deploy at this point");
        }

        SidecarInstanceDeployment d = Objects.requireNonNull(deployments).stream()
                .filter((c) -> env.equals(c.environmentName))
                .findFirst()
                .orElse(null);

        SidecarInstance retVal = SidecarInstance.builder()
                .params(params)
                .deployment(d)
                .sidecar(this)
                .build();

        retVal.validate(sip);
        return retVal;
    }

    public boolean deploysAt(SidecarInputPoint sip) {
        return getPoints() == null || getPoints().contains(sip);
    }
}
