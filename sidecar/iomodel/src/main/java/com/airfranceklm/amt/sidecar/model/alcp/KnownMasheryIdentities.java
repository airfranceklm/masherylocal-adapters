package com.airfranceklm.amt.sidecar.model.alcp;

import com.airfranceklm.amt.sidecar.identity.CounterpartIdentity;

/**
 * Interface for the object within a sidecar process that manages the known Mashery identities.
 */
public interface KnownMasheryIdentities {
    /**
     * Resolves the expected identity data based on the <code>areaId</code> and <code>keyId</code>
     * attributes supplied
     * @param areaId area id, as supplied by the sending
     * @param keyId key id, as supplied by the sender
     * @return Instance of {@link CounterpartIdentity} listing known keys for this identity, or null if not found.
     * If the identity is null, the process may continue and the sidecar may be able to produce the output, depending
     * on the protocol requirements.
     *
     * @throws UnknownMasheryRequester if the sidecar process requires the sidecar to service known identities.
     */
    CounterpartIdentity getMasheryIdentity(String areaId, String keyId) throws UnknownMasheryRequester;
}
