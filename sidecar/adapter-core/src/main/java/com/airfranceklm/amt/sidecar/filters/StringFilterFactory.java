package com.airfranceklm.amt.sidecar.filters;

import com.airfranceklm.amt.sidecar.CommonExpressions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory for string matchers.
 */
public class StringFilterFactory {

    private static Pattern literalExpressionPattern = Pattern.compile("`(.*)(i)?`");
    private static Pattern regex = Pattern.compile("~/(.+)/(i)?");


    /**
     * Implement a filter supporting the following expressions:
     * <ul>
     *     <li>A literal filter, which must be expressed as `value` that is, having <code>`</code> leading character
     *     and <code>`</code> as the trailing character;</li>
     *     <li>A regular expression filter, starting with ~/ and ending with /</li>
     *     <li>A set filter, wherein a string is separated using common list separation {@link CommonExpressions#listSeparationRegex}</li>
     * </ul>
     */
    public static Predicate<String> createMatcher(String pExpr) {
        Matcher m = literalExpressionPattern.matcher(pExpr);
        if (m.matches()) {
            String exp = m.group(1);
            return exp::equals;
        }

        m = regex.matcher(pExpr);
        if (m.matches()) {
            Pattern p = Pattern.compile(m.group(1));
            return (v) -> {
                if (v != null) {
                    return p.matcher(v).matches();
                } else {
                    return false;
                }
            };
        }

        Set<String> set = new HashSet<>();
        Collections.addAll(set, pExpr.split(CommonExpressions.listSeparationRegex));

        return set::contains;
    }

    /**
     * A case-insensitive version of {@link #createMatcher(String)}.
     * @param pExpr expression to parse
     * @return function that will be performing the match according to the expression passed.
     */
    public static Function<String,Boolean> createCaseInsensitiveMatcher(String pExpr) {
        Matcher m = literalExpressionPattern.matcher(pExpr);
        if (m.matches()) {
            String exp = m.group(1);
            return exp::equalsIgnoreCase;
        }

        m = regex.matcher(pExpr);
        if (m.matches()) {
            Pattern p = Pattern.compile(m.group(1), Pattern.CASE_INSENSITIVE);
            return (v) -> p.matcher(v).matches();
        }

        Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Collections.addAll(set, pExpr.split(CommonExpressions.listSeparationRegex));

        return set::contains;
    }

}
