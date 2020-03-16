package com.airfranceklm.amt.sidecar.model.alcp;

import com.airfranceklm.amt.sidecar.model.SidecarSynchronicity;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EncryptedMessage<T> {
    @Getter
    SidecarSynchronicity synchronicity;
    @Getter
    private T payload;
    @Getter @Singular("contextEntry")
    private Map<String,String> context;

    public EncryptedMessage(SidecarSynchronicity sync, T payload) {
        this(sync, payload, null);
    }

}
