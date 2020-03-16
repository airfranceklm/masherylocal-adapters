package com.airfranceklm.amt.sidecar;

/**
 * Features that could be configured with {@link SidecarProcessorDefaults}.
 */
public enum SidecarProcessorFeature {
    ErrorCodes,
    LocalConfigurationProvider, MasheryConfigurationDialect,
    ConfigurationStore, ElementsFactory, IdempotentCallSupport,
    CircuitBreaker, Stacks, ProcessorServices, AsyncExecutors,
    ALCPAlgorithmsFactory, ALCPIdentityKeySet, ALCPSidecarKeySet
}
