package com.airfranceklm.amt.sidecar.filters;

import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Lambda scope filter group.
 *
 * @param <T>
 */
public class SidecarScopeFilterGroup<T> {
    private String group;
    private String param;
    private boolean outputMap;

    private ProcessorEventValueExtractor<T> commonExtractor;
    private PreProcessorEventValueExtractor<T> preProcessorExtractor;
    private PostProcessorEventValueExtractor<T> postProcessorExtractor;

    /**
     * Group will match if either of these will match
     */
    private Set<SidecarScopeMatchCondition<T>> inclusionFilters = new HashSet<>();
    /**
     * Group will match if neither of thewse will match.
     */
    private Set<SidecarScopeMatchCondition<T>> exclusionFilters = new HashSet<>();

    public SidecarScopeFilterGroup(String group) {
        this(group, null);
    }

    public SidecarScopeFilterGroup(String group, String param) {
        this.group = group;
        this.param = param;
    }

    public String getParam() {
        return param;
    }

    public void addPreProcessorExtractor(PreProcessorEventValueExtractor<T> func) {
        this.preProcessorExtractor = func;
    }

    public void addPostProcessorExtractor(PostProcessorEventValueExtractor<T> func) {
        this.postProcessorExtractor = func;
    }

    public void addCommonExtractor(ProcessorEventValueExtractor<T> func) {
        this.commonExtractor = func;
    }

    public void add(SidecarScopeMatchCondition<T> cond, boolean inclusive) {
        if (inclusive) {
            this.addInclusiveFilter(cond);
        } else {
            this.addExclusionFilter(cond);
        }
    }

    public void addInclusiveFilter(SidecarScopeMatchCondition<T> f) {
        inclusionFilters.add(f);
    }

    public void addExclusionFilter(SidecarScopeMatchCondition<T> f) {
        exclusionFilters.add(f);
    }

    public String getGroup() {
        return group;
    }

    public boolean isOutputMap() {
        return outputMap;
    }

    public void setOutputMap(boolean outputMap) {
        this.outputMap = outputMap;
    }

    public boolean match(PreProcessEvent ppe, SidecarScopeFilteringResult result) {
        T value = null;
        if (preProcessorExtractor != null) {
            value = preProcessorExtractor.accept(ppe, param);
        } else {
            value = commonExtractor.accept(ppe, param);
        }
        return doMatch(value, result);

    }

    protected boolean doMatch(T value, SidecarScopeFilteringResult result) {

        boolean matchResult = true;

        // If either of the exclusion filters will match, then we dont' have a
        // match on this group.
        for (SidecarScopeMatchCondition<T> excludeFilter : exclusionFilters) {
            if (excludeFilter.match(value)) {
                return false;
            }
        }

        String incLabel = null;

        if (inclusionFilters.size() > 0) {
            boolean inclMatched = false;

            for (SidecarScopeMatchCondition<T> inclusionFilter : inclusionFilters) {
                if (inclusionFilter.match(value)) {
                    incLabel = inclusionFilter.getLabel();
                    inclMatched = true;
                    break;
                }
            }

            // If none of the inclusion filters has matched, then
            // the total match result will be false.
            if (!inclMatched) {
                matchResult = false;
            }

        }

        if (matchResult) {
            if (param != null) {
                result.add(group, param, value);
                if (incLabel != null) {
                    result.add(group, param + "Label", incLabel);
                }
            } else {
                result.add(group, value);
                if (incLabel != null) {
                    result.add(group + "Label", incLabel);
                }
            }

        }

        return matchResult;
    }

    public boolean match(PostProcessEvent ppe, SidecarScopeFilteringResult result) {
        T value = null;
        if (postProcessorExtractor != null) {
            value = postProcessorExtractor.accept(ppe, param);
        } else {
            value = commonExtractor.accept(ppe, param);
        }
        return doMatch(value, result);
    }

}
