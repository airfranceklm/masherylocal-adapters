package com.airfranceklm.amt.sidecar.model.alcp;

import com.airfranceklm.amt.sidecar.identity.ClusterIdentity;
import com.airfranceklm.amt.sidecar.identity.CounterpartIdentity;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Authentication channel, specifying Mashery and counterpart identity
 */
@AllArgsConstructor
public class SidecarAuthenticationChannel {

    @Getter
    private ClusterIdentity masheryIdentity;
    @Getter
    private CounterpartIdentity sidecarIdentity;

}
