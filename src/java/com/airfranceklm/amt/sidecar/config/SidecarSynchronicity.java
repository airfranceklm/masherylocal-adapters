package com.airfranceklm.amt.sidecar.config;

/**
 * Synchronicity of the sidecar invocation:
 * <ol>
 *     <li>{@link #RequestResponse}: the response must be used to adapt the request to the
 *     backend / response to the client</li>
 *     <li>{@link #Event}: the invocation is performed within the context of processing a call, however waiting for
 *     the response to be returned.</li>
 *     <li>{@link #NonBlockingEvent}: the invocation is performed outside of the context of the processing call.
 *     This will increase the response time but it will not guarantee the invocation.</li>
 * </ol>
 */
public enum SidecarSynchronicity {
    RequestResponse, Event, NonBlockingEvent
}
