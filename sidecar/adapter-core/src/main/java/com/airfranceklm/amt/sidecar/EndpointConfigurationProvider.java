package com.airfranceklm.amt.sidecar;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for the local configuration provider that could help with specialized configuration cases.
 */
public interface EndpointConfigurationProvider {

    void setup(SidecarProcessor processor);

    void start();

    void shutdown();

    /**
     * A programmatic interface to force a provider to load data from specified stream with the specified logical\
     * name. The handling of the data is provider-dependant.
     * @param streamName name of the stream
     * @param is stream
     * @throws IOException if loading is not possible, or error occurs.
     */
    void loadStream(String streamName, InputStream is) throws IOException;

    String describe();
}
