package com.airfranceklm.amt.sidecar.config;

/**
 * Indication of what needs to included in the Lambda request in addition to the base information:
 * <ul>
 *     <li>{@link #ApplicationEAVs}: include information about the listed ApplicationEAVs</li>
 *     <li>{@link #Token}: include information about the user context</li>
 *     <li>{@link #RequestHeaders}: include information about the request details (remote address, verb, path, query string)</li>
 *     <li>{@link #RequestPayload}: include the payload of the request</li>
 * </ul>
 */
public enum InputScopeExpansion {
    ApplicationEAVs, PackageKeyEAVS, Token, FullToken, GrantType, RequestHeaders, AllRequestHeaders, TokenScope,
    ResponseHeaders, AllResponseHeaders,
    RequestPayload, ResponsePayload,
    Operation, RemoteAddress, Routing,
    RequestVerb,
    RelayParams
}