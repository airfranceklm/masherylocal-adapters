package com.airfranceklm.amt.sidecar.config.afkl;

import com.airfranceklm.amt.sidecar.CommonExpressions;
import com.airfranceklm.amt.sidecar.config.AbstractMasheryConfigReader;
import com.airfranceklm.amt.sidecar.elements.DataElementFilterIntent;
import com.airfranceklm.amt.sidecar.elements.StringFilterAlgorithms;
import com.airfranceklm.amt.sidecar.model.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.airfranceklm.amt.sidecar.CommonExpressions.parseShortTimeInterval;
import static com.airfranceklm.amt.sidecar.CommonExpressions.splitStandardValueList;
import static com.airfranceklm.amt.sidecar.config.KeySpecialization.CommonKey;
import static com.airfranceklm.amt.sidecar.config.KeySpecialization.ScopedKey;
import static com.airfranceklm.amt.sidecar.elements.DataElementFilterIntent.*;
import static com.airfranceklm.amt.sidecar.elements.ParameterizedStringElement.*;
import static com.airfranceklm.amt.sidecar.elements.SyntheticElements.KillSwitch;
import static com.airfranceklm.amt.sidecar.model.SidecarSynchronicity.*;

public abstract class AFKLDialectBase<SCType extends SidecarConfiguration> extends AbstractMasheryConfigReader<SCType> {

    /**
     * Default max payload size.
     */
    static final long DEFAULT_MAX_PAYLOAD = 50 * 1024;

    // ----------------------------------------------------------------------
    // Common configuration keys that apply to all sidecar points
    // Section A: The basic configuration

    private static final String CFG_KILL_SWITCH = "deny-service";
    private static final String CFG_FAILSAFE = "failsafe";
    private static final String CFF_SIDECAR_PARAM = "param-(.{1,})";
    private static final String CFG_STACK = "stack";
    private static final String CFG_STACK_PARAM = "stack.(.{1,})";
    private static final String CFG_TIMEOUT = "timeout";

    // Section B: The elements to be included in the call

    private static final String CFG_DEMANDED_ELEMENTS = "elements";
    private static final String CFG_INCLUDE_EAVS = "eavs";
    private static final String CFG_INCLUDE_REQUEST_HEADERS = "request-headers";
    private static final String CFG_INCLUDE_PACKAGE_KEY_EAVS = "packageKey-eavs";
    private static final String CFG_SKIP_REQUEST_HEADERS = "skip-request-headers";

    private static final String CFG_REQUIRE_EAVS = "require-eavs";
    private static final String CFG_REQUIRE_PACKAGE_KEY_EAVS = "require-packageKey-eavs";
    private static final String CFG_REQUIRE_REQUEST_HEADERS = "require-request-headers";

    // Section C: ALCP settings
    private static final String CFG_ALCP_ALG = "aclp.alg";
    private static final String CFG_ALCP_ALG_PARAM = "aclp.alg.(.{1,})";
    private static final String CFG_ALCP_SIDECAR_IDENT_REF = "aclp.sidecar.identity";
    private static final String CFG_ALCP_SIDECAR_PUBLIC_KEY = "aclp.sidecar.pk";
    private static final String CFG_ALCP_SIDECAR_PWD_SALT = "aclp.sidecar.salt";
    private static final String CFG_ALCP_SYMMETRIC_KEY = "aclp.sidecar.sk";

    // -------------------------------------------------------------------
    // Type-specific

    /**
     * Maximum payload size. This should be configured by the actual reader to indicate where
     * this belongs, to the pre-processor or to the post-processor.
     */
    protected static final String CFG_MAX_PAYLOAD = "max-payload-size";
    private static final String CFG_MAX_REQUEST_PAYLOAD = "max-request-size";
    private static final Pattern payloadParsePattern = Pattern.compile("(\\d{1,})([km])?b,(noop-exceeding|client-error)?");
    private static final Pattern acceptPattern = Pattern.compile("when-(\\w{3,})(\\(([\\w-]{1,})\\))?(-([\\w-]+)(\\.\\w+)?)?");
    private static final Pattern rejectPattern = Pattern.compile("unless-(\\w{3,})(\\(([\\w-]{1,})\\))?(-([\\w-]+)(\\.\\w+)?)?");

    /**
     * Synchronicity is defined for pre- and post-processors, but ignored for pre-flight.
     */
    static final String CFG_SYNCHRONICITY = "synchronicity";

    // ------------------------------------------------------------------
    // Configuration values
    private static final String CFG_VAL_SYNC_EVENT = "event";
    private static final String CFG_VAL_REQUEST_RESPONSE = "request-response";
    private static final String CFG_VAL_NON_BLOCKING = "non-blocking";

    private static final String CFG_VAL_NOOP = "noop-exceeding";
    private static final String CFG_VAL_CLIENT_ERROR = "client-error";

    static final String CFG_INCLUDE_RESPONSE_HEADERS = "response-headers";
    static final String CFG_REQUIRE_RESPONSE_HEADERS = "require-response-headers";

    static final String CFG_SKIP_RESPONSE_HEADERS = "skip-response-headers";
    static final String CFG_MAX_RESPONSE_PAYLOAD = "max-response-size";


    public AFKLDialectBase() {
        super();
    }

    public AFKLDialectBase(String prefix) {
        super(prefix);
    }

    // TODO: do we need to include all request/response headers?

    /**
     * Initializes the parameters that are common for all elements:
     * <ul>
     *     <li>Kill-switch</li>
     *     <li>Fiil-safe indication</li>
     *     <li>Sidecar parameters</li>
     *     <li>Stack</li>
     *     <li>Stack parameters</li>
     *     <li>Invocation timeout</li>
     *     <li>Elements to be expanded</li>
     *     <li>Application EAV (required and included)</li>
     *     <li>Package key EAV (required and included)</li>
     *     <li>Package key EAV (required and included)</li>
     *     <li>Request headers (required and included)</li>
     *     <li>All headers excluding specified list</li>
     *     <li>ALCP algorithm</li>
     *     <li>ALCP algorithm parameters</li>
     *     <li>ALCP sidecar identity</li>
     * </ul>
     */
    protected void supportCommon() {
        // -------------------------------------------------------
        // Section A of the common configuration.

        addSimple(CFG_KILL_SWITCH, ScopedKey, (cfg, m, v) -> cfg.demandElement(KillSwitch.getElementName()));

        add(CFG_FAILSAFE, ScopedKey, this::parseFailsafe);
        addSimple(CFF_SIDECAR_PARAM, ScopedKey, (cfg, m, v) -> cfg.addSidecarParameter(m.group(1), minimumTypeConversion(v)));

        addSimple(CFG_STACK, CommonKey, (cfg, m, v) -> cfg.allocOrGetStackDemand().setName(v));
        addSimple(CFG_STACK_PARAM, CommonKey, (cfg, m, v) -> cfg.allocOrGetStackDemand().pushParam(m.group(1), v));

        add(CFG_TIMEOUT, CommonKey, this::parseSidecarTimeout);

        // -------------------------------------------------------
        // Section B of the common configuration.

        // Include listed elements.
        add(CFG_DEMANDED_ELEMENTS, ScopedKey, this::parseDemandedElements);
        // Inclusion of the EAVs, Package key EAVs and Request headers.
        addSimple(CFG_INCLUDE_EAVS, ScopedKey, (cfg, m, v) -> forEachLexeme(v, (lexeme) -> cfg.demandElement(EAV.getElementName(), lexeme)));
        addSimple(CFG_INCLUDE_PACKAGE_KEY_EAVS, ScopedKey, (cfg, m, v) -> cfg.demandAll(PackageKeyEAV.getElementName(), splitStandardValueList(v)));
        addSimple(CFG_INCLUDE_REQUEST_HEADERS, ScopedKey, (cfg, m, v) -> cfg.demandAll(RequestHeader.getElementName(), splitStandardValueList(v)));
        // Skipping of the request headers.
        addSimple(CFG_SKIP_REQUEST_HEADERS, ScopedKey, (cfg, m, v) -> cfg.demandElement(RequestHeadersSkipping.getElementName(), v));

        // Mandatory inclusion of the EAVs, package key EAVds and Request headers
        addSimple(CFG_REQUIRE_EAVS, ScopedKey, (cfg, m, v) -> cfg.requireAll(EAV.getElementName(), splitStandardValueList(v)));
        addSimple(CFG_REQUIRE_PACKAGE_KEY_EAVS, ScopedKey, (cfg, m, v) -> cfg.requireAll(PackageKeyEAV.getElementName(), splitStandardValueList(v)));
        addSimple(CFG_REQUIRE_REQUEST_HEADERS, ScopedKey, (cfg, m, v) -> cfg.requireAll(RequestHeader.getElementName(), splitStandardValueList(v)));

        // Section C of the common configuration
        // Support for the ALCP algorithm and ALCP parameters; including sidecar identity
        addSimple(CFG_ALCP_ALG, CommonKey, (cfg, m, v) -> cfg.allocOrGetALCPConfiguration().setAlgorithm(v));
        addSimple(CFG_ALCP_ALG_PARAM, CommonKey, (cfg, m, v) -> cfg.allocOrGetALCPConfiguration().addParam(m.group(1), v));

        // Identity can be provided with two options:
        // - by reference, which requires a pre-loaded public key and encryption data; or
        // - by providing the key material
        addSimple(CFG_ALCP_SIDECAR_IDENT_REF, CommonKey, (cfg, m, v) -> cfg.allocOrGetALCPConfiguration().setSidecarIdentityRef(v));
        addSimple(CFG_ALCP_SIDECAR_PUBLIC_KEY, CommonKey, (cfg, m, v) -> cfg.allocOrGetALCPConfiguration().setPublicKey(v));
        addSimple(CFG_ALCP_SIDECAR_PWD_SALT, CommonKey, (cfg, m, v) -> cfg.allocOrGetALCPConfiguration().setPasswordSalt(v));
        addSimple(CFG_ALCP_SYMMETRIC_KEY, CommonKey, (cfg, m, v) -> cfg.allocOrGetALCPConfiguration().setSymmetricKey(v));

        add(CFG_MAX_PAYLOAD, CommonKey, (cfg, m, v) -> {
            if (cfg.getMaximumRequestPayloadSize() != null && cfg.getMaximumRequestPayloadSize().isDefault()) {
                return parsePayloadSize(cfg, m, v, cfg::maximumRequestPayloadSizeFrom);
            } else {
                return 0;
            }

        });
        add(CFG_MAX_REQUEST_PAYLOAD, CommonKey, (cfg, m, v) -> parsePayloadSize(cfg, m, v, cfg::maximumRequestPayloadSizeFrom));
    }

    protected void supportSynchronicity() {
        add(CFG_SYNCHRONICITY, CommonKey, this::parseSynchronicity);
    }

    protected void supportLimiters() {
        addSimple(acceptPattern, CommonKey, (cfg, m, dslExpr) -> confineSidecarTo(cfg, m, MatchScopes, dslExpr));
        addSimple(rejectPattern, CommonKey, (cfg, m, dlsExpr) -> confineSidecarTo(cfg, m, MatchDescopes, dlsExpr));
    }

    int parseFailsafe(SCType cfg, Matcher m, String value) {
        Boolean b = standardBooleanLexeme(value);
        if (b) {
            cfg.setFailsafe(b);
            return 0;
        } else {
            yieldParseMessage(String.format("Lexem %s is not valid for failsafe key %s", value, m.group()));
            return 1;
        }
    }

    int parseSynchronicity(SCType cfg, Matcher m, String value) {
        switch (value) {
            case CFG_VAL_REQUEST_RESPONSE:
                cfg.setSynchronicity(RequestResponse);
                break;
            case CFG_VAL_NON_BLOCKING:
                cfg.setSynchronicity(NonBlockingEvent);
                break;
            case CFG_VAL_SYNC_EVENT:
                cfg.setSynchronicity(Event);
                break;
            default:
                yieldParseMessage(String.format("Value `%s` is not a valid synchronicity", value));
                return 1;
        }

        return 0;
    }

    int parsePayloadSize(SCType cfg, Matcher keyMatcher, String value, BiConsumer<Long, MaxPayloadSizeExcessAction> c) {

        Matcher m = payloadParsePattern.matcher(value);
        int errors = 0;

        if (m.matches()) {
            MaxPayloadSizeExcessAction uComplyReq = MaxPayloadSizeExcessAction.BlockSidecarCall;

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
                        errors++;
                        yieldParseMessage(String.format("Payload scale literal '%s' is not valid", scale));
                        break;
                }
            }

            long uMaxSize = base;

            if (CFG_VAL_NOOP.equals(mode)) {
                uComplyReq = MaxPayloadSizeExcessAction.NoopSidecarCall;
            }

            c.accept(uMaxSize, uComplyReq);
            return 0;
        } else {
            yieldParseMessage(String.format("Payload size expression `%s` is not correct", value));
            errors++;
        }

        return errors;
    }

    int parseDemandedElements(SCType cfg, Matcher keyMatcher, String list) {
        for (String elem : splitStandardValueList(list)) {
            cfg.demandElement(elem);
        }
        return 0;
    }

    private void confineSidecarTo(SCType cfg, Matcher keyMatcher, DataElementFilterIntent intent, String dslExpr) {
        String group = keyMatcher.group(1);
        String param = keyMatcher.group(3);
        String label = keyMatcher.group(5);

        ElementFilterDemand efd = new ElementFilterDemand(StringFilterAlgorithms.DslExpression,
                dslExpr,
                intent);
        efd.setLabel(label);

        cfg.allocOrGetElementDemand(group, param).addFilter(efd);
    }

    int parseSidecarTimeout(SCType cfg, Matcher keyMatcher, String timeoutVal) {
        Integer interval = parseShortTimeInterval(timeoutVal);
        if (interval != null) {
            cfg.setTimeout(interval);
            return 0;
        } else {
            yieldParseMessage(String.format("Expression %s is not a valid short interval expressions", timeoutVal));
            return 1;
        }
    }


    protected Map<String, String> write(SidecarInstance si, SidecarInputPoint point) {
        Map<String, String> retVal = new LinkedHashMap<>();
        SidecarDescriptor desc = si.getSidecar();
        if (desc.getElements() != null
                && find(desc.getElements(), (e) -> e.isFor(KillSwitch)) != null) {
            retVal.put(CFG_KILL_SWITCH, "true");
        }

        if (si.getParams() != null) {
            si.getParams().forEach((k, v) -> {
                retVal.put(String.format("param-%s", k), String.valueOf(v));
            });
        }

        String syncVal;
        switch (desc.getSynchronicity()) {
            case RequestResponse: syncVal = CFG_VAL_REQUEST_RESPONSE; break;
            case Event: syncVal = CFG_VAL_SYNC_EVENT; break;
            case NonBlockingEvent:
            default:
                syncVal = CFG_VAL_NON_BLOCKING; break;
        }
        retVal.put(CFG_SYNCHRONICITY, syncVal);

        StackDemand sd = si.getDeployment().getStack();
        if (sd != null) {
            retVal.put(CFG_STACK, sd.getName());
            if (sd.getParams() != null) {
                sd.getParams().forEach((k, v) -> {
                    retVal.put(String.format("stack.%s", k), v);
                });
            }
        }

        retVal.put(CFG_TIMEOUT, String.valueOf(desc.getTimeout()));
        if (desc.getElements() != null) {
            List<String> includeReqHeaders = new ArrayList<>();
            List<String> reqReqHeaders = new ArrayList<>();

            List<String> includeRespHeaders = new ArrayList<>();
            List<String> reqRespHeaders = new ArrayList<>();

            List<String> eavs = new ArrayList<>();
            List<String> reqEav = new ArrayList<>();

            List<String> pkEavs = new ArrayList<>();
            List<String> reqPkEav = new ArrayList<>();

            List<String> inclElems = new ArrayList<>();

            for (ElementDemand ed : desc.getElements()) {
                // We don't need to include a kill-switch directly.
                if (!ed.eitherOf(KillSwitch)) {

                    if (ed.eitherOf(RequestHeader)) {
                        distributeOnFilter(ed, StringFilterAlgorithms.NonEmpty, MatchRequired, reqReqHeaders, includeReqHeaders);
                    } else if (ed.eitherOf(ResponseHeader)) {
                        distributeOnFilter(ed, StringFilterAlgorithms.NonEmpty, MatchRequired, reqRespHeaders, includeRespHeaders);
                    } else if (ed.eitherOf(PackageKeyEAV)) {
                        distributeOnFilter(ed, StringFilterAlgorithms.NonEmpty, MatchRequired, pkEavs, reqPkEav);
                    } else if (ed.eitherOf(EAV)) {
                        distributeOnFilter(ed, StringFilterAlgorithms.NonEmpty, MatchRequired, reqEav, eavs);
                    } else if (ed.eitherOf(ResponseHeadersSkipping)) {
                        retVal.put(CFG_SKIP_RESPONSE_HEADERS, ed.getParameter());
                    } else if (ed.eitherOf(RequestHeadersSkipping)) {
                        retVal.put(CFG_SKIP_REQUEST_HEADERS, ed.getParameter());
                    } else {
                        inclElems.add(ed.getName());
                    }
                }
            }

            includeCommaSeparated(inclElems, CFG_DEMANDED_ELEMENTS, retVal);

            includeCommaSeparated(reqReqHeaders, CFG_REQUIRE_REQUEST_HEADERS, retVal);
            includeCommaSeparated(includeReqHeaders, CFG_INCLUDE_REQUEST_HEADERS, retVal);

            includeCommaSeparated(reqRespHeaders, CFG_REQUIRE_RESPONSE_HEADERS, retVal);
            includeCommaSeparated(includeReqHeaders, CFG_INCLUDE_REQUEST_HEADERS, retVal);

            includeCommaSeparated(reqEav, CFG_REQUIRE_EAVS, retVal);
            includeCommaSeparated(eavs, CFG_INCLUDE_EAVS, retVal);

            includeCommaSeparated(reqPkEav, CFG_REQUIRE_PACKAGE_KEY_EAVS, retVal);
            includeCommaSeparated(pkEavs, CFG_INCLUDE_PACKAGE_KEY_EAVS, retVal);
        }

        switch (point) {
            case Preflight:
            case PreProcessor:
                if (desc.getMaxRequestPayload() != null) {
                    retVal.put(CFG_MAX_REQUEST_PAYLOAD, formatPayloadLimit(desc.getMaxRequestPayload()));
                }
            case PostProcessor:
                if (desc.getMaxResponsePayload() != null) {
                    retVal.put(CFG_MAX_RESPONSE_PAYLOAD, formatPayloadLimit(desc.getMaxResponsePayload()));
                }
        }

        return retVal;
    }

    private String formatPayloadLimit(MaxPayloadSizeSetting setting) {
        String action = setting.getAction() == MaxPayloadSizeExcessAction.NoopSidecarCall ? CFG_VAL_NOOP : CFG_VAL_CLIENT_ERROR;
        return String.format("%db,%s", setting.getLimit() / 1024, action);
    }

    private void includeCommaSeparated(List<String> l, String cfgKey, Map<String,String> m) {
        if (l.size() > 0) {
            m.put(cfgKey, CommonExpressions.toCommaSeparated(l));
        }
    }

    private void distributeOnFilter(ElementDemand ed, String alg, DataElementFilterIntent intent, List<String> req, List<String> noReq) {
        if (ed.hasFilter(alg, intent)) {
            req.add(ed.getParameter());
        } else {
            noReq.add(ed.getParameter());
        }
    }

    private <T> T find(Collection<T> list, Predicate<T> p) {
        return list.stream().filter(p).findFirst().orElse(null);
    }
}
