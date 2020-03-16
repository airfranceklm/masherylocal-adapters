package com.airfranceklm.amt.sidecar.elements;

/**
 * A registry for the standard name elements. The application should not be using this directly, but via either
 * of the enums that provide a hint as to what type of element it is:
 * <ul>
 *     <li>{@link StringElements} for String elements</li>
 *     <li>{@link NumericElements} for numeric elements</li>
 *     <li>{@link PayloadElements} for payload</li>
 *     <li>{@link ParameterizedStringElement} for string elements that require an argument</li>
 *     <li>{@link ObjectElements} that extract individual objects from the request</li>
 *     <li>{@link SyntheticElements} that are added without attachment to the data of API call</li>
 * </ul>
 */
public class StandardElements {

    // Package key and EAVs
    static final String PackageKey = "packageKey";
    static final String PackageKeyEAV = "packageKeyEAV";
    static final String EAV = "eav";

    // Headers
    static final String NoHeaders = "-headers";
    static final String RequestHeader = "requestHeader";
    static final String ResponseHeader = "responseHeader";
    static final String RequestHeadersSkipping = "-requestHeaders";
    static final String ResponseHeadersSkipping = "-responseHeaders";
    static final String AllRequestHeaders = "+requestHeaders";
    static final String AllResponseHeaders = "+responseHeaders";

    // Paylaods
    static final String RequestPayload = "requestPayload";
    static final String RequestPayloadFragment = "requestPayloadFragment";
    static final String RequestPayloadSize = "requestPayloadSize";
    static final String ResponsePayload = "responsePayload";
    static final String ResponsePayloadFragment = "responsePayloadFragment";
    static final String ResponsePayloadSize = "responsePayloadSize";

    // Token-related
    static final String TokenGrantType = "grantType";
    static final String TokenScope = "tokenScope";
    static final String TokenUserContext = "userContext";
    static final String Token = "token";
     static final String FullToken = "+token";

     static final String TokenUserContextField = "userContextField";

    // Request routing
    static final String Routing = "routing";
    static final String RoutingHost = "routingHost";

    // Operation elements
    static final String Operation = "operation";
    static final String FullOperation = "+operation";
    static final String HttpVerb = "httpVerb";
    static final String ResourcePath = "resourcePath";

    // Common elements
    static final String RemoteAddress = "remoteAddress";
    static final String EndpointIdentification = "endpointIdentification";
    static final String MessageId = "messageId";

    static final String KillSwitch = "+denyService";
    static final String ServiceBreaker = "+serviceBreaker";
    static final String ResponseCode = "responseCode";

    static final String Relay = "relay";
}
