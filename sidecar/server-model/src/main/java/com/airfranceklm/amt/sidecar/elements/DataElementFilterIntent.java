package com.airfranceklm.amt.sidecar.elements;

/**
 * An intent of the element filter.
 */
public enum DataElementFilterIntent {
    /**
     * Matching filter de-scopes
     */
    MatchDescopes,
    /**
     * Matching filter scope
     */
    MatchScopes,
    /**
     * Match is required, otherwise the client should receive an error
     */
    MatchRequired,
    /**
     * Upon match, the client should receive a 400-type error.
     */
    MatchProhibits
}
