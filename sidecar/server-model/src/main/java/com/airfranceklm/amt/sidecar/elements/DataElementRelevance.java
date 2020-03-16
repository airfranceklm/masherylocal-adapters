package com.airfranceklm.amt.sidecar.elements;

/**
 * The result of the data element extraction from the pre-processor event into the sidecar input.
 */
public enum DataElementRelevance {
    /**
     * Special relevance indicating that the service is not ready.
     */
    ServiceNotReady,
    /**
     * The element is not relevant for the input, and the API call processing should continue
     */
    Ignored,
    /**
     * Sidecar invocation is not required, as the output of the sidecar will be an empty one (no-operation).
     */
    Noop,
    /**
     * The client didn't include a (valid) entry in this call; error 400 need to be returned
     */
    ClientError,
    /**
     * A fault has occurred during building of the element; client should receive 500-type error message
     */
    InternalFault,

    /**
     * Element is accepted for the input and was incorporated into the sidecar input; the sidecar
     * has to be invoked.
     */
    Invoke
}
