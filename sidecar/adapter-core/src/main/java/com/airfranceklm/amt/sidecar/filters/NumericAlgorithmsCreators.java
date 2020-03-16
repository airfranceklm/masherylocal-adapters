package com.airfranceklm.amt.sidecar.filters;

import com.airfranceklm.amt.sidecar.CommonExpressions;
import com.airfranceklm.amt.sidecar.elements.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumericAlgorithmsCreators {

    private static final Pattern rangePattern = Pattern.compile("(\\d+)\\s*\\.\\.\\s*(\\d+)");
    private static final Pattern numberOnly = Pattern.compile("(\\d+)");
    private static final Pattern ltPattern = Pattern.compile("<\\s*(\\d+)");
    private static final Pattern ltePattern = Pattern.compile("<=\\s*(\\d+)");
    private static final Pattern gtPattern = Pattern.compile(">\\s*(\\d+)");
    private static final Pattern gtePattern = Pattern.compile(">=\\s*(\\d+)");
    private static final Pattern httpCodes = Pattern.compile(String.format("%s|%s|%s|%s",
            NumericAlgorithms.HttpOk, NumericAlgorithms.HttpAuth, NumericAlgorithms.HttpFunc, NumericAlgorithms.HttpFatal));

    private static final Map<Pattern, ElementFilterCreator<Integer>> intComposite = new HashMap<>();
    private static final Map<Pattern, ElementFilterCreator<Long>> longComposite = new HashMap<>();

    static {
        intComposite.put(numberOnly, NumericAlgorithmsCreators::createEQ_Integer);
        intComposite.put(ltPattern, NumericAlgorithmsCreators::createLT_Integer);
        intComposite.put(ltePattern, NumericAlgorithmsCreators::createLTE_Integer);
        intComposite.put(gtPattern, NumericAlgorithmsCreators::createGT_Integer);
        intComposite.put(gtePattern, NumericAlgorithmsCreators::createGTE_Integer);

        longComposite.put(numberOnly, NumericAlgorithmsCreators::createEQ_Long);
        longComposite.put(ltPattern, NumericAlgorithmsCreators::createLT_Long);
        longComposite.put(ltePattern, NumericAlgorithmsCreators::createLTE_Long);
        longComposite.put(gtPattern, NumericAlgorithmsCreators::createGT_Long);
        longComposite.put(gtePattern, NumericAlgorithmsCreators::createGTE_Long);
    }

    public static void fill(ElementsFactoryBuilder b) {
        b.addFilter(NumericAlgorithms.Eq, Long.class, NumericAlgorithmsCreators::createEQ_Long);
        b.addFilter(NumericAlgorithms.Eq, Integer.class, NumericAlgorithmsCreators::createEQ_Integer);

        b.addFilter(NumericAlgorithms.Lt, Long.class, NumericAlgorithmsCreators::createLT_Long);
        b.addFilter(NumericAlgorithms.Lt, Integer.class, NumericAlgorithmsCreators::createLT_Integer);


        b.addFilter(NumericAlgorithms.Gt, Long.class, NumericAlgorithmsCreators::createGT_Long);
        b.addFilter(NumericAlgorithms.Gt, Integer.class, NumericAlgorithmsCreators::createGT_Integer);

        b.addFilter(NumericAlgorithms.Gte, Long.class, NumericAlgorithmsCreators::createGTE_Long);
        b.addFilter(NumericAlgorithms.Gte, Integer.class, NumericAlgorithmsCreators::createGTE_Integer);

        b.addFilter(NumericAlgorithms.Lte, Long.class, NumericAlgorithmsCreators::createLTE_Long);
        b.addFilter(NumericAlgorithms.Lte, Integer.class, NumericAlgorithmsCreators::createLTE_Integer);

        b.addFilter(NumericAlgorithms.Range, Long.class, NumericAlgorithmsCreators::createRange_Long);
        b.addFilter(NumericAlgorithms.Range, Integer.class, NumericAlgorithmsCreators::createRange_Int);

        b.addFilter(NumericAlgorithms.HttpOk, Integer.class, (elem, expr) -> NumericAlgorithmsFactory.httpOk());
        b.addFilter(NumericAlgorithms.HttpAuth, Integer.class, (elem, expr) -> NumericAlgorithmsFactory.httpAuth());
        b.addFilter(NumericAlgorithms.HttpFunc, Integer.class, (elem, expr) -> NumericAlgorithmsFactory.httpFunc());
        b.addFilter(NumericAlgorithms.HttpFatal, Integer.class, (elem, expr) -> NumericAlgorithmsFactory.httpFatal());

        b.addFilter(NumericAlgorithms.Composite, Long.class, NumericAlgorithmsCreators::createLongComposite);
        b.addFilter(NumericAlgorithms.Composite, Integer.class, NumericAlgorithmsCreators::createComposite);

    }

    // ------------------------------------
    // Implementation

    private static Predicate<Long> createEQ_Long(ElementSpec belongingTo, String expr) throws IllegalFilterExpressionException {
        try {
            return NumericAlgorithmsFactory.eq(Long.parseLong(expr));
        } catch (NumberFormatException ex) {
            throw new IllegalFilterExpressionException(belongingTo, ex.getMessage(), ex, expr);
        }
    }

    private static Predicate<Integer> createEQ_Integer(ElementSpec belongingTo, String expr) throws IllegalFilterExpressionException {
        try {
            final int n = Integer.parseInt(expr);
            return NumericAlgorithmsFactory.eq(n);
        } catch (NumberFormatException ex) {
            throw new IllegalFilterExpressionException(belongingTo, ex.getMessage(), ex, expr);
        }
    }

    private static Predicate<Long> createLT_Long(ElementSpec belongingTo, String expr) throws IllegalFilterExpressionException {
        try {
            return NumericAlgorithmsFactory.lt(Long.parseLong(expr));
        } catch (NumberFormatException ex) {
            throw new IllegalFilterExpressionException(belongingTo, ex.getMessage(), ex, expr);
        }
    }

    private static Predicate<Integer> createLT_Integer(ElementSpec belongingTo, String expr) throws IllegalFilterExpressionException {
        try {
            return NumericAlgorithmsFactory.lt(Integer.parseInt(expr));
        } catch (NumberFormatException ex) {
            throw new IllegalFilterExpressionException(belongingTo, ex.getMessage(), ex, expr);
        }
    }

    private static Predicate<Long> createGT_Long(ElementSpec belongingTo, String expr) throws IllegalFilterExpressionException {
        try {
            return NumericAlgorithmsFactory.gt(Long.parseLong(expr));
        } catch (NumberFormatException ex) {
            throw new IllegalFilterExpressionException(belongingTo, ex.getMessage(), ex, expr);
        }
    }

    private static Predicate<Integer> createGT_Integer(ElementSpec belongingTo, String expr) throws IllegalFilterExpressionException {
        try {
            return NumericAlgorithmsFactory.gt(Integer.parseInt(expr));
        } catch (NumberFormatException ex) {
            throw new IllegalFilterExpressionException(belongingTo, ex.getMessage(), ex, expr);
        }
    }

    private static Predicate<Long> createGTE_Long(ElementSpec belongingTo, String expr) throws IllegalFilterExpressionException {
        try {
            return NumericAlgorithmsFactory.gte(Long.parseLong(expr));
        } catch (NumberFormatException ex) {
            throw new IllegalFilterExpressionException(belongingTo, ex.getMessage(), ex, expr);
        }
    }

    private static Predicate<Integer> createGTE_Integer(ElementSpec belongingTo, String expr) throws IllegalFilterExpressionException {
        try {
            return NumericAlgorithmsFactory.gte(Integer.parseInt(expr));
        } catch (NumberFormatException ex) {
            throw new IllegalFilterExpressionException(belongingTo, ex.getMessage(), ex, expr);
        }
    }

    private static Predicate<Long> createLTE_Long(ElementSpec belongingTo, String expr) throws IllegalFilterExpressionException {
        try {
            return NumericAlgorithmsFactory.lte(Long.parseLong(expr));
        } catch (NumberFormatException ex) {
            throw new IllegalFilterExpressionException(belongingTo, ex.getMessage(), ex, expr);
        }
    }

    private static Predicate<Integer> createLTE_Integer(ElementSpec belongingTo, String expr) throws IllegalFilterExpressionException {
        try {
            return NumericAlgorithmsFactory.lte(Integer.parseInt(expr));
        } catch (NumberFormatException ex) {
            throw new IllegalFilterExpressionException(belongingTo, ex.getMessage(), ex, expr);
        }
    }

    private static Predicate<Long> createRange_Long(ElementSpec belongingTo, String expr) throws IllegalFilterExpressionException {
        if (expr == null) {
            throw new IllegalFilterExpressionException(belongingTo, "Null expression not allowed", null, null);
        }

        Matcher m = rangePattern.matcher(expr);
        if (!m.matches()) {
            throw new IllegalFilterExpressionException(belongingTo, "Expression does not represent the range", null, expr);
        }

        try {
            return NumericAlgorithmsFactory.range(Long.parseLong(m.group(1)), Long.parseLong(m.group(2)));
        } catch (NumberFormatException ex) {
            throw new IllegalFilterExpressionException(belongingTo, ex.getMessage(), ex, expr);
        }
    }

    private static Predicate<Integer> createRange_Int(ElementSpec belongingTo, String expr) throws IllegalFilterExpressionException {
        if (expr == null) {
            throw new IllegalFilterExpressionException(belongingTo, "Null expression not allowed", null, null);
        }

        Matcher m = rangePattern.matcher(expr);
        if (!m.matches()) {
            throw new IllegalFilterExpressionException(belongingTo, "Expression does not represent the range", null, expr);
        }

        try {
            return NumericAlgorithmsFactory.range(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
        } catch (NumberFormatException ex) {
            throw new IllegalFilterExpressionException(belongingTo, ex.getMessage(), ex, expr);
        }
    }

    private static Predicate<Integer> createComposite(ElementSpec belongingTo, String compositeExpr) throws IllegalFilterExpressionException {
        if (compositeExpr == null) {
            throw new IllegalFilterExpressionException(belongingTo, "Null expression not allowed for composite algorithm", null, null);
        }

        Predicate<Integer> retVal = null;
        String[] bits = CommonExpressions.splitStandardValueList(compositeExpr);

        for (String s : bits) {
            Predicate<Integer> joinee = null;
            Matcher m = rangePattern.matcher(s);

            if (m.matches()) {
                joinee = createRange_Int(belongingTo, s);
            } else {
                m = httpCodes.matcher(s);
                if (m.matches()) {
                    switch (s) {
                        case NumericAlgorithms.HttpOk:
                            joinee = NumericAlgorithmsFactory.httpOk();
                            break;
                        case NumericAlgorithms.HttpAuth:
                            joinee = NumericAlgorithmsFactory.httpAuth();
                            break;
                        case NumericAlgorithms.HttpFunc:
                            joinee = NumericAlgorithmsFactory.httpFunc();
                            break;
                        case NumericAlgorithms.HttpFatal:
                            joinee = NumericAlgorithmsFactory.httpFatal();
                            break;
                        default:
                            throw new IllegalFilterExpressionException(belongingTo, "Unknown http status group", null, s);
                    }
                } else {
                    for (Map.Entry<Pattern, ElementFilterCreator<Integer>> entry : intComposite.entrySet()) {
                        m = entry.getKey().matcher(s);
                        if (m.matches()) {
                            joinee = entry.getValue().create(belongingTo, m.group(1));
                            break;
                        }
                    }
                }
            }

            if (joinee == null) {
                throw new IllegalFilterExpressionException(belongingTo, String.format("Unknown expression '%s'", s), null, compositeExpr);
            }

            retVal = retVal == null ? joinee : retVal.or(joinee);
        }

        return retVal;
    }

    private static Predicate<Long> createLongComposite(ElementSpec belongingTo, String compositeExpr) throws IllegalFilterExpressionException {
        if (compositeExpr == null) {
            throw new IllegalFilterExpressionException(belongingTo, "Null expression not allowed for composite algorithm", null, null);
        }

        Predicate<Long> retVal = null;
        String[] bits = CommonExpressions.splitStandardValueList(compositeExpr);

        for (String s : bits) {
            Predicate<Long> joinee = null;
            Matcher m = rangePattern.matcher(s);

            if (m.matches()) {
                joinee = createRange_Long(belongingTo, s);
            } else {
                for (Map.Entry<Pattern, ElementFilterCreator<Long>> entry : longComposite.entrySet()) {
                    m = entry.getKey().matcher(s);
                    if (m.matches()) {
                        joinee = entry.getValue().create(belongingTo, m.group(1));
                        break;
                    }
                }
            }

            if (joinee == null) {
                throw new IllegalFilterExpressionException(belongingTo, String.format("Unknown expression '%s' for Long", s), null, compositeExpr);
            }

            retVal = retVal == null ? joinee : retVal.or(joinee);
        }

        return retVal;
    }
}
