package com.airfranceklm.amt.sidecar.elements;

import java.util.function.Function;

public class DataElementFilters {

    private static Function<Boolean, DataElementRelevance> matchInvoke = (v) -> {
        return v ? DataElementRelevance.Invoke : DataElementRelevance.Ignored;
    };

    private static Function<Boolean, DataElementRelevance> matchNoop = (v) -> {
        return v ? DataElementRelevance.Noop : DataElementRelevance.Ignored;
    };

    private static Function<Boolean, DataElementRelevance> matchRequired = (v) -> {
        return v ? DataElementRelevance.Invoke : DataElementRelevance.ClientError;
    };

    private static Function<Boolean, DataElementRelevance> matchProhibited = (v) -> {
        return v ? DataElementRelevance.ClientError : DataElementRelevance.Invoke;
    };

    private static Function<Boolean, DataElementRelevance> internalFault = (v) -> {
        return DataElementRelevance.InternalFault;
    };

    public static <T> Function<T, DataElementRelevance> translateIntent(Function<T,Boolean> f, DataElementFilterIntent intent) {
        switch (intent) {
            case MatchScopes:
                return f.andThen(matchInvoke);
            case MatchDescopes:
                return f.andThen(matchNoop);
            case MatchRequired:
                return f.andThen(matchRequired);
            case MatchProhibits:
                return f.andThen(matchProhibited);
            default:
                return f.andThen(internalFault);
        }
    }
}
