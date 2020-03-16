package com.airfranceklm.amt.sidecar;

import lombok.NonNull;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonExpressions {

    public static String str = "\\)";
    public static final String listSeparationRegex = "(?<!\\\\),|(?<!\\\\);|(?<!\\\\)\\|";

    private static final Pattern timeReferencePattern = Pattern.compile("(\\d+(\\.\\d+)?)([sm]{1})?");

    public static String[] lowercase(String... params) {
        if (params == null) {
            return null;
        }

        String[] retVal = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            if (params[i] != null) {
                retVal[i] = params[i].toLowerCase();
            }
        }

        return retVal;
    }

    public static String toCommaSeparated(Collection<?> c) {
        if (c == null) return null;
        StringBuilder sb = new StringBuilder();
        for (Object o : c) {
            if (sb.length()> 0) sb.append(",");
            sb.append(String.valueOf(o));
        }

        return sb.toString();
    }

    /**
     * Split the list using either of common separator
     *
     * @param list separator string
     * @return A list of split values. Empty array will be returned if the <code>list</code> parameter
     * is null.
     */
    public static String[] splitStandardValueList(String list) {
        if (list == null) {
            return new String[]{};
        }

        final String[] split = list.split(listSeparationRegex);
        final String[] retVal = new String[split.length];
        for (int i=0; i<split.length; i++) {
            retVal[i] = split[i].trim().replaceAll("\\\\,", ",")
                    .replaceAll("\\\\;", ";")
                    .replaceAll("\\\\\\|", "|");
        }
        return retVal;
    }

    /**
     * Parses the timeout expression
     * @param timeoutVal expression value
     * @return 0 if the expressions is valid, or positive value if the expression is not valid
     */
    public static Integer parseShortTimeInterval(String timeoutVal) {
        Matcher m = timeReferencePattern.matcher(timeoutVal);
        if (m.matches()) {
            float f = Float.parseFloat(m.group(1));
            TimeUnit tu = TimeUnit.MILLISECONDS;
            if ("m".equals(m.group(3))) {
                tu = TimeUnit.MINUTES;
            } else if ("s".equals(m.group(3))) {
                tu = TimeUnit.SECONDS;
            }
            final float a = f * tu.toMillis(1);
            return Math.round(a);
        } else {
            return null;
        }
    }

    private static <T> Supplier<T> supplyNewInstance(Class<? extends T> clazz) {
        return () -> {
            try {
                return clazz.newInstance();
            } catch (IllegalAccessException | InstantiationException ex) {
                return null;
            }
        };
    }

    public static <T> T allocOrGet(@NonNull Supplier<T> getter, @NonNull Consumer<T> setter, @NonNull Supplier<T> creator) {
        T retVal = getter.get();
        if (retVal == null) {
            retVal = creator.get();
            setter.accept(retVal);
        }

        return retVal;
    }
}
