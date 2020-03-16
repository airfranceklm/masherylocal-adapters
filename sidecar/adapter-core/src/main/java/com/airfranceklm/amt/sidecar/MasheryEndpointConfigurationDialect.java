package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.PostProcessorSidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.PreFlightSidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.PreProcessorSidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.SidecarDescriptor;

import java.util.Map;
import java.util.Set;

/**
 * Interface for the Mashery endpoint configuration domain-specific language.
 */
public interface MasheryEndpointConfigurationDialect {

    String getName();

    enum UnrecognizedKeyLenience {
        UnrecognizedKeysAllowed, UnrecognizedKeysDisallowed
    }

    /**
     * Returns a reader that is suitable for reading the {@link PreFlightSidecarConfiguration} configuration.
     * @return reader instance, or null if not supporter.
     */
    MasheryConfigurationReader<PreFlightSidecarConfiguration> getPreflightReader();

    /**
     * Returns a reader that is suitable for reading the {@link PreProcessorSidecarConfiguration} configuration.
     * @return reader instance, or null if not supporter.
     */

    MasheryConfigurationReader<PreProcessorSidecarConfiguration> getPreProcessorReader();

    /**
     * Returns a reader that is suitable for reading the {@link PostProcessorSidecarConfiguration} configuration.
     * @return reader instance, or null if not supporter.
     */

    MasheryConfigurationReader<PostProcessorSidecarConfiguration> getPostProcessorReader();

    /**
     * Shortcut for {@link #preProcessorConfig(SidecarDescriptor, String, Map, Map)} that requires no parameters
     * @param desc Descriptor
     * @param env environment
     * @return Key-value map that should be entered into Mashery to configure
     */
    default Map<String,String> preProcessorConfig(SidecarDescriptor desc, String env) {
        return preProcessorConfig(desc, env, null, null);
    }

    /**
     * Creates a PRE-processor configuration, to the extent this would be supported by this dialect
     * @param desc Descriptor, required
     * @param env environment, required
     * @param params optional parameters
     * @return Key-value map that should be entered into Mashery to configure
     */
    Map<String,String> preProcessorConfig(SidecarDescriptor desc, String env, Map<String,Object> preParams,  Map<String,Object> params);

    /**
     * Shortcut for {@link #postProcessorConfig(SidecarDescriptor, String, Map)} that requires no parameters
     * @param desc descriptor, required
     * @param env name of the environment, requires
     * @return Key-value map that should be entered into Mashery to configure
     */
    default Map<String,String> postProcessorConfig(SidecarDescriptor desc, String env) {
        return postProcessorConfig(desc, env, null);
    }

    /**
     * Creates a POST-processor configuration, to the extent this would be supported by this dialect.
     * @param desc Descriptor, required
     * @param env environment, required
     * @param params optional parameters
     * @return Key-value map that should be entered into Mashery to configure
     */
    Map<String,String> postProcessorConfig(SidecarDescriptor desc, String env, Map<String,Object> params);

    /**
     * Qualifies what has to happen
     * @param keys keys that were not used in the configuration, if required by the logic of the particular dialect.
     * @return indication of whether it's safe to continue. The default implementation does not allow
     * any remaining keys.
     */
    default UnrecognizedKeyLenience qualifyRemainingKeys(Set<String> keys) {
        return UnrecognizedKeyLenience.UnrecognizedKeysDisallowed;
    }

}
