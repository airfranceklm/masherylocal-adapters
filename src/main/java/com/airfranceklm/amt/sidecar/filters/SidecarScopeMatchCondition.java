package com.airfranceklm.amt.sidecar.filters;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lambda function scope filter, providing easy pluggability of examining the data elements
 * of the request.
 */
public class SidecarScopeMatchCondition<T>  {

    private String label;

    private Pattern matchPattern;
    private Set<T> matchSet;
    private boolean nullMatches = false;

    public SidecarScopeMatchCondition(String label) {
        this.label = label;
    }

    public void setNullMatcher(boolean how) {
        nullMatches = how;
    }

    public String getLabel() {
        return label;
    }


    public Pattern getMatchPattern() {
        return matchPattern;
    }

    public void setMatchPattern(Pattern matchPattern) {
        this.matchPattern = matchPattern;
    }

    public Set<T> getMatchSet() {
        return matchSet;
    }

    public void setMatchSet(Set<T> matchSet) {
        this.matchSet = matchSet;
    }


    public boolean match(T value) {

        boolean match = false;

        if (value == null) {
            if (nullMatches) {
                match = true;
            }
        } else {
            if (matchSet != null && matchSet.contains(value)) {
                match = true;
            } else if (matchPattern != null) {
                Matcher m = matchPattern.matcher(value.toString());
                match = m.matches();
            }

        }

        return match;
    }

}
