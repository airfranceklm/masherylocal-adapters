package com.airfranceklm.amt.sidecar;

/**
 * Constants used by the Sidecar processor.
 */
public class SidecarProcessorConstants {

    public static final String RELAY_PARAM_NAME = "relay";
    public static final String RELAY_CONTEXT_VALUE = "/sidecar/Relay";


    /**
     * Name of the YAML file containing the identities.
     */
    public static final String ALCP_IDENTITY_FILE_NAME = ".alcp-id.keyset.yaml";
    public static final String ALCP_PASSWORD_FILE_NAME = ".alcp-id.key";
    /**
     * Default local configuration root
     */
    public static final String LOCAL_CONFIG_ROOT = "/etc/mashery/local-sidecar-configuration";
    /**
     * Default number of threads that will be processing asynchronous requests.
     */
    public static final int DEFAULT_ASYNC_EXECUTORS_POOL_SIZE = 20;
    public static final String ALCP_SIDECARS_KEYSET_FILE = "alcp-sidecars.keyset.yaml";
}
