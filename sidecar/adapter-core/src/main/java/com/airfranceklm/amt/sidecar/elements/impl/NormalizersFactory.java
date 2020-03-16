package com.airfranceklm.amt.sidecar.elements.impl;

import com.airfranceklm.amt.sidecar.JsonHelper;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.elements.ElementsFactoryBuilder;
import com.airfranceklm.amt.sidecar.elements.StandardNormalizers;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class NormalizersFactory {

    private static String lowercase(String input) {
        return input != null ? input.toLowerCase() : null;
    }

    private static String sha256(String input) {
        if (input != null) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(input.getBytes());

                return Hex.encodeHexString(md.digest());
            } catch (NoSuchAlgorithmException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    private static String jsonStringify(Object o) {
        return JsonHelper.toJSON(o);
    }

    public static void fill(ElementsFactoryBuilder b) {
        b.addNormalizer(StandardNormalizers.Lowercase.name(), NormalizersFactory::lowercase);
        b.addNormalizer(StandardNormalizers.Sha256.name(), NormalizersFactory::sha256);
        b.addNormalizer(StandardNormalizers.JsonStringify.name(), NormalizersFactory::jsonStringify);
    }
}
