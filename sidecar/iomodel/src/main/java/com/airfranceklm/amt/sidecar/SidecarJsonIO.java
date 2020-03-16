package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.CallModificationCommand;
import com.airfranceklm.amt.sidecar.model.SidecarOutput;

/**
 * Sidecar-specific Json I/O, intended to support Mashery to sidecar dialects
 */
public interface SidecarJsonIO extends JsonIO {

    /**
     * Converts an output object to the dialect that is understood by Mashery. Default implementation
     * returns the <code>output</code> object as-is, without modifications
     * @param output output objects.
     * @param <T> type of the modification
     * @return Object that should be written as a response to Mashery.
     */
    default <T extends CallModificationCommand> Object toMasheryDialect(SidecarOutput<T> output) {
        return output;
    }
}
