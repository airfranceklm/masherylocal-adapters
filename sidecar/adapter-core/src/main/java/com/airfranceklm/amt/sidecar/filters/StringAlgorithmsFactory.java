package com.airfranceklm.amt.sidecar.filters;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Collection of string algorithms
 */
public class StringAlgorithmsFactory {

    private static final Pattern jsonPattern = Pattern.compile("application/json((.*)?(;\\s*charset\\s*=.*)?)?");

    private static Predicate<String> json = (v) -> v != null && jsonPattern.matcher(v).matches();

    private static Predicate<String> absent = (v) -> v == null || v.trim().length() == 0;
    private static Predicate<String> present = absent.negate();

    public static Predicate<String> stringPresent() {
        return present;
    }

    public static Predicate<String> stringAbsent() {
        return absent;
    }

    public static Predicate<String> json() {
        return json;
    }

    public static Predicate<String> eq(String req, boolean caseSensitive) {
        if (caseSensitive) {
            if (req == null) {
                return Objects::isNull;
            } else return (v) -> v != null && Objects.equals(v, req);
        } else {
            if (req == null) {
                return Objects::isNull;
            }
            return req::equalsIgnoreCase;
        }
    }

    public static Predicate<String> regex(String rx, boolean caseSensitive) {
        Pattern p = caseSensitive ? Pattern.compile(rx) : Pattern.compile(rx, Pattern.CASE_INSENSITIVE);
        return (v) -> v != null && p.matcher(v).matches();
    }

    public static Predicate<String> inSet(Collection<String> in, boolean caseSensitive) {
        if (caseSensitive) {
            Set<String> set = new HashSet<>(in.size());
            set.addAll(in);
            return set::contains;
        } else {
            Set<String> set = new HashSet<>(in.size());
            in.forEach((s) -> set.add(s.toLowerCase()));

            return (v) -> {
                if (v == null) {
                    return set.contains(null);
                } else {
                    return set.contains(v.toLowerCase());
                }
            };
        }
    }

    public static Predicate<String> inSet(boolean caseSensitive, String... elements) {
        if (caseSensitive) {
            Set<String> set = new HashSet<>(elements.length);
            Collections.addAll(set, elements);
            return set::contains;
        } else {
            Set<String> set = new HashSet<>(elements.length);
            for (String s: elements) {
                set.add(s == null ? null : s.toLowerCase());
            }

            return (v) -> {
                if (v == null) {
                    return set.contains(null);
                } else {
                    return set.contains(v.toLowerCase());
                }
            };
        }
    }


}
