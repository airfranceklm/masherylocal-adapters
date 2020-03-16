package com.airfranceklm.amt.sidecar.identity;

import com.airfranceklm.amt.sidecar.identity.PartyIdentity;

/**
 * Interface usable within the sidecar process and represents its identity
 */
public interface SidecarIdentity extends PartyIdentity {

    /**
     * Returns the name of this sidecar that can logically describe it.
     * @return String giving a logical name to this sidecar
     */
    String getName();
}
