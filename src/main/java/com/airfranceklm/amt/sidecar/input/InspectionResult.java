package com.airfranceklm.amt.sidecar.input;

/**
 * Inspection result:
 * <ul>
 *     <li>{@link #Pass}: inspection has passed</li>,
 *     <li>{@link #Reject}: inspection is successful, but the conclusion is negative</li>
 *     <li>{@link #Noop}: inspection is successful, no action required</li>
 *     <li>{@link #Fail}: inspection has failed, e.g. due to incurred exception.</li>
 * </ul>
 */
public enum InspectionResult {
    Pass, Reject, Fail, Noop
}
