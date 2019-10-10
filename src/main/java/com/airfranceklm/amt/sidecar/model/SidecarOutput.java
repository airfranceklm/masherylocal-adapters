package com.airfranceklm.amt.sidecar.model;


import java.io.Serializable;
import java.util.Date;

/**
 * Base interface for the Sidecar output. This interface as such is not used directly, bur rather through it's
 * subclasses:
 * <ul>
 *     <li>a pre-processor sidecar should return output that is compatible with {@link SidecarPreProcessorOutput} which can
 *    communicate relay parameters to the post-processor;</li>
 *  <li>A post-processor should return output that will be compatible with {@link SidecarPostProcessorOutput}
 *      interface</li>
 * </ul>
 *
 * @param <T> type of the response modification command.
 */
public interface SidecarOutput<T extends CallModificationCommand> extends Serializable  {

    Date getUnchangedUntil();

    SidecarOutputTerminationCommand getTerminate();

    T getModify();

    /**
     * Returns true if this object contains a non-null {@link #getTerminate()} value.
     * @return true if termination was communicated, false otherwise.
     */
    boolean terminates();

    /**
     * Returns true if this object contains call modification settings.
     * @return true if {@link #getModify()} will return a non-null object, false otherwise.
     */
    boolean modifies();
}
