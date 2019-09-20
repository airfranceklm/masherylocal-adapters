package com.airfranceklm.amt.sidecar.input;

import com.airfranceklm.amt.sidecar.config.InputScopeExpansion;
import com.airfranceklm.amt.sidecar.config.MaxSizeComplianceRequirement;
import com.airfranceklm.amt.sidecar.config.MaxSizeSetting;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.filters.BoundNumberMatchCondition;
import com.airfranceklm.amt.sidecar.filters.ProcessorEventValueExtractor;
import com.airfranceklm.amt.sidecar.filters.SidecarScopeFilterGroup;
import com.airfranceklm.amt.sidecar.filters.SidecarScopeMatchCondition;
import com.airfranceklm.amt.sidecar.stack.AFKLMSidecarStack;
import com.airfranceklm.amt.sidecar.stack.AFKLMSidecarStacks;
import com.mashery.http.io.ContentSource;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import com.mashery.trafficmanager.model.auth.AuthorizationType;
import com.mashery.trafficmanager.model.core.ExtendedAttributes;
import com.mashery.trafficmanager.model.oauth.OAuthContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static com.airfranceklm.amt.sidecar.config.ParseUtils.lowercase;
import static com.airfranceklm.amt.sidecar.config.ParseUtils.splitValueList;

/**
 * Compiles {@link com.airfranceklm.amt.sidecar.config.SidecarConfiguration} into {@link SidecarInputBuilderImpl}.
 */
public class SidecarRuntimeCompiler {

    static private AFKLMSidecarStacks sidecarStacks;

    static {
        sidecarStacks = new AFKLMSidecarStacks();
    }


    public SidecarInputBuilder<PreProcessEvent> compilePreProcessor(SidecarConfiguration cfg) {
        SidecarPreProcessorInputBuilderImpl retVal = new SidecarPreProcessorInputBuilderImpl(cfg);

        int errors = compileCommon(cfg, retVal);
        // Scope filter


        if (cfg.requiresRequestHeaders()) {
            retVal.assertRequiredHeaders();
        }

        // 2.c. Which headers did it send?
        if (!cfg.needsExpansionOf(InputScopeExpansion.SuppressHeaders)) {

            if (cfg.includesRequestHeaders()) {
                retVal.addExpander(retVal::expandHeaders);
            } else if (cfg.skipsRequestHeaders()) {
                retVal.addExpander(retVal::expandHeadersSkipping);
            } else {
                retVal.addExpander(retVal::expandAllHeaders);
            }
        }

        // 5. What does it contain?
        expandUsing(retVal, InputScopeExpansion.RequestPayload, cfg, retVal::expandPreprocessToPayload);

        // 6. Where should it go?
        expandUsing(retVal, InputScopeExpansion.Routing, cfg, retVal::expandPreprocessToRouting);

        // If the configuration contains errors, fail all requests.
        if (errors > 0) {
            retVal.addConditionAssertion(SidecarRuntimeCompiler::alwaysFail);
        }

        return retVal;
    }

    public SidecarInputBuilder<PostProcessEvent> compilePostProcessor(SidecarConfiguration cfg) {
        SidecarPostProcessorInputBuilderImpl retVal = new SidecarPostProcessorInputBuilderImpl(cfg);

        int errors = compileCommon(cfg, retVal);
        retVal.addExpander(retVal::expandResponseCode);

        // 2.c. Which headers did it send?
        if (!cfg.needsExpansionOf(InputScopeExpansion.SuppressHeaders)) {

            if (cfg.needsExpansionOf(InputScopeExpansion.RequestHeaders)) {
                if (cfg.skipsRequestHeaders()) {
                    retVal.addExpander(retVal::expandRequestHeadersSkipping);
                } else {
                    retVal.addExpander(retVal::expandAllRequestHeaders);
                }
            }

            // Include the response headers.
            if (cfg.skipsResponseHeaders()) {
                retVal.addExpander(retVal::expandResponseHeadersSkipping);
            } else {
                retVal.addExpander(retVal::expandAllResponseHeaders);
            }
        }

        // 5. What does it contain?
        expandUsing(retVal, InputScopeExpansion.ResponsePayload, cfg, retVal::expandPostProcessorToResponsePayload);
        expandUsing(retVal, InputScopeExpansion.RequestPayload, cfg, retVal::expandPostProcessorToRequestPayload);

        // If the configuration contains errors, fail all requests.
        if (errors > 0) {
            retVal.addConditionAssertion(SidecarRuntimeCompiler::alwaysFail);
        }

        return retVal;
    }

    /**
     * Compile assertions that are common for pre and post-processor.
     *
     * @param cfg    sidecar configuration
     * @param retVal building to configure
     * @return number of errors in the supplied configuration. If more than zero, means that the configuration
     * is not operable.
     */
    private int compileCommon(SidecarConfiguration cfg, SidecarInputBuilderImpl<?> retVal) {
        int errors = 0;
        // Stack
        errors += resolveStack(retVal, cfg);

        errors += readScopeFilters(retVal, cfg);

        // Condition assertions
        compileCommonAssertions(retVal);

        if (cfg.getMaxSize() != null && cfg.getMaxSize().getCompliance() == MaxSizeComplianceRequirement.Blocking) {
            retVal.assertRequestBodySize();
        }

        // --------------------------------------------------------------
        // Scope expansions.

        // Fill-in the common parameters for the sidecar.
        retVal.addExpander(retVal::expandCommonSidecarParameters);

        // 1. Where did it come from?
        expandUsing(retVal, InputScopeExpansion.RemoteAddress, cfg, retVal::expandRemoteAddress);

        // 2. What does this API call will be trying to do?
        // 2.a What's the verb?
        expandUsing(retVal, InputScopeExpansion.RequestVerb, cfg, retVal::expandRequestVerb);

        // 2.b. Which operation does it try to do?
        expandUsing(retVal, InputScopeExpansion.Operation, cfg, retVal::expandOperation);

        // 3. How's the user that's authentication?

        expandUsing(retVal, InputScopeExpansion.GrantType, cfg, retVal::expandGrantType);
        expandUsing(retVal, InputScopeExpansion.TokenScope, cfg, retVal::expandTokenScope);
        expandUsing(retVal, InputScopeExpansion.Token, cfg, retVal::expandToken);

        // 4. What's the application?
        expandUsing(retVal, InputScopeExpansion.ApplicationEAVs, cfg, retVal::expandApplicationEAVs);
        expandUsing(retVal, InputScopeExpansion.PackageKeyEAVS, cfg, retVal::expandPackageKeyEAVS);

        // --------------------------------------------------------------------------------------------
        // Command properties.
        if (cfg.isIdempotentAware()) {
            retVal.expandConfigurationIdempotence();
        }

        return errors;
    }

    /**
     * Automation for the simple expander: expand the <code>builder</code> if <code>exp</code> needs to be
     * expanded according to the <code>cfg</code> using a supplied <code>expander</code>.
     *
     * @param <T>      requirep pre-processor type.
     * @param builder  builder
     * @param exp      expander parameter
     * @param cfg      configuration
     * @param expander expander to use
     */
    private static <T extends ProcessorEvent> void expandUsing(SidecarInputBuilderImpl<T> builder, InputScopeExpansion exp, SidecarConfiguration cfg, SidecarInputExpander<T> expander) {
        if (cfg.needsExpansionOf(exp)) {
            builder.addExpander(expander);
        }
    }

    /**
     * Expand the pre-flight <code>builder</code> if <code>exp</code> needs to be
     * expanded according to the <code>cfg</code> using a supplied <code>expander</code>.
     *
     * @param <T>      requirep pre-processor type.
     * @param builder  builder
     * @param exp      expander parameter
     * @param cfg      configuration
     * @param expander expander to use
     */
    private static <T extends ProcessorEvent> void expandPreflightUsing(SidecarInputBuilderImpl<T> builder, InputScopeExpansion exp, SidecarConfiguration cfg, SidecarInputExpander<T> expander) {
        if (cfg.needsPreflightExpansionOf(exp)) {
            builder.addExpander(expander);
        }
    }

    public SidecarInputBuilder<PreProcessEvent> compilePreFlight(SidecarConfiguration cfg) {
        SidecarPreProcessorInputBuilderImpl retVal = new SidecarPreProcessorInputBuilderImpl(cfg);

        retVal.expandForcedIdempotence();

        int errors = compileCommon(cfg, retVal);

        if (retVal.getSidecarConfig().requiresPreflightApplicationEAVs()) {
            retVal.assertPreflightApplicationEAVs();
        }

        if (retVal.getSidecarConfig().requiresPreflightPackageKeyEAVs()) {
            retVal.assertPreflightPackageKeyEAVs();
        }

        if (cfg.requiresPreflightRequestHeaders()) {
            retVal.assertRequiredPreflightHeaders();
        }

        // 2.c. Which headers did it send?
        if (cfg.expandsPreflightHeaders()) {
            retVal.addExpander(retVal::expandPreflightHeaders);
        }

        // Fill-in the common parameters for the sidecar.
        retVal.addExpander(retVal::expandCommonSidecarParameters);

        // 1. Where did it come from?
        expandPreflightUsing(retVal, InputScopeExpansion.RemoteAddress, cfg, retVal::expandRemoteAddress);

        // 2. What does this API call will be trying to do?
        // 2.a What's the verb?
        expandPreflightUsing(retVal, InputScopeExpansion.RequestVerb, cfg, retVal::expandRequestVerb);

        // 2.b. Which operation does it try to do?
        expandPreflightUsing(retVal, InputScopeExpansion.Operation, cfg, retVal::expandOperation);

        // 3. How's the user that's authentication?

        expandPreflightUsing(retVal, InputScopeExpansion.GrantType, cfg, retVal::expandGrantType);
        expandPreflightUsing(retVal, InputScopeExpansion.TokenScope, cfg, retVal::expandTokenScope);

        // 4. What's the application?
        expandPreflightUsing(retVal, InputScopeExpansion.ApplicationEAVs, cfg, retVal::expandApplicationEAVs);
        expandPreflightUsing(retVal, InputScopeExpansion.PackageKeyEAVS, cfg, retVal::expandPackageKeyEAVS);

        // If the configuration contains errors, fail all requests.
        if (errors > 0) {
            retVal.addConditionAssertion(SidecarRuntimeCompiler::alwaysFail);
        }

        return retVal;
    }

    private int resolveStack(SidecarInputBuilderImpl<?> builder, SidecarConfiguration cfg) {
        AFKLMSidecarStack stack = getStackFor(cfg);
        if (stack != null) {
            builder.setStack(stack);
            builder.setStackConfiguration(stack.configureFrom(cfg));

            return 0;
        } else {
            return 1;
        }
    }

    /**
     * Resolutoin of the stack that will be used to call lambda.
     * @param cfg
     * @return stack to use.
     */
    public AFKLMSidecarStack getStackFor(SidecarConfiguration cfg) {
        return sidecarStacks.getStackFor(cfg);
    }

    private static int readScopeFilters(SidecarInputBuilderImpl<?> retVal, SidecarConfiguration cfg) {
        AtomicInteger errors = new AtomicInteger();

        cfg.forEachScopeFilterEntry((entry) -> {

            boolean inclusive = entry.isInclusive();
            String group = entry.getGroup();
            String param = entry.getParam();
            String label = entry.getLabel();
            String value = entry.getValue();

            switch (group) {
                case "resourcePath":
                    SidecarScopeFilterGroup<String> rpGroup = retVal.getScopeFilter(group, null);
                    if (rpGroup == null) {
                        rpGroup = createResourcePathScopeFilter(group);
                        retVal.addScopeFilter(rpGroup);
                    }

                    // The label of the resource path is ignored. Even if multiple path matches will be defined,
                    // there is only a single request per an API call.
                    rpGroup.add(createResourcePathFilterCondition(label, value), inclusive);
                    break;
                case "httpVerb":
                    SidecarScopeFilterGroup<String> hvGroup = retVal.getScopeFilter(group, null);
                    if (hvGroup == null) {
                        hvGroup = createHTTPVerbScopeFilter(group);
                        retVal.addScopeFilter(hvGroup);
                    }

                    hvGroup.add(createStringSetMatchCondition(label, value, true), inclusive);
                    break;
                case "requestHeader":
                    if (param != null) {
                        SidecarScopeFilterGroup<String> hdrGroup = retVal.getScopeFilter(group, param);
                        if (hdrGroup == null) {
                            hdrGroup = createRequestHeaderScopeFilter(group, param);
                            retVal.addScopeFilter(hdrGroup);
                        }

                        hdrGroup.add(createMatchPatternCondition(label, value), inclusive);
                    }
                    // TODO: else record an error.
                    break;
                case "responseHeader":
                    if (param != null) {
                        SidecarScopeFilterGroup<String> respHdrGroup = retVal.getScopeFilter(group, param);
                        if (respHdrGroup == null) {
                            respHdrGroup = createResponseHeaderScopeFilter(group, param);
                            retVal.addScopeFilter(respHdrGroup);
                        }

                        respHdrGroup.add(createMatchPatternCondition(label, value), inclusive);
                    }
                    // TODO: else record an error.
                    break;
                case "packageKey":
                    SidecarScopeFilterGroup<String> pkGroup = retVal.getScopeFilter(group, null);
                    if (pkGroup == null) {
                        pkGroup = createPackageKeyScopeFilter(group);
                        retVal.addScopeFilter(pkGroup);
                    }
                    pkGroup.add(createStringSetMatchCondition(label, value, false), inclusive);
                    break;
                case "scope":
                    SidecarScopeFilterGroup<String> scopeGroup = retVal.getScopeFilter(group, null);
                    if (scopeGroup == null) {
                        scopeGroup = createOAuthTokenScopeFilter(group);
                        retVal.addScopeFilter(scopeGroup);
                    }
                    scopeGroup.add(createMatchPatternCondition(label, value), inclusive);
                    break;

                case "userContext":
                    SidecarScopeFilterGroup<String> ucGroup = retVal.getScopeFilter(group, null);
                    if (ucGroup == null) {
                        ucGroup = createOAuthTokenUserContextFilter(group);
                        retVal.addScopeFilter(ucGroup);
                    }
                    ucGroup.add(createMatchPatternCondition(label, value), inclusive);
                    break;

                case "eav":
                    SidecarScopeFilterGroup<String> eavGroup = retVal.getScopeFilter(group, param);
                    if (eavGroup == null) {
                        eavGroup = createApplicationEAVFilter(group, param);
                        retVal.addScopeFilter(eavGroup);
                    }
                    eavGroup.add(createMatchPatternCondition(label, value), inclusive);
                    break;
                case "packageKeyEAV":
                    SidecarScopeFilterGroup<String> pkEavGroup = retVal.getScopeFilter(group, param);
                    if (pkEavGroup == null) {
                        pkEavGroup = createPackageKeyEAVFilter(group, param);
                        retVal.addScopeFilter(pkEavGroup);
                    }
                    pkEavGroup.add(createMatchPatternCondition(label, value), inclusive);
                    break;
                case "responseCode":
                    SidecarScopeFilterGroup<Integer> repsCodeGroup = retVal.getScopeFilter(group, null);
                    if (repsCodeGroup == null) {
                        repsCodeGroup = createResponseCodeScopeFilter(group);
                        retVal.addScopeFilter(repsCodeGroup);
                    }
                    repsCodeGroup.add(createIntegerSetMatchCondition(label, value), inclusive);
                    break;
                default:
                    errors.incrementAndGet();
                    break;
            }
        });

        return errors.intValue();
    }

    private static <T extends ProcessorEvent> InspectionResult alwaysFail(T ppe) {
        return InspectionResult.Fail;
    }


    /**
     * Compile the precondition assertions
     *
     * @param builder builder to compile the conditional assertions for.
     */
    private static <T extends ProcessorEvent> void compileCommonAssertions(SidecarInputBuilderImpl<T> builder) {
        // If the configuration is not ready, add an assertion that the service wil fail.
        if (!builder.getStackConfiguration().isValid()) {
            builder.addConditionAssertion(SidecarRuntimeCompiler::alwaysFail);
        }

        final MaxSizeSetting mxSize = builder.getSidecarConfig().getMaxSize();
        if (mxSize != null) {
            if (mxSize.getCompliance() == MaxSizeComplianceRequirement.Filtering) {

                SidecarScopeFilterGroup<Long> sizeGroup = new SidecarScopeFilterGroup<>("payloadSize");
                sizeGroup.addPreProcessorExtractor((ppe, label) -> getPreProcessorBodySize(ppe));
                sizeGroup.addPostProcessorExtractor((ppe, label) -> getPostProcessorBodySize(ppe));

                sizeGroup.addInclusiveFilter(new BoundNumberMatchCondition(null, mxSize.getMaxSize()));

                builder.addScopeFilter(sizeGroup);
            }
        }

        if (builder.getSidecarConfig().requiresApplicationEAVs()) {
            builder.assertApplicationEAVs();
        }

        if (builder.getSidecarConfig().requiresPackageKeyEAVs()) {
            builder.assertPackageKeyEAVs();
        }


    }

    static Long getPostProcessorBodySize(PostProcessEvent ppe) {
        final ContentSource body = ppe.getClientResponse().getBody();
        return body == null ? 0 : body.getContentLength();
    }

    static Long getPreProcessorBodySize(PreProcessEvent ppe) {
        final ContentSource body = ppe.getServerRequest().getBody();
        return body == null ? 0 : body.getContentLength();
    }

    // ----------------------------------------------------------------------------------
    // Compilation methods=

    private static SidecarScopeFilterGroup<String> createRequestHeaderScopeFilter(String group, String param) {
        SidecarScopeFilterGroup<String> retVal = new SidecarScopeFilterGroup<>(group, param);

        retVal.addPreProcessorExtractor((ppe, hdr) -> ppe.getClientRequest().getHeaders().get(hdr));
        retVal.addPostProcessorExtractor((ppe, hdr) -> ppe.getCallContext().getRequest().getHTTPRequest().getHeaders().get(hdr));

        return retVal;
    }

    private static SidecarScopeFilterGroup<String> createResponseHeaderScopeFilter(String group, String param) {
        SidecarScopeFilterGroup<String> retVal = new SidecarScopeFilterGroup<>(group, param);

        retVal.addPostProcessorExtractor((ppe, hdr) -> ppe.getClientResponse().getHeaders().get(param));

        return retVal;
    }

    private static SidecarScopeFilterGroup<String> createHTTPVerbScopeFilter(String group) {
        SidecarScopeFilterGroup<String> retVal = new SidecarScopeFilterGroup<>(group);

        retVal.addPreProcessorExtractor((ppe, label) -> ppe.getServerRequest().getMethod().toLowerCase());
        retVal.addPostProcessorExtractor((ppe, label) -> ppe.getCallContext().getRequest().getHTTPRequest().getMethod().toLowerCase());
        return retVal;
    }


    private static SidecarScopeMatchCondition<Integer> createIntegerSetMatchCondition(String label, String suppliedValue) {
        SidecarScopeMatchCondition<Integer> retVal = new SidecarScopeMatchCondition<>(label);

        String[] split = splitValueList(suppliedValue);
        HashSet<Integer> v = new HashSet<>();
        for (String s : split) {
            v.add(Integer.parseInt(s));
        }

        retVal.setMatchSet(v);
        return retVal;
    }

    private static SidecarScopeFilterGroup<Integer> createResponseCodeScopeFilter(String group) {
        SidecarScopeFilterGroup<Integer> retVal = new SidecarScopeFilterGroup<Integer>(group);

        retVal.addPostProcessorExtractor((ppe, param) -> ppe.getClientResponse().getStatusCode());
        return retVal;
    }

    private static SidecarScopeFilterGroup<String> createResourcePathScopeFilter(String group) {
        SidecarScopeFilterGroup<String> retVal = new SidecarScopeFilterGroup<String>(group);

        retVal.addPreProcessorExtractor((ppe, param) -> ppe.getCallContext().getRequest().getPathRemainder());
        retVal.addPostProcessorExtractor((ppe, param) -> ppe.getCallContext().getRequest().getPathRemainder());
        return retVal;
    }

    private static SidecarScopeFilterGroup<String> createPackageKeyScopeFilter(String group) {
        SidecarScopeFilterGroup<String> retVal = new SidecarScopeFilterGroup<String>(group);

        retVal.addPreProcessorExtractor((ppe, param) -> ppe.getKey().getExternalID());
        retVal.addPostProcessorExtractor((ppe, param) -> ppe.getKey().getExternalID());
        return retVal;
    }

    private static SidecarScopeFilterGroup<String> createOAuthTokenScopeFilter(String group) {
        SidecarScopeFilterGroup<String> retVal = new SidecarScopeFilterGroup<String>(group);

        ProcessorEventValueExtractor<String> extractor = (ppe, param) -> {
            if (ppe.getAuthorizationContext() != null
                    && ppe.getAuthorizationContext().getType() == AuthorizationType.OAUTH_2) {

                OAuthContext ctx = (OAuthContext) ppe.getAuthorizationContext();
                if (ctx.getAccessToken() != null) {
                    return ctx.getAccessToken().getScope();
                }
            }
            // Default: return null
            return null;
        };

        retVal.addCommonExtractor(extractor);
        return retVal;
    }

    private static SidecarScopeFilterGroup<String> createOAuthTokenUserContextFilter(String group) {
        SidecarScopeFilterGroup<String> retVal = new SidecarScopeFilterGroup<String>(group);

        ProcessorEventValueExtractor<String> extractor = (ppe, param) -> {
            if (ppe.getAuthorizationContext() != null
                    && ppe.getAuthorizationContext().getType() == AuthorizationType.OAUTH_2) {

                OAuthContext ctx = (OAuthContext) ppe.getAuthorizationContext();
                if (ctx.getAccessToken() != null) {
                    return ctx.getAccessToken().getUserToken();
                }
            }
            // Default: return null
            return null;
        };

        retVal.addCommonExtractor(extractor);
        return retVal;
    }

    private static SidecarScopeFilterGroup<String> createApplicationEAVFilter(String group, String eavName) {
        SidecarScopeFilterGroup<String> retVal = new SidecarScopeFilterGroup<String>(group, eavName);

        ProcessorEventValueExtractor<String> extractor = (ppe, param) -> {
            if (ppe.getKey() != null
                    && ppe.getKey().getApplication() != null) {

                ExtendedAttributes ea = ppe.getKey().getApplication().getExtendedAttributes();
                if (ea != null) {
                    return ea.getValue(param);
                }
            }
            // Default: return null
            return null;
        };

        retVal.addCommonExtractor(extractor);
        return retVal;
    }

    private static SidecarScopeFilterGroup<String> createPackageKeyEAVFilter(String group, String eavName) {
        SidecarScopeFilterGroup<String> retVal = new SidecarScopeFilterGroup<String>(group, eavName);

        ProcessorEventValueExtractor<String> extractor = (ppe, param) -> {
            if (ppe.getKey() != null
                    && ppe.getKey().getExtendedAttributes() != null) {

                return ppe.getKey().getExtendedAttributes().getValue(param);
            }
            // Default: return null
            return null;
        };

        retVal.addCommonExtractor(extractor);
        return retVal;
    }

    private static SidecarScopeMatchCondition<String> createResourcePathFilterCondition(String label, String suppliedValue) {
        SidecarScopeMatchCondition<String> retVal = new SidecarScopeMatchCondition<>(label);
        final String regExp = suppliedValue
                // replace individual path expressions
                .replaceAll("\\{\\w+}", "[\\\\w-]+")
                .replaceAll("\\*", ".*");
        retVal.setMatchPattern(Pattern.compile(regExp));

        return retVal;
    }

    private static SidecarScopeMatchCondition<String> createMatchPatternCondition(String label, String suppliedValue) {
        SidecarScopeMatchCondition<String> retVal = new SidecarScopeMatchCondition<>(label);
        retVal.setMatchPattern(Pattern.compile(suppliedValue, Pattern.CASE_INSENSITIVE));

        return retVal;
    }

    private static SidecarScopeMatchCondition<String> createStringSetMatchCondition(String label, String suppliedValue, boolean forceLowercase) {
        SidecarScopeMatchCondition<String> retVal = new SidecarScopeMatchCondition<>(label);
        String[] split = splitValueList(suppliedValue);
        HashSet<String> v = new HashSet<>();
        if (forceLowercase) {
            Collections.addAll(v, lowercase(split));
        } else {
            Collections.addAll(v, split);
        }

        retVal.setMatchSet(v);
        return retVal;
    }

}
