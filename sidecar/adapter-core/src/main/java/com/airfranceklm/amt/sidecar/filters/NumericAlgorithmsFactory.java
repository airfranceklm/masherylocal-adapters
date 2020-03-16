package com.airfranceklm.amt.sidecar.filters;

import java.util.function.Predicate;

/**
 * Number predicates factory.
 */
public class NumericAlgorithmsFactory {

    private static final Predicate<Integer> httpOk = (v) -> v >= 200 && v < 400;
    private static final Predicate<Integer> httpAuth = (v) -> v == 401 || v == 403;
    private static final Predicate<Integer> httpFunctional = httpAuth.negate().and((v) -> v >= 400 && v<=499);

    private static final Predicate<Integer> httpFatal = (v) -> v >= 500 && v <= 599;

    public static Predicate<Long> gt(Long n) {
        return (v) -> {
            if (v != null) {
                return n.compareTo(v) < 0;
            } else {
                return false;
            }
        };
    }

    public static Predicate<Integer> gt(Integer n) {
        return (v) -> {
            if (v != null) {
                return n.compareTo(v) < 0;
            } else {
                return false;
            }
        };
    }

    public static Predicate<Long> eq(Long n) {
        return (v) -> {
            if (v != null) {
                return n.compareTo(v) == 0;
            } else {
                return false;
            }
        };
    }

    public static Predicate<Integer> eq(Integer n) {
        return (v) -> {
            if (v != null) {
                return v.compareTo(n) == 0;
            } else {
                return false;
            }
        };
    }

    public static Predicate<Long> gte(Long n) {
        return (v) -> {
            if (v != null) {
                return n.compareTo(v) <= 0;
            } else {
                return false;
            }
        };
    }

    public static Predicate<Integer> gte(Integer n) {
        return (v) -> {
            if (v != null) {
                return n.compareTo(v) <= 0;
            } else {
                return false;
            }
        };
    }

    public static Predicate<Long> lt(Long n) {
        return (v) -> {
            if (v != null) {
                return v.compareTo(n) < 0;
            } else {
                return false;
            }
        };
    }

    public static Predicate<Integer> lt(Integer n) {
        return (v) -> {
            if (v != null) {
                return v.compareTo(n) < 0;
            } else {
                return false;
            }
        };
    }

    public static Predicate<Long> lte(Long n) {
        return (v) -> {
            if (v != null) {
                return v.compareTo(n) <= 0;
            } else {
                return false;
            }
        };
    }

    public static Predicate<Integer> lte(Integer n) {
        return (v) -> {
            if (v != null) {
                return v.compareTo(n) <= 0;
            } else {
                return false;
            }
        };
    }

    public static Predicate<Long> range(Long from, Long to) {
        return (v) -> {
            if (v != null) {
                return v.compareTo(from) >= 0 && v.compareTo(to) <= 0;
            } else {
                return false;
            }
        };
    }

    public static Predicate<Integer> range(Integer from, Integer to) {
        return (v) -> {
            if (v != null) {
                return v.compareTo(from) >= 0 && v.compareTo(to) <= 0;
            } else {
                return false;
            }
        };
    }

    public static Predicate<Integer> httpOk() {
        return httpOk;
    }

    public static Predicate<Integer> httpAuth() {
        return httpAuth;
    }

    public static Predicate<Integer> httpFunc() {
        return httpFunctional;
    }

    public static Predicate<Integer> httpFatal() {
        return httpFatal;
    }
}
