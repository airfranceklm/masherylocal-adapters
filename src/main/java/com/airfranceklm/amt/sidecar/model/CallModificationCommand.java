package com.airfranceklm.amt.sidecar.model;

import java.util.List;
import java.util.Map;

/**
 * Base interface for the object communicating the command change.
 */
public interface CallModificationCommand extends PayloadCarrier {

    /**
     * Specifies the headers to add
     * @return Map with headers, or null if not required.
     */
    Map<String,String> getAddHeaders();

    /**
     * Specifies the headers to drop.
     * @return List of headers to remove, or null if not required
     */
    List<String> getDropHeaders();

    /**
     * Checks whether the command adds the content type.
     * @return
     */
    boolean addsContentType();
}
