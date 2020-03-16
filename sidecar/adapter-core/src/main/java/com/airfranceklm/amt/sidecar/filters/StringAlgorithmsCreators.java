package com.airfranceklm.amt.sidecar.filters;

import com.airfranceklm.amt.sidecar.CommonExpressions;
import com.airfranceklm.amt.sidecar.elements.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StringAlgorithmsCreators {

    private static Pattern literalExpressionPattern = Pattern.compile("`(?!\\[)(.*)`(i)?");
    private static Pattern setExpressionPattern = Pattern.compile("`\\[(.*)]`(i)?");
    private static Pattern regex = Pattern.compile("~/(.+)/(i)?");

    public static void fill(ElementsFactoryBuilder b) {
        b.addStringFilter(StringFilterAlgorithms.Empty, StringAlgorithmsFactory.stringAbsent());
        b.addStringFilter(StringFilterAlgorithms.NonEmpty, StringAlgorithmsFactory.stringPresent());
        b.addStringFilter(StringFilterAlgorithms.Json, StringAlgorithmsFactory.json());

        b.addStringFilter(StringFilterAlgorithms.Eq, (owner, expr) -> StringAlgorithmsFactory.eq(expr, true));
        b.addStringFilter(StringFilterAlgorithms.EqI, (owner, expr) -> StringAlgorithmsFactory.eq(expr, false));

        b.addStringFilter(StringFilterAlgorithms.Regex, regex());
        b.addStringFilter(StringFilterAlgorithms.RegexI, regex_i());

        b.addStringFilter(StringFilterAlgorithms.OneOf, oneof());
        b.addStringFilter(StringFilterAlgorithms.OneOfI, oneof_i());

        b.addStringFilter(StringFilterAlgorithms.DslExpression, dslExpression());
    }

    private static ElementFilterCreator<String> regex() {
        return (owner, expr) -> {
            if (expr == null) {
                throw new IllegalFilterExpressionException(owner, "Regex requires a non-null expression", null, null);
            }

            try {
                return StringAlgorithmsFactory.regex(expr, true);
            } catch (PatternSyntaxException ex) {
                throw new IllegalFilterExpressionException(owner,
                        String.format("Expression '%s' is not a valid regexp", expr),
                        ex,
                        expr
                );
            }
        };
    }

    private static ElementFilterCreator<String> regex_i() {
        return (owner, expr) -> StringAlgorithmsFactory.regex(expr, false);
    }

    private static ElementFilterCreator<String> oneof() {
        return (owner, expr) -> {
            String[] elems = CommonExpressions.splitStandardValueList(expr);
            return StringAlgorithmsFactory.inSet(true, elems);
        };
    }

    private static ElementFilterCreator<String> oneof_i() {
        return (owner, expr) -> {
            String[] elems = CommonExpressions.splitStandardValueList(expr);
            return StringAlgorithmsFactory.inSet(false, elems);
        };
    }

    public static ElementFilterCreator<String> dslExpression() {
        return (owner, pExpr) -> {
            if (pExpr == null) {
                throw new IllegalFilterExpressionException(owner, "DslExpression requires a string expression", null, null);
            }

            Matcher m = literalExpressionPattern.matcher(pExpr);
            if (m.matches()) {
                String expr = m.group(1);
                boolean caseSensitive = m.group(2) == null;

                return StringAlgorithmsFactory.eq(expr, caseSensitive);
            }

            m = regex.matcher(pExpr);
            if (m.matches()) {
                String expr = m.group(1);
                boolean caseSensitive = m.group(2) == null;

                return StringAlgorithmsFactory.regex(expr, caseSensitive);
            }

            String setExpr = pExpr;
            boolean caseSensitive = true;

            m = setExpressionPattern.matcher(pExpr);
            if (m.matches()) {
                setExpr = m.group(1);
                caseSensitive = m.group(2) == null;
            }

            return StringAlgorithmsFactory.inSet(caseSensitive, CommonExpressions.splitStandardValueList(setExpr));
        };
    }
}
