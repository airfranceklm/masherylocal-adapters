package com.airfranceklm.amt.sidecar.model;

import java.util.List;
import java.util.Map;

/**
 * Base interface for the object communicating the command change.
 */
public interface CallModificationCommand extends PayloadCarrier {

    /**
     * Specifies the headers to drop.
     *
     * @return List of headers to remove, or null if not required
     */
    List<String> getDropHeaders();

    Map<String, Object> getPassFragments();

    List<String> getDropFragments();

    /**
     * Tells the caller whether this command intends to modify fragments of the passing payload.
     *
     * @return <code>true</code> if fragments will be modified, <code>false</code> otherwise.
     */
    default boolean modifiesFragments() {
        return (getDropFragments() != null && getDropFragments().size() > 0) ||
                (getPassFragments() != null && getPassFragments().size() > 0);
    }
}
