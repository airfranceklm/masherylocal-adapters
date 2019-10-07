package com.airfranceklm.amt.sidecar.config;

import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder class that will build the sidecar configuration from the Mashery properties.
 */
public class MasheryConfigSidecarConfigurationBuilder {

    /**
     * Default max payload size.
     */
    private static final long DEFAULT_MAX_PAYLOAD = 50 * 1024;

    private static final String CFG_MAX_PAYLOAD = "max-payload-size";
    private static final Pattern payloadParsePattern = Pattern.compile("(\\d{1,})([km])?b,(filtering|blocking)?");

    private static final String SIDECAR_PARAM_PREFIX = "sidecar-param-";
    private static final Pattern sidecarParamPattern = Pattern.compile(SIDECAR_PARAM_PREFIX + "(.{1,})");
    private static final int SIDECAR_PARAM_PREFIX_LENGTH = SIDECAR_PARAM_PREFIX.length();

    private static final Pattern preflightParamPattern = Pattern.compile("preflight-param-(.{1,})");

    static final String CFG_SYNCHRONICITY = "synchronicity";
    static final String CFG_TIMEOUT = "timeout";
    static final String CFG_FAILSAFE = "failsafe";
    static final String CFG_IDEMPOTENT_AWARE = "idempotent-aware";

    private static final String CFG_REQUIRED_REQUEST_HEADERS = "require-request-headers";

    private static final String CFG_INCLUDE_REQUEST_HEADERS = "include-request-headers";
    private static final String CFG_INCLUDE_RESPONSE_HEADERS = "include-response-headers";

    private static final String CFG_SKIP_REQUEST_HEADERS = "skip-request-headers";
    private static final String CFG_SKIP_RESPONSE_HEADERS = "skip-response-headers";

    private static final String CFG_REQUIRE_PACKAGE_KEY_EAVS = "require-packageKey-eavs";
    private static final String CFG_INCLUDE_PACKAGE_KEY_EAVS = "include-packageKey-eavs";

    private static final String CFG_STACK = "stack";
    static final String CFG_EXPAND = "expand-input";
    private static final String CFG_EXPAND_PREFLIGHT = "expand-preflight";
    private static final String CFG_REQUIRE_EAVS = "require-eavs";
    private static final String CFG_INCLUDE_EAVS = "include-eavs";

    // ------------------------------------------------------------------
    // Pre-flight settings
    private static final String CFG_PREFLIGHT_REQUIRE_HEADERS = "preflight-require-headers";
    private static final String CFG_PREFLIGHT_INCLUDE_HEADERS = "preflight-include-headers";
    private static final String CFG_PREFLIGHT_REQUIRE_EAVS = "preflight-require-eavs";
    private static final String CFG_PREFLIGHT_INCLUDE_EAVS = "preflight-include-eavs";
    private static final String CFG_PREFLIGHT_REQIURE_PACKAGE_KEY_EAVS = "preflight-require-packageKey-eavs";
    private static final String CFG_PREFLIGHT_INCLUDE_PACKAGE_KEY_EAVS = "preflight-include-packageKey-eavs";
    private static final String CFG_PREFLIGHT_ENABLED = "preflight-enabled";

    // ------------------------------------------------------------------
    // Configuration values
    static final String CFG_VAL_SYNC_EVENT = "event";
    static final String CFG_VAL_REQUEST_RESPONSE = "request-response";
    static final String CFG_VAL_NON_BLOCKING = "non-blocking";

    private static final Pattern numberPattern = Pattern.compile("\\d+");
    private static final Pattern floatPattern = Pattern.compile("\\d+\\.\\d+");
    private static final Pattern booleanPattern = Pattern.compile("true|false");
    private static final Pattern timeReferencePattern = Pattern.compile("(\\d+(\\.\\d+)?)([sm]{1})");


    public SidecarConfiguration buildFrom(ProcessorEvent pe) {
        if (pe instanceof PreProcessEvent) {
            return buildFrom((PreProcessEvent) pe);
        } else if (pe instanceof PostProcessEvent) {
            return buildFrom((PostProcessEvent) pe);
        } else {
            throw new IllegalStateException(String.format("Unsupported class: %s", pe.getClass().getName()));
        }
    }

    private SidecarConfiguration buildFrom(PreProcessEvent ppe) {
        final SidecarConfiguration retVal = getSidecarConfiguration(SidecarInputPoint.PreProcessor, ppe.getEndpoint().getProcessor().getPreProcessorParameters());
        applyEndpointIdentification(ppe, retVal);
        return retVal;
    }

    private void applyEndpointIdentification(ProcessorEvent ppe, SidecarConfiguration retVal) {
        retVal.setServiceId(ppe.getEndpoint().getAPI().getExternalID());
        retVal.setEndpointId(ppe.getEndpoint().getExternalID());
    }

    private SidecarConfiguration buildFrom(PostProcessEvent ppe) {
        final SidecarConfiguration retVal = getSidecarConfiguration(SidecarInputPoint.PostProcessor, ppe.getEndpoint().getProcessor().getPostProcessorParameters());
        applyEndpointIdentification(ppe, retVal);
        return retVal;
    }

    /**
     * Side car configuration for the specified endpoint using in-Mashery configuration.
     *
     * @param cfg parameters specified in the configuration
     * @return Parsed configuration object.
     */
    SidecarConfiguration getSidecarConfiguration(SidecarInputPoint point, Map<String, String> cfg) {

        // TODO: this algorithm is forgiving if the configuration contains keys
        // that are not managed. It may be worthwhile to change this, i.e. when an unsupported key
        // is supplied, the service will not be offered (e.g. this could be a typo).

        SidecarConfiguration retVal = new SidecarConfiguration(point);

        // Default configuration assumes that the function is async and is NOT fail-safe.
        // This is required in order to ensure that fail-safe functions are purposefully marked
        // as such in the configuration.
        retVal.setSynchronicity(SidecarSynchronicity.Event);
        retVal.setStack("http");
        retVal.setFailsafe(false);

        readString(cfg, CFG_SYNCHRONICITY, (syncVal) -> {
            switch (syncVal) {
                case CFG_VAL_REQUEST_RESPONSE:
                    retVal.setSynchronicity(SidecarSynchronicity.RequestResponse);
                    break;
                case CFG_VAL_NON_BLOCKING:
                    retVal.setSynchronicity(SidecarSynchronicity.NonBlockingEvent);
                    break;
                case CFG_VAL_SYNC_EVENT:
                    break;
                default:
                    // Increment the error counter.
                    retVal.incrementError();
                    break;
            }
        });

        readString(cfg, CFG_TIMEOUT, (timeoutVal) -> {
            parseSidecarTimeout(timeoutVal, retVal);
        });


        // ----------------------------------------------------------------
        // Request headers.
        readRequireIncludePair(cfg, CFG_REQUIRED_REQUEST_HEADERS, CFG_INCLUDE_REQUEST_HEADERS, (req, tokens) -> {
            retVal.processRequestHeader(req, ParseUtils.lowercase(tokens));
        });

        readRequireIncludePair(cfg, CFG_REQUIRE_EAVS, CFG_INCLUDE_EAVS, retVal::processApplicationEAV);
        readRequireIncludePair(cfg, CFG_REQUIRE_PACKAGE_KEY_EAVS, CFG_INCLUDE_PACKAGE_KEY_EAVS, retVal::processPackageKeyEAV);


        // ---------------------------------------------------------------------
        // Extended application values for the application and package key.

        readList(cfg, CFG_SKIP_REQUEST_HEADERS, retVal::skipRequestHeader);

        readList(cfg, CFG_INCLUDE_RESPONSE_HEADERS, retVal::includeResponseHeader);
        readList(cfg, CFG_SKIP_RESPONSE_HEADERS, retVal::skipResponseHeaders);

        readString(cfg, CFG_FAILSAFE, (token) -> {
            retVal.setFailsafe(Boolean.parseBoolean(token));
        });

        readString(cfg, CFG_IDEMPOTENT_AWARE, (token) -> {
            retVal.setIdempotentAware(Boolean.parseBoolean(token));
        });

        readList(cfg, CFG_EXPAND, (inputExpansions) -> {
            int misses = parseExpansionList(inputExpansions, retVal::expandTo);
            retVal.incrementError(misses);
        });


        // The maximum payload body will be added
        if (retVal.needsExpansionOf(InputScopeExpansion.RequestPayload)) {

            long uMaxSize = DEFAULT_MAX_PAYLOAD;
            MaxSizeComplianceRequirement uComplyReq = MaxSizeComplianceRequirement.Blocking;

            if (cfg.containsKey(CFG_MAX_PAYLOAD)) {
                Matcher m = payloadParsePattern.matcher(cfg.get(CFG_MAX_PAYLOAD));

                if (m.matches()) {
                    String size = m.group(1);
                    String scale = m.group(2);
                    String mode = m.group(3);

                    long base = Long.parseLong(size);
                    if (scale != null) {
                        switch (scale) {
                            case "k":
                                base = base * 1024L;
                                break;
                            case "m":
                                base = base * 1024L * 1024L;
                                break;
                            default:
                                break;
                        }
                    }

                    uMaxSize = base;

                    if ("filtering".equals(mode)) {
                        uComplyReq = MaxSizeComplianceRequirement.Filtering;
                    }
                } else {
                    retVal.incrementError();
                }
            }

            retVal.setMaxSize(new MaxSizeSetting(uMaxSize, uComplyReq));
        }

        readString(cfg, CFG_STACK, retVal::setStack);

        // Compute the transport parameters that will be passed
        // to the actual transport.
        String cfgRegex = String.format("%s\\..*", retVal.getStack());
        int cfgPrefixLength = retVal.getStack().length() + 1;

        grep(cfg, Pattern.compile(cfgRegex), (key, value) -> {
            retVal.getStackParams().put(key.substring(cfgPrefixLength), value);
        });

        // -------------------------------------------------------------------------
        // Gather functional parameters
        grep(cfg, sidecarParamPattern, (key, value) -> {
            retVal.addFunctionParam(key.substring(SIDECAR_PARAM_PREFIX_LENGTH), minimumTypeConversion(value));
        });

        readPreProcessorScopeFilters(retVal, cfg);

        // --------------------------------------
        // Read the PREFLIGHT settings, if enabled.

        readRequireIncludePair(cfg, CFG_PREFLIGHT_REQUIRE_HEADERS,
                CFG_PREFLIGHT_INCLUDE_HEADERS,
                retVal::processPreflightHeaders);
        readRequireIncludePair(cfg, CFG_PREFLIGHT_REQUIRE_EAVS,
                CFG_PREFLIGHT_INCLUDE_EAVS,
                retVal::processPreflightEAVs);
        readRequireIncludePair(cfg, CFG_PREFLIGHT_REQIURE_PACKAGE_KEY_EAVS,
                CFG_PREFLIGHT_INCLUDE_PACKAGE_KEY_EAVS,
                retVal::processPreflightPackageKeyEAVs);

        grep(cfg, preflightParamPattern, retVal::addPreflightParam);

        readList(cfg, CFG_EXPAND_PREFLIGHT, (tokens) -> {
            int missed = parsePreflightExpansionList(tokens, retVal);
            retVal.incrementError(missed);
        });

        readString(cfg, CFG_PREFLIGHT_ENABLED, (v) -> {
            retVal.setPreflightEnabled(Boolean.parseBoolean(v));
        });

        return retVal;
    }

    static void parseSidecarTimeout(String timeoutVal, SidecarConfiguration retVal) {
        Matcher m = timeReferencePattern.matcher(timeoutVal);
        if (m.matches()) {
            float f = Float.parseFloat(m.group(1));
            TimeUnit tu = TimeUnit.SECONDS;
            if ("m".equals(m.group(3))) {
                tu = TimeUnit.MINUTES;
            }
            retVal.setSidecarTimeout(Math.round(f * tu.toMillis(1)));
        } else {
            retVal.incrementError();
        }
    }

    static int parsePreflightExpansionList(String[] tokens, SidecarConfiguration retVal) {
        return parseExpansionList(tokens, (ise) -> {
            switch (ise) {
                // These options make no sense to expand, as these operations
                // are unlikely to be repeatable. A regular sidecar should take
                // care of filtering these parameters.
                case Operation:
                case RequestPayload:
                case RelayParams:
                case AllResponseHeaders:
                case ResponsePayload:
                    retVal.incrementError();
                    break;
                default:
                    retVal.addPreflightExpansions(ise);
            }
        });
    }

    static int parseExpansionList(String[] expandos, Consumer<InputScopeExpansion> c) {
        int errors = 0;
        for (String s : expandos) {
            switch (s.trim().toLowerCase()) {
                case "+requestheaders":
                    c.accept(InputScopeExpansion.AllRequestHeaders);
                    break;
                case "operation":
                    c.accept(InputScopeExpansion.Operation);
                    errors++;
                    break;
                case "remoteaddress":
                    c.accept(InputScopeExpansion.RemoteAddress);
                    break;
                case "granttype":
                    c.accept(InputScopeExpansion.GrantType);
                    break;
                case "tokenscope":
                    c.accept(InputScopeExpansion.TokenScope);
                    break;
                case "token":
                    c.accept(InputScopeExpansion.Token);
                    break;
                case "+token":
                    c.accept(InputScopeExpansion.FullToken);
                    break;
                case "routing":
                    c.accept(InputScopeExpansion.Routing);
                    break;
                case "requestpayload":
                    c.accept(InputScopeExpansion.RequestPayload);
                    break;
                case "responsepayload":
                    c.accept(InputScopeExpansion.ResponsePayload);
                    break;
                case "verb":
                    c.accept(InputScopeExpansion.RequestVerb);
                    break;
                case "+responseheaders":
                    c.accept(InputScopeExpansion.AllResponseHeaders);
                    break;
                case "relayparams":
                    c.accept(InputScopeExpansion.RelayParams);
                    break;
                default:
                    errors++;
            }
        }

        return errors;
    }

    /**
     * Greps the configuration values on a particular pattern of the key.
     *
     * @param cfg    configuration
     * @param p      pattern for the keu
     * @param lambda consumer lambda that will receive all matching keys and corresponding values.
     */
    private void grep(Map<String, String> cfg, Pattern p, BiConsumer<String, String> lambda) {
        if (cfg == null) {
            return;
        }

        cfg.forEach((key, value) -> {
            Matcher keyM = p.matcher(key);
            if (keyM.matches()) {
                lambda.accept(key, value);
            }
        });
    }

    private void readString(Map<String, String> cfg, String prop, IncludeSingleTokenOp lambda) {
        if (cfg == null) {
            return;
        }

        if (cfg.containsKey(prop)) {
            lambda.accept(cfg.get(prop));
        }
    }

    /**
     * Configures a list of properties that is passed.
     *
     * @param cfg    configuration map
     * @param prop   property
     * @param lambda lambda processing the response.
     */
    private void readList(Map<String, String> cfg, String prop, IncludeTokensOp lambda) {
        if (cfg == null) {
            return;
        }
        if (cfg.containsKey(prop)) {
            lambda.accept(ParseUtils.splitValueList(cfg.get(prop)));
        }
    }

    /**
     * Configured the include / require pair.
     *
     * @param cfg     supplied properties
     * @param require name of the key specifying the "required" list
     * @param include name of the key specifying the "include list"
     * @param lambda  receiving lambda function.
     */
    private void readRequireIncludePair(Map<String, String> cfg, String require, String include, IncludeConfigTokenOp lambda) {
        if (cfg == null) {
            return;
        }

        if (cfg.containsKey(require)) {
            lambda.accept(ConfigRequirement.Required, ParseUtils.splitValueList(cfg.get(require)));
        }

        if (cfg.containsKey(include)) {
            lambda.accept(ConfigRequirement.Included, ParseUtils.splitValueList(cfg.get(include)));
        }
    }

    @FunctionalInterface
    private interface IncludeConfigTokenOp {
        void accept(ConfigRequirement req, String... token);
    }

    @FunctionalInterface
    private interface IncludeTokensOp {
        void accept(String... token);
    }

    @FunctionalInterface
    private interface IncludeSingleTokenOp {
        void accept(String token);
    }

    private void readPreProcessorScopeFilters(SidecarConfiguration retVal, Map<String, String> cfg) {
        if (cfg == null) {
            return;
        }

        Pattern p = Pattern.compile("filter(out)?-(\\w{3,})(\\(([\\w-]{1,})\\))?(-([\\w-]+)(\\.\\w+)?)?");
        cfg.forEach((key, value) -> {
            Matcher m = p.matcher(key);
            if (m.matches()) {
                retVal.addScopeFilter(new SidecarScopeFilterEntry(
                        m.group(2),                     // group
                        m.group(4),                      // param
                        m.group(6),                     // label
                        value,                          // value
                        !("out".equals(m.group(1))))    // inclusive
                );
            }
        });
    }

    static Object minimumTypeConversion(String value) {
        if (value == null) {
            return null;
        }
        String uVal = value.trim();

        if ("null".equals(uVal)) {
            return null;
        } else if (numberPattern.matcher(uVal).matches()) {
            // Longs will be used only if the value exceeds the maximum.
            Long lv = new Long(uVal);
            if (lv > Integer.MAX_VALUE) {
                return lv;
            } else {
                return lv.intValue();
            }
        } else if (booleanPattern.matcher(uVal).matches()) {
            return Boolean.parseBoolean(uVal);
        } else if (floatPattern.matcher(uVal).matches()) {
            return Double.parseDouble(uVal);
        } else {
            return uVal;
        }
    }
}
