package com.airfranceklm.amt.sidecar.model.alcp;

import com.airfranceklm.amt.sidecar.identity.CounterpartIdentity;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ProtectedSidecarRequest {
    @Getter
    SidecarInput input;
    @Getter
    CounterpartIdentity caller;

    public ProtectedSidecarRequest(SidecarInput input) {
        this(input, null);
    }

}
