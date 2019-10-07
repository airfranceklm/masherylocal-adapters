package com.airfranceklm.amt.sidecar.config;

import com.airfranceklm.amt.sidecar.SidecarOutput;
import com.airfranceklm.amt.sidecar.input.SidecarInputBuilderImpl;

import java.util.*;
import java.util.function.Consumer;

/**
 * Configuration of the sidecar required for the particular service Id and endpoint.
 * <p>
 * The sidecar configuration abstracts the configuration data that can be presented in the variety of sources. For
 * example, it hides the difference whether the configuration comes from Mashery key-value settings or it has
 * loaded from a local YAML configuration.
 * </p>
 * This configuration isn't immediately executable. A {@link SidecarInputBuilderImpl} needs to be built.
 * <p>
 * The configuration also centralized dealing with case insencitivity of the headers.
 */
public class SidecarConfiguration {

    private SidecarInputPoint point;
    private String serviceId;
    private String endpointId;

    private MaxSizeSetting maxSize;

    /**
     * Defines the synchronicity of the invocation.
     */
    private SidecarSynchronicity synchronicity;
    /**
     * Whether this function is fail-safe. Fail-safe sidecars are not essential for producing the return value
     * to the back-end and to the client. If a function is not a fail-safe -- i.e., an invocation must be successful --
     * then 500 should be returned if such function would fail.
     */
    private boolean failsafe;
    private boolean preflightEnabled = false;
    private boolean idempotentAware = false;

    private Map<String, String> stackParams;
    private Map<String, Object> sidecarParams;
    private Set<InputScopeExpansion> inputExpansions = new HashSet<>();
    private Set<ConfigSetting> applicationEAVs = new HashSet<>();
    private Set<String> skipRequestHeaders = new HashSet<>();
    private Set<String> skipResponseHeaders = new HashSet<>();
    private Set<ConfigSetting> packageKeyEAVs = new HashSet<>();
    private Set<ConfigSetting> requestHeaders = new HashSet<>();
    private Set<String> includeResponseHeaders = new HashSet<>();

    private Set<ConfigSetting> preflightHeaders = new HashSet<>();
    private Map<String, Object> preflightParams = new HashMap<>();
    private Set<ConfigSetting> preflightEAVs = new HashSet<>();
    private Set<ConfigSetting> preflightPackageKeyEAVs = new HashSet<>();
    private Set<InputScopeExpansion> preflightExpansions = new HashSet<>();

    private Set<SidecarScopeFilterEntry> scopeFilters = new HashSet<>();

    private long sidecarTimeout = 3000L;

    private String stack;

    private SidecarOutput staticModification;

    private int errors = 0;


    public SidecarConfiguration(SidecarInputPoint point) {
        this.point = point;
    }

    void setIdempotentAware(boolean how) {
        idempotentAware = how;
    }



    public boolean isIdempotentAware() {
        return idempotentAware;
    }

    void setPreflightEnabled(boolean how) {
        preflightEnabled = how;
    }

    /**
     * Indicates whether pre-flight is enabled (in the default configuration). The processor should use
     * {@link #demandsPreflightHandling()} instead.
     * @return
     */
    public boolean isPreflightEnabled() {
        return preflightEnabled;
    }

    public MaxSizeSetting getMaxSize() {
        return maxSize;
    }

    void expandTo(InputScopeExpansion ise) {
        inputExpansions.add(ise);
    }

    void expandPreflightTo(InputScopeExpansion exp) {
        preflightExpansions.add(exp);
    }

    public boolean needsExpansionOf(InputScopeExpansion ise) {
        return inputExpansions.contains(ise);
    }

    public boolean needsPreflightExpansionOf(InputScopeExpansion ise) {
        return preflightExpansions.contains(ise);
    }

    private <T> void filterOutNulls(T[] list, Consumer<T> c) {
        for (T v : list) {
            if (v != null) {
                c.accept(v);
            }
        }
    }

    void addPreflightExpansions(InputScopeExpansion... exps) {
        filterOutNulls(exps, preflightExpansions::add);
    }

    void processPreflightHeaders(ConfigRequirement req, String... str) {
        filterOutNulls(str, (s) -> {
            preflightHeaders.add(new ConfigSetting(s.toLowerCase(), req));
        });

    }

    void processPreflightEAVs(ConfigRequirement req, String... str) {
        filterOutNulls(str, (s) -> {
            preflightEAVs.add(new ConfigSetting(s, req));
        });
    }

    void processPreflightPackageKeyEAVs(ConfigRequirement req, String... str) {
        filterOutNulls(str, (s) -> {
            preflightPackageKeyEAVs.add(new ConfigSetting(s, req));
        });
    }

    void addPreflightParam(String key, String value) {
        preflightParams.put(key, value);
    }

    void processApplicationEAV(ConfigRequirement required, String... str) {
        expandTo(InputScopeExpansion.ApplicationEAVs);
        for (String s : str) {
            applicationEAVs.add(new ConfigSetting(s, required));
        }
    }

    void processPackageKeyEAV(ConfigRequirement required, String... str) {
        expandTo(InputScopeExpansion.PackageKeyEAVS);

        filterOutNulls(str, (s) -> {
            packageKeyEAVs.add(new ConfigSetting(s, required));
        });
    }

    void skipRequestHeaders(List<String> headers) {
        if (headers != null && headers.size() > 0) {
            skipRequestHeaders.addAll(headers);
            expandTo(InputScopeExpansion.RequestHeaders);
        }
    }

    void skipRequestHeader(String... headers) {
        Collections.addAll(skipRequestHeaders, ParseUtils.lowercase(headers));
    }

    public boolean skipsRequestHeader(String h) {
        return skipRequestHeaders.contains(h);
    }

    public boolean skipsRequestHeaders() {
        return skipRequestHeaders.size() > 0;
    }

    public boolean skipsResponseHeader(String h) {
        return skipResponseHeaders.contains(h);
    }

    public boolean skipsResponseHeaders() {
        return skipResponseHeaders.size() > 0;
    }

    void skipResponseHeaders(List<String> headers) {
        if (headers == null) {
            return;
        }
        if (headers.size() > 0) {
            if (skipResponseHeaders == null) {
                skipResponseHeaders = new HashSet<>();
            }

            skipResponseHeaders.addAll(headers);
            expandTo(InputScopeExpansion.ResponseHeaders);
        }
    }

    void skipResponseHeaders(String... headers) {
        expandTo(InputScopeExpansion.ResponseHeaders);
        Collections.addAll(skipResponseHeaders, headers);
    }

    boolean requiresNoApplicationEAVs() {
        return applicationEAVs == null || applicationEAVs.size() == 0;
    }

    public void forEachApplicationEAV(Consumer<ConfigSetting> function) {
        if (applicationEAVs != null) {
            applicationEAVs.forEach(function);
        }
    }

    boolean requiresApplicationEAV(String eav, ConfigRequirement req) {
        return applicationEAVs.contains(new ConfigSetting(eav, req));
    }

    public void forEachPreflightApplicationEAV(Consumer<ConfigSetting> function) {
        if (preflightEAVs != null) {
            preflightEAVs.forEach(function);
        }
    }

    public Set<InputScopeExpansion> getInputExpansions() {
        return inputExpansions;
    }

    boolean requiresNoPackageKeyEAVs() {
        return packageKeyEAVs == null || packageKeyEAVs.size() == 0;
    }

    boolean requiresPackageKeyEAVs(String eav, ConfigRequirement req) {
        return packageKeyEAVs.contains(new ConfigSetting(eav, req));
    }


    public void forEachPackageKeyEAV(Consumer<ConfigSetting> function) {
        if (packageKeyEAVs != null) {
            packageKeyEAVs.forEach(function);
        }
    }

    public void forEachPreflightPackageKeyEAV(Consumer<ConfigSetting> function) {
        if (preflightPackageKeyEAVs != null) {
            preflightPackageKeyEAVs.forEach(function);
        }
    }

    public void addStackParameter(String p, String value) {
        if (stackParams == null) {
            stackParams = new HashMap<>();
        }
        stackParams.put(p, value);
    }

    void addFunctionParam(String name, Object value) {
        if (sidecarParams == null) {
            sidecarParams = new HashMap<>();
        }
        sidecarParams.put(name, value);
    }

    /**
     * How a request header should be processed.
     *
     * @param require whether we should require that the client will send this header
     * @param headers list of headers.
     */
    void processRequestHeader(ConfigRequirement require, String... headers) {
        if (headers == null) {
            return;
        }

        filterOutNulls(headers, (s) -> {
            requestHeaders.add(new ConfigSetting(s.toLowerCase(), require));
        });

        expandTo(InputScopeExpansion.RequestHeaders);
    }

    /**
     * Checks if the specified header is actually required. The name of the header is case-insensitive.
     *
     * @param h header
     * @return true if header is required, false otherwise.
     */
    boolean demandsRequestHeader(String h, ConfigRequirement how) {
        return requestHeaders.contains(new ConfigSetting(h.toLowerCase(), how));
    }

    /**
     * How a request header should be processed.
     *
     * @param require whether we should require that the client will send this header
     * @param headers list of headers.
     */
    void processRequestHeaders(ConfigRequirement require, List<String> headers) {
        if (headers == null) {
            return;

        }
        headers.forEach((v) -> {
            requestHeaders.add(new ConfigSetting(v.toLowerCase(), require));
        });
        expandTo(InputScopeExpansion.RequestHeaders);
    }

    boolean includesResponseHeaders() {
        return includeResponseHeaders.size() > 0;
    }

    void includeResponseHeader(String... headers) {
        if (headers == null) {
            return;

        }

        expandTo(InputScopeExpansion.ResponseHeaders);
        filterOutNulls(headers, (s) -> {
            includeResponseHeaders.add(s.toLowerCase());
        });
    }


    void forEachRequestHeader(Consumer<ConfigSetting> func) {
        requestHeaders.forEach(func);
    }

    /**
     * Checks if the header is present.
     *
     * @param h header to check
     * @return true if header is present, false otherwise
     */
    boolean includesResponseHeader(String h) {
        return includeResponseHeaders.contains(h.toLowerCase());
    }

    public void forEachIncludedResponseHeader(Consumer<String> func) {
        includeResponseHeaders.forEach(func);
    }


    public Map<String, Object> getSidecarParams() {
        return sidecarParams;
    }

    @SuppressWarnings("unchecked")
    <T> T getSidecarParameter(String pName) {
        if (sidecarParams == null) {
            return null;
        } else {
            return (T) sidecarParams.get(pName);
        }
    }

    public boolean hasSidecarParams() {
        return sidecarParams != null && sidecarParams.size() > 0;
    }


    void setMaxSize(MaxSizeSetting maxSize) {
        this.maxSize = maxSize;
    }

    public SidecarSynchronicity getSynchronicity() {
        return synchronicity;
    }

    public void setSynchronicity(SidecarSynchronicity synchronicity) {
        this.synchronicity = synchronicity;
    }

    public boolean isFailsafe() {
        return failsafe;
    }

    void setFailsafe(boolean failsafe) {
        this.failsafe = failsafe;
    }

    /**
     * Method used only for unit testing. Returns the stack parameters that were read. For production use,
     * use {@link #getStackParams()}
     *
     * @return stack parameters as-is.
     */
    Map<String, String> getStackParamsWithoutNullChecks() {
        return stackParams;
    }

    public Map<String, String> getStackParams() {
        if (stackParams == null) {
            stackParams = new HashMap<>();
        }
        return stackParams;
    }

    void setStackParams(Map<String, String> stackParams) {
        this.stackParams = stackParams;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }


    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public void setSidecarTimeout(long sidecarTimeout) {
        this.sidecarTimeout = sidecarTimeout;
    }

    public long getSidecarTimeout() {
        return sidecarTimeout;
    }

    public SidecarOutput getStaticModification() {
        return staticModification;
    }

    void setStaticModification(SidecarOutput staticModification) {
        this.staticModification = staticModification;
    }

    /**
     * Increment an error in this configuration.
     */
    void incrementError() {
        errors++;
    }

    /**
     * Increment an error in this configuration.
     */
    void incrementError(int count) {
        errors += count;
    }

    boolean hasErrors() {
        return errors > 0;
    }

    void addScopeFilter(SidecarScopeFilterEntry entry) {
        scopeFilters.add(entry);
    }

    public void forEachHeaderConfig(Consumer<ConfigSetting> c) {
        requestHeaders.forEach(c);
    }

    /**
     * Carry out an action for each header configuration with the indicated configuration requirement
     *
     * @param req requirement level to match
     * @param c   consumer to receive all.
     */
    public void forEachHeaderConfig(ConfigRequirement req, Consumer<ConfigSetting> c) {
        requestHeaders.forEach((cs) -> {
            if (cs.getRequired() == req) {
                c.accept(cs);
            }
        });
    }

    public boolean requiresRequestHeaders() {
        return setContains(this.requestHeaders, ConfigRequirement.Required);
    }

    public boolean requiresPreflightRequestHeaders() {
        return setContains(this.preflightHeaders, ConfigRequirement.Required);
    }

    boolean includesRequestHeaders() {
        return setContains(this.requestHeaders, ConfigRequirement.Included);
    }

    public boolean requiresApplicationEAVs() {
        return setContains(this.applicationEAVs, ConfigRequirement.Required);
    }

    public boolean requiresPackageKeyEAVs() {
        return setContains(this.packageKeyEAVs, ConfigRequirement.Required);
    }

    public boolean requiresPreflightApplicationEAVs() {
        return setContains(this.preflightEAVs, ConfigRequirement.Required);
    }

    public boolean requiresPreflightPackageKeyEAVs() {
        return setContains(this.preflightPackageKeyEAVs, ConfigRequirement.Required);
    }

    private boolean setContains(Set<ConfigSetting> set, ConfigRequirement target) {
        for (ConfigSetting s : set) {
            if (s.getRequired() == target) {
                return true;
            }
        }

        return false;
    }

    boolean demandsScopeFiltering() {
        return scopeFilters != null && scopeFilters.size() > 0;
    }

    boolean demandsScopeFilteringOn(String group, String param, String label, String value, boolean include) {
        if (scopeFilters == null) {
            return false;
        }

        SidecarScopeFilterEntry entry = new SidecarScopeFilterEntry(group, param, label, value, include);
        return scopeFilters.contains(entry);
    }

    /**
     * Execute an action on each entry in the scope filter.
     *
     * @param lambda code to handle the scope filter entyr.
     */
    public void forEachScopeFilterEntry(Consumer<SidecarScopeFilterEntry> lambda) {
        if (scopeFilters != null) {
            scopeFilters.forEach(lambda);
        }
    }

    public SidecarInputPoint getPoint() {
        return point;
    }

    boolean demandsPreflightHeader(String h, ConfigRequirement how) {
        return preflightHeaders.contains(new ConfigSetting(h.toLowerCase(), how));
    }

    public void forEachPreflightHeader(Consumer<ConfigSetting> c) {
        preflightHeaders.forEach(c);
    }

    public Set<ConfigSetting> getRequestHeaders() {
        return requestHeaders;
    }

    public Map<String, Object> getPreflightParams() {
        return preflightParams;
    }

    public Set<ConfigSetting> getPreflightHeaders() {
        return preflightHeaders;
    }

    public boolean expandsPreflightHeaders() {
        return preflightHeaders.size() > 0;
    }

    void setSidecarParams(Map<String, Object> sidecarParams) {
        this.sidecarParams = sidecarParams;
    }

    void setPreflightParams(Map<String, Object> preflightParams) {
        this.preflightParams = preflightParams;
    }

    boolean needsNoExpansion() {
        return inputExpansions.size() == 0;
    }

    /**
     * Checks if the configuration demands preflight processing. The demand is stnading if pre-flight
     * is explicitly enabled or either of the associated options is specified as non-null value.
     *
     * @return true if preflight class enabled explicitly or expansions are specified
     */
    public boolean demandsPreflightHandling() {
        return preflightEnabled || preflightHeaders.size() > 0
                || preflightParams.size() > 0
                || preflightEAVs.size() > 0
                || preflightPackageKeyEAVs.size() > 0
                || preflightExpansions.size() > 0;
    }

    /**
     * Checks whether the configuraiton actuall demands the application EAV with the specified requirement level.
     * @param eav name of the EAV
     * @param required requirement level
     * @return <code>true</code> if the configuration requires this EAV with specified leve, false otherwise.
     */
    boolean demandsPreflightApplicationEAV(String eav, ConfigRequirement required) {
        return preflightEAVs.contains(new ConfigSetting(eav, required));
    }

    /**
     * Checks whether the configuration actually demands specified EAV with the specified level
     * @param eav EAV
     * @param reqLevel requirement level
     * @return true if specified with this level, false otherwise.
     */
    boolean demandsPreflightPackageKeyEAV(String eav, ConfigRequirement reqLevel) {
        return preflightPackageKeyEAVs.contains(new ConfigSetting(eav, reqLevel));
    }
}
