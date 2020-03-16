package com.airfranceklm.amt.sidecar.model.alcp.alg.hspav1;

import com.airfranceklm.amt.sidecar.model.alcp.HighSecurityAlgorithmV1Options;
import com.airfranceklm.amt.sidecar.model.alcp.AlgorithmActivation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Mapper that will apply the defined options.
 */
class OptionsMapper {

    private static Map<HighSecurityAlgorithmV1Options, BiConsumer<Object, HighSecurityProtectionAlgorithmV1>> mapper;

    static {
        mapper = new HashMap<>();
        mapper.put(HighSecurityAlgorithmV1Options.RSACipher, OptionsMapper::setRSACipher);
        mapper.put(HighSecurityAlgorithmV1Options.AESCipher, OptionsMapper::setAESCipher);
        mapper.put(HighSecurityAlgorithmV1Options.SigningCipher, OptionsMapper::setSigningCipher);
        mapper.put(HighSecurityAlgorithmV1Options.IVInferenceDigestAlgorithm, OptionsMapper::setIVInferenceAlgorithm);
        mapper.put(HighSecurityAlgorithmV1Options.AESVariablePasswordLength, OptionsMapper::setAESVariablePasswordLength);
        mapper.put(HighSecurityAlgorithmV1Options.MaximumClockInaccuracy, OptionsMapper::setMaxTimestampDifference);
        mapper.put(HighSecurityAlgorithmV1Options.GCMMode, OptionsMapper::useGCMModel);
    }

    private static void setRSACipher(Object obj, HighSecurityProtectionAlgorithmV1 retVal) {
        retVal.setRsaCipher(asString(obj));
    }

    private static void setAESCipher(Object obj, HighSecurityProtectionAlgorithmV1 retVal) {
        retVal.setAesCipher(asString(obj));
    }

    private static void setSigningCipher(Object obj, HighSecurityProtectionAlgorithmV1 retVal) {
        retVal.setAesCipher(asString(obj));
    }

    private static void setIVInferenceAlgorithm(Object obj, HighSecurityProtectionAlgorithmV1 retVal) {
        retVal.setIVInferenceDigestAlgorithm(asString(obj));
    }

    private static void setAESVariablePasswordLength(Object obj, HighSecurityProtectionAlgorithmV1 retVal) {
        retVal.setAesVariablePasswordLength(asInteger(obj));
    }

    private static void setMaxTimestampDifference(Object obj, HighSecurityProtectionAlgorithmV1 retVal) {
        retVal.setAesVariablePasswordLength(asInteger(obj));
    }

    private static void useGCMModel(Object obj, HighSecurityProtectionAlgorithmV1 retVal) {
        retVal.setGCMMode();
    }

    private static String asString(Object o) {
        if (o instanceof String) {
            return (String) o;
        } else {
            throw new IllegalArgumentException("Parameter must be a string");
        }
    }

    private static Integer asInteger(Object o) {
        if (o instanceof Integer) {
            return (Integer) o;
        } else {
            throw new IllegalArgumentException("Parameter must be an integer");
        }
    }

    public static HighSecurityProtectionAlgorithmV1 fromOptions(AlgorithmActivation actv, Map<String, Object> opts) {
        HighSecurityProtectionAlgorithmV1 retVal = new HighSecurityProtectionAlgorithmV1(actv);

        if (opts != null) {
            for (Map.Entry<String, Object> e : opts.entrySet()) {
                HighSecurityAlgorithmV1Options opt = HighSecurityAlgorithmV1Options.valueOf(e.getKey());
                BiConsumer<Object, HighSecurityProtectionAlgorithmV1> bc = mapper.get(opt);
                if (bc != null) {
                    bc.accept(e.getValue(), retVal);
                } else {
                    throw new IllegalArgumentException(String.format("Option %s is not understood", e.getKey()));
                }
            }
        }

        return retVal;
    }
}
