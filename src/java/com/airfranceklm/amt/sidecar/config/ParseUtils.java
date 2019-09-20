package com.airfranceklm.amt.sidecar.config;

public class ParseUtils {
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

    /**
     * Split the list using either of common separateor
     *
     * @param list separator string
     * @return A list of split values. Empty array will be returned if the <code>list</code> parameter
     * is null.
     */
    public static String[] splitValueList(String list) {
        if (list == null) {
            return new String[]{};
        }

        return list.split("[,;|]");
    }
}
