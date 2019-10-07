package com.airfranceklm.amt.sidecar.input;

import com.airfranceklm.amt.sidecar.*;
import com.airfranceklm.amt.sidecar.config.ConfigRequirement;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.config.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.config.SidecarSynchronicity;
import com.airfranceklm.amt.sidecar.filters.SidecarScopeFilterGroup;
import com.airfranceklm.amt.sidecar.filters.SidecarScopeFilteringResult;
import com.airfranceklm.amt.sidecar.stack.AFKLMSidecarStack;
import com.mashery.http.ParamGroup;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import com.mashery.trafficmanager.model.auth.AuthorizationType;
import com.mashery.trafficmanager.model.core.Application;
import com.mashery.trafficmanager.model.core.ExtendedAttributes;
import com.mashery.trafficmanager.model.core.Key;
import com.mashery.trafficmanager.model.oauth.AccessToken;
import com.mashery.trafficmanager.model.oauth.OAuthContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Runtime sidecar setting that can actually be used by the {@link com.airfranceklm.amt.sidecar.AFKLMSidecarProcessor}
 * to process the incoming events.
 */
public abstract class SidecarInputBuilderImpl<T extends ProcessorEvent> implements SidecarInputBuilder<T> {

    private static final Logger log = LoggerFactory.getLogger(AFKLMSidecarProcessor.class);
    static final String MASH_MSG_ID_HEADER = "X-Mashery-Message-ID";

    private SidecarConfiguration cfg;

    private Set<EventInspector<T>> conditionAssertions;

    private List<SidecarInputExpander<T>> expanders = new ArrayList<>();
    private List<CommandExpander<T>> commandExpanders = new ArrayList<>();

    private Set<SidecarScopeFilterGroup<?>> scopeFilters;

    private Set<String> requiredEAVs;
    private Set<String> requiredPackageKeyEAVs;


    private AFKLMSidecarStack stack;
    private AFKLMSidecarStack.AFKLMSidecarStackConfiguration stackConfiguration;

    private long lastChecked;

    SidecarInputBuilderImpl(SidecarConfiguration cfg) {
        this.cfg = cfg;
        commandExpanders.add(this::expandCommonCommand);
    }

    long getLastChecked() {
        return lastChecked;
    }

    void stillValid() {
        lastChecked = System.currentTimeMillis();
    }

    void addExpander(SidecarInputExpander<T> expander) {
        this.expanders.add(expander);
    }

    void setStackConfiguration(AFKLMSidecarStack.AFKLMSidecarStackConfiguration stackConfiguration) {
        this.stackConfiguration = stackConfiguration;
    }

    @Override
    public AFKLMSidecarStack.AFKLMSidecarStackConfiguration getStackConfiguration() {
        return stackConfiguration;
    }

    @Override
    public boolean requiresScopeFiltering() {
        return scopeFilters != null && scopeFilters.size() > 0;
    }

    @Override
    public boolean requiresPreconditionInspection() {
        return conditionAssertions != null && conditionAssertions.size() > 0;
    }

    /**
     * Adds a condition assertion of this request.
     *
     * @param filterGroup instance of the pre-processor instance. Null values are ignored.
     */
    void addScopeFilter(SidecarScopeFilterGroup<?> filterGroup) {
        if (filterGroup == null) {
            return;
        }

        if (scopeFilters == null) {
            scopeFilters = new HashSet<>();
        }
        scopeFilters.add(filterGroup);
    }

    /**
     * Add the assertion on the body size, depending on the type of the builder.
     */
    abstract void assertRequestBodySize();

    /**
     * Returns the scope filter group
     *
     * @param group name of the group
     * @param param optional parameter for the group.
     * @return instance of existing filter, or null if it doesn't exist.
     */
    <T> SidecarScopeFilterGroup<T> getScopeFilter(String group, String param) {
        if (scopeFilters == null) {
            return null;
        }

        for (SidecarScopeFilterGroup<?> f : scopeFilters) {
            if (Objects.equals(group, f.getGroup()) && Objects.equals(param, f.getParam())) {
                return (SidecarScopeFilterGroup<T>) f;
            }
        }

        return null;
    }

    @Override
    public Set<SidecarScopeFilterGroup<?>> getScopeFilters() {
        return scopeFilters;
    }

    /**
     * Adds a condition assertion of this request.
     *
     * @param ppi instance of the pre-processor instance. Null values are ignored.
     */
    void addConditionAssertion(EventInspector<T> ppi) {
        if (ppi == null) {
            return;
        }

        if (conditionAssertions == null) {
            conditionAssertions = new LinkedHashSet<>();
        }
        conditionAssertions.add(ppi);
    }

    @Override
    public Set<EventInspector<T>> getConditionAssertions() {
        return conditionAssertions;
    }

    @Override
    public AFKLMSidecarStack getStack() {
        return stack;
    }

    public void setStack(AFKLMSidecarStack stack) {
        this.stack = stack;
    }

    /**
     * Whether this builder will support idempotent calls. Default implementation delegates to the configuration
     * for the level of the endpoint.
     *
     * @return whether the idempotent configuration is supported.
     */
    @Override
    public boolean supportsIdempotentCalls(SidecarInput input) {
        return cfg.isIdempotentAware();
    }

    private SidecarInput buildInput(T ppe, SidecarScopeFilteringResult filterResult) throws IOException {

        SidecarInput retVal = new SidecarInput();
        for (SidecarInputExpander<T> exp: expanders) {
            exp.accept(ppe, retVal);
        }

        if (filterResult != null) {
            retVal.addAllParams(filterResult.getFilteredParams());
        }
        return retVal;
    }


    @Override
    public SidecarInvocationData build(T ppe) throws IOException {
        return build(ppe, null);
    }

    @Override
    public SidecarInvocationData build(T ppe, SidecarScopeFilteringResult filterResult) throws IOException {
        SidecarInvocationData retVal = new SidecarInvocationData(buildInput(ppe, filterResult), getStack(), getStackConfiguration());

        commandExpanders.forEach((exp) -> {
            exp.accept(ppe, retVal);
        });

        retVal.getInput().shrinkNullObjects();

        return retVal;
    }

    private void expandCommonCommand(T ppe, SidecarInvocationData cmd) {
        cmd.setServiceId(ppe.getEndpoint().getAPI().getExternalID());
        cmd.setEndpointId(ppe.getEndpoint().getExternalID());
        cmd.setCache(ppe.getCache());
        cmd.setDebugContext(ppe.getDebugContext());
    }

    private void expandConfigurationIdempotence(T ppe, SidecarInvocationData cmd) {
        cmd.setIdempotentAware(cfg.isIdempotentAware());
    }

    private void expandForceIdempotence(T ppe, SidecarInvocationData cmd) {
        cmd.setIdempotentAware(true);
    }

    public void expandForcedIdempotence() {
        commandExpanders.add(this::expandForceIdempotence);
    }

    public void expandConfigurationIdempotence() {
        commandExpanders.add(this::expandConfigurationIdempotence);
    }

    public void expandSidecarParams() {
        expanders.add(this::expandSidecarParams);
    }

    @FunctionalInterface
    interface CommandExpander<T> {
        void accept(T ppe, SidecarInvocationData cmd);
    }

    SidecarConfiguration getSidecarConfig() {
        return cfg;
    }


    void assertApplicationEAVs() {
        this.requiredEAVs = new HashSet<>();
        cfg.forEachApplicationEAV((cs) -> {
            if (cs.getRequired() == ConfigRequirement.Required) {
                this.requiredEAVs.add(cs.getToken());
            }
        });

        addConditionAssertion(this::inspectApplicationEAV);
    }

    void assertPreflightApplicationEAVs() {
        this.requiredEAVs = new HashSet<>();
        cfg.forEachPreflightApplicationEAV((cs) -> {
            if (cs.getRequired() == ConfigRequirement.Required) {
                this.requiredEAVs.add(cs.getToken());
            }
        });

        addConditionAssertion(this::inspectApplicationEAV);
    }

    void assertPackageKeyEAVs() {
        this.requiredPackageKeyEAVs = new HashSet<>();
        cfg.forEachPackageKeyEAV((cs) -> {
            if (cs.getRequired() == ConfigRequirement.Required) {
                this.requiredPackageKeyEAVs.add(cs.getToken());
            }
        });

        addConditionAssertion(this::inspectPackageKeyEAVs);
    }

    void assertPreflightPackageKeyEAVs() {
        this.requiredPackageKeyEAVs = new HashSet<>();
        cfg.forEachPreflightPackageKeyEAV((cs) -> {
            if (cs.getRequired() == ConfigRequirement.Required) {
                this.requiredPackageKeyEAVs.add(cs.getToken());
            }
        });

        addConditionAssertion(this::inspectPackageKeyEAVs);
    }


    InspectionResult doAssertBodySize(Long preProcessorBodySize) {
        if (preProcessorBodySize < cfg.getMaxSize().getMaxSize()) {
            return InspectionResult.Pass;
        } else {
            return InspectionResult.Reject;
        }
    }

    private InspectionResult inspectApplicationEAV(T event) {

        Key key = event.getKey();
        if (key != null) {
            Application app = key.getApplication();
            if (app != null) {
                return inspectExtendedAttributes(String.format("application %s(id=%s)", app.getName(), app.getExternalID()),
                        app.getExtendedAttributes(),
                        this.requiredEAVs);
            }
        }

        return InspectionResult.Fail;

    }

    private InspectionResult inspectPackageKeyEAVs(T event) {

        Key key = event.getKey();
        if (key != null) {
            return inspectExtendedAttributes(String.format("packageKey %s", key.getExternalID()),
                    key.getExtendedAttributes(), this.requiredPackageKeyEAVs);
        }

        return InspectionResult.Fail;

    }

    private InspectionResult inspectExtendedAttributes(String entityId, ExtendedAttributes ea, Set<String> requiredSet) {
        if (ea != null) {
            try {
                for (String s : requiredSet) {
                    String v = ea.getValue(s);

                    if (v == null || v.trim().length() == 0) {
                        return InspectionResult.Reject;
                    }
                }

                return InspectionResult.Pass;
            } catch (Throwable ex) {
                // This is needed because of malformed EAV structure in Mashery Local database.
                log.error(String.format("Could not extract EAV of %s for this reason: %s; probably EAV XML is malformed and needs optimizing.",
                        entityId,
                        ex.getMessage()), ex);
            }
        }
        return InspectionResult.Reject;
    }

    // ----------------------------------------------------------------------------
    // Expand operations

    private void expandKeyServiceParams(ProcessorEvent ppe, SidecarInput input) {
        input.setServiceId(ppe.getEndpoint().getAPI().getExternalID());
        input.setEndpointId(ppe.getEndpoint().getExternalID());

        Key appKey = ppe.getKey();
        if (appKey != null) {
            input.setPackageKey(appKey.getExternalID());
        }
    }

    void expandCommonSidecarParameters(ProcessorEvent ppe, SidecarInput input) {
        input.setPoint(cfg.getPoint());
        input.setSynchronicity(cfg.getSynchronicity());

        if (cfg.hasSidecarParams()) {
            input.addAllParams(cfg.getSidecarParams());
        }

        expandKeyServiceParams(ppe, input);
    }



    /**
     * Sets the point to be pre-flight and synchronicity to request-response.
     * @param ppe point; not used
     * @param input input being expanded.
     */
    void expandPreflightSidecarPoint(ProcessorEvent ppe, SidecarInput input) {
        input.setSynchronicity(SidecarSynchronicity.RequestResponse);
        input.setPoint(SidecarInputPoint.Preflight);
    }

    private void doExpandToken(ProcessorEvent ppe, SidecarInput input, BiConsumer<AccessToken, SidecarInputToken> c) {
        if (ppe.getAuthorizationContext() != null
                && ppe.getAuthorizationContext().getType() == AuthorizationType.OAUTH_2) {

            OAuthContext ctx = (OAuthContext) ppe.getAuthorizationContext();
            final AccessToken mashAccessToken = ctx.getAccessToken();

            if (mashAccessToken != null) {
                if (input.getToken() == null) {
                    input.setToken(new SidecarInputToken());
                }

                c.accept(mashAccessToken, input.getToken());
            }
        }
    }

    void expandToken(ProcessorEvent ppe, SidecarInput input) {
        doExpandToken(ppe, input, (mashAccessToken, tkn) -> {
            tkn.setExpires(mashAccessToken.getExpires());
            tkn.setUserContext(mashAccessToken.getUserToken());
            tkn.setScope(mashAccessToken.getScope());
            tkn.setGrantType(mashAccessToken.getGrantType());
        });
    }

    void expandFullToken(ProcessorEvent ppe, SidecarInput input) {
        doExpandToken(ppe, input, (mashAccessToken, tkn) -> {
            tkn.setExpires(mashAccessToken.getExpires());
            tkn.setUserContext(mashAccessToken.getUserToken());
            tkn.setScope(mashAccessToken.getScope());
            tkn.setGrantType(mashAccessToken.getGrantType());
            tkn.setBearerToken(mashAccessToken.getAccessToken());
        });
    }

    void expandGrantType(ProcessorEvent ppe, SidecarInput input) {
        doExpandToken(ppe, input, (mashAccessToken, tkn) -> {
            tkn.setGrantType(mashAccessToken.getGrantType());
        });
    }

    void expandTokenScope(ProcessorEvent ppe, SidecarInput input) {
        doExpandToken(ppe, input, (mashAccessToken, tkn) -> {
            tkn.setScope(mashAccessToken.getScope());
        });
    }

    void expandOperation(ProcessorEvent ppe,  SidecarInput input) {
        SidecarInputOperation op = new SidecarInputOperation();
        op.setPath(ppe.getCallContext().getRequest().getPathRemainder());
        op.setUri(ppe.getCallContext().getRequest().getURI().toString());
        op.setHttpVerb(ppe.getCallContext().getRequest().getHTTPRequest().getMethod());

        // Pass query data if it exists.
        final ParamGroup mashQueryData = ppe.getCallContext().getRequest().getQueryData();
        if (mashQueryData != null) {
            Map<String, String> queryParams = new HashMap<>();
            for (String p : mashQueryData) {
                queryParams.put(p, mashQueryData.get(p));
            }
            op.setQuery(queryParams);
        }

        input.setOperation(op);
    }

    void expandRemoteAddress(ProcessorEvent ppe, SidecarInput input) {
        input.setRemoteAddress(ppe.getCallContext().getRequest().getHTTPRequest().getRemoteAddr());
    }

    void expandApplicationEAVs(ProcessorEvent ppe, SidecarInput input) {
        Key appKey = ppe.getKey();
        if (appKey != null) {
            Application app = appKey.getApplication();

            if (app != null) {
                cfg.forEachApplicationEAV(eav -> {
                    String eavValue = app.getExtendedAttributes().getValue(eav.getToken());
                    if (eavValue != null) {
                        input.addApplicationEAV(eav.getToken(), eavValue);
                    }
                });
            }
        }
    }

    void expandPackageKeyEAVS(ProcessorEvent ppe, SidecarInput input) {
        Key appKey = ppe.getKey();
        if (appKey != null) {
            cfg.forEachPackageKeyEAV(eav -> {
                String eavValue = appKey.getExtendedAttributes().getValue(eav.getToken());
                if (eavValue != null) {
                    input.addPackageKeyEAV(eav.getToken(), eavValue);
                }
            });
        }
    }

    void expandRequestVerb(ProcessorEvent ppe, SidecarInput input) {
        input.getOrCreateOperation().setHttpVerb(ppe.getCallContext().getRequest().getHTTPRequest().getMethod());
    }

    void expandSidecarParams(ProcessorEvent ppe, SidecarInput input) {
        input.addAllParams(cfg.getSidecarParams());
    }
}
