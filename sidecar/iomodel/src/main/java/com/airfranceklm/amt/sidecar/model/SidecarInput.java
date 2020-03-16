package com.airfranceklm.amt.sidecar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;

import static com.airfranceklm.amt.sidecar.CommonExpressions.allocOrGet;


/**
 * Input for the sidecar, capturing the parameters of the call.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder=true, builderMethodName = "buildSidecarInput")
public class SidecarInput {

    private static final byte[] COLON_BYTES = stdOf(":");
    private static final byte[] LEFT_CURLY_BRACE = stdOf("{");
    private static final byte[] RIGHT_CURLY_BRACE = stdOf("}");
    private static final byte[] GT_THEN = stdOf(">");
    private static final byte[] EMPTY_BYTES = {};

    @Getter
    @Setter
    private String masheryMessageId;

    // Base elements of the input.
    @Getter
    @Setter
    private SidecarInputPoint point;
    @Getter
    @Setter
    private SidecarSynchronicity synchronicity;
    @Getter
    @Setter
    private String packageKey;
    @Getter
    @Setter
    private String serviceId;
    @Getter
    @Setter
    private String endpointId;
    @Getter @Setter @Singular
    private Map<String, Object> params;


    /**
     * Location to store the extended elements, to remove the need to modify the
     * input structure
     */
    @JsonProperty("ext")
    @Getter
    @Setter @Singular
    private Map<String, Object> extendedElements;

    // -----------------------------------------------------
    @Getter
    @Setter
    private SidecarInputHTTPMessage request;
    @Getter
    @Setter
    private SidecarInputHTTPResponseMessage response;
    @Getter
    @Setter @Singular
    private Map<String, String> eavs;
    @Getter
    @Setter @Singular
    private Map<String, String> packageKeyEAVs;
    @Getter
    @Setter
    private SidecarInputOperation operation;
    @Getter
    @Setter
    private SidecarInputToken token;
    @Getter
    @Setter
    private SidecarInputRouting routing;
    @Getter
    @Setter
    private String remoteAddress;

    /**
     * Returns the sha256 checksum of the fields, excluding the message Id and timestamp.
     *
     * @return Sha-256 string representing the checksum of this input object.
     */
    @JsonIgnore
    public String getInputChecksum() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            if (remoteAddress != null) {
                md.update(stdOf(remoteAddress));
            }

            md.update(COLON_BYTES);
            md.update(stdOf(serviceId));
            md.update(COLON_BYTES);
            md.update(stdOf(endpointId));
            md.update(COLON_BYTES);
            md.update(stdOf(packageKey));
            md.update(COLON_BYTES);

            if (point != null) {
                md.update(stdOf(point.name()));
            }
            md.update(COLON_BYTES);

            if (synchronicity != null) {
                md.update(stdOf(synchronicity.name()));
            }
            md.update(COLON_BYTES);

            updateChecksumOfMap(md, "pkEav", packageKeyEAVs);
            md.update(COLON_BYTES);

            updateChecksumOfMap(md, "appEav", eavs);
            md.update(COLON_BYTES);

            updateChecksumOfMap(md, "params", params);
            md.update(COLON_BYTES);

            updateChecksumOfMap(md, "ext", extendedElements);
            md.update(COLON_BYTES);

            if (request != null) {
                request.updateChecksum(md);
            }
            md.update(COLON_BYTES);

            if (operation != null) {
                operation.updateChecksum(md);
            }
            md.update(COLON_BYTES);

            if (token != null) {
                token.updateChecksum(md);
            }
            md.update(COLON_BYTES);

            if (routing != null) {
                routing.updateChecksum(md);
            }
            md.update(COLON_BYTES);

            if (response != null) {
                response.updateChecksum(md);
            }

            return Hex.encodeHexString(md.digest(), true);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA256 is not supported");
        }
    }


    static void updateChecksumOfMap(MessageDigest md, String mapName, Map<String, ?> map) {
        if (map != null) {
            md.update(stdOf(mapName));
            md.update(LEFT_CURLY_BRACE);

            List<String> keys = new ArrayList<>(map.keySet());

            Collections.sort(keys);
            for (String s : keys) {
                md.update(utf8Of(s));

                Object val = map.get(s);
                if (val != null) {
                    md.update(utf8Of("=" + ""));
                    md.update(utf8Of(val.toString()));
                }

            }

            md.update(RIGHT_CURLY_BRACE);
        }
    }

    static byte[] utf8Of(String mapName) {
        return mapName.getBytes(StandardCharsets.UTF_8);
    }

    static void updateUTF8Nullable(MessageDigest md, String nullableString) {
        if (nullableString != null) {
            md.update(utf8Of(nullableString));
            md.update(GT_THEN);
        }
    }

    static void updateNullable(MessageDigest md, String nullableString) {
        if (nullableString != null) {
            md.update(stdOf(nullableString));
            md.update(GT_THEN);
        }
    }

    static void updateRedirect(MessageDigest md) {
        md.update(GT_THEN);
    }

    static byte[] stdOf(String mapName) {
        if (mapName == null) {
            return EMPTY_BYTES;
        }
        return mapName.getBytes();
    }

    static <K, V> boolean nullOrEmptyMap(Map<K,V> l1) {
        return l1 == null || l1.size() == 0;
    }

    static <K, V> boolean equalNullOrEmptyMap(Map<K,V> m1, Map<K, V> m2) {
        if (m1 == null && m2 == null) {
            return true;
         } else if (nullOrEmptyMap(m1) && nullOrEmptyMap(m2)) {
            return true;
        } else {
            return Objects.equals(m1, m2);
        }


    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SidecarInput that = (SidecarInput) o;
        return Objects.equals(masheryMessageId, that.masheryMessageId) &&
                point == that.point &&
                synchronicity == that.synchronicity &&
                equalNullOrEmptyMap(params, that.params) &&
                equalNullOrEmptyMap(extendedElements, that.extendedElements) &&
                Objects.equals(request, that.request) &&
                Objects.equals(response, that.response) &&
                equalNullOrEmptyMap(eavs, that.eavs) &&
                equalNullOrEmptyMap(packageKeyEAVs, that.packageKeyEAVs) &&
                Objects.equals(packageKey, that.packageKey) &&
                Objects.equals(operation, that.operation) &&
                Objects.equals(token, that.token) &&
                Objects.equals(routing, that.routing) &&
                Objects.equals(serviceId, that.serviceId) &&
                Objects.equals(endpointId, that.endpointId) &&
                Objects.equals(remoteAddress, that.remoteAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, synchronicity, params, extendedElements, request, response, eavs,
                packageKeyEAVs, packageKey, operation, token, routing,
                serviceId,
                endpointId, remoteAddress);
    }

    static String mapToString(Map<String, ?> map) {
        if (map == null) {
            return "null-map";
        }
        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (String str : keys) {
            sb.append(str).append("=").append(map.get(str)).append(";");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Sidecar input").append("@").append(masheryMessageId).append(":\n");
        sb.append("-: ").append(synchronicity).append(" type at ").append(point).append(" point:\n");

        if (params != null) {
            sb.append("-: params={").append(mapToString(params)).append("},\n");
        } else {
            sb.append("- no params,\n");
        }

        if (request != null) {
            sb.append("-:").append(request).append("},\n");
        } else {
            sb.append("- no request,\n");
        }

        if (response != null) {
            sb.append("-:").append(response).append("},\n");
        } else {
            sb.append("- no response,\n");
        }

        if (eavs != null) {
            sb.append("- eavs={").append(mapToString(eavs)).append("},\n");
        } else {
            sb.append("- no eavs,");
        }

        if (packageKeyEAVs != null) {
            sb.append("- packageKeyEAVs={").append(mapToString(packageKeyEAVs)).append("},\n");
        } else {
            sb.append("- no packageKeyEAVs,\n");
        }

        sb.append("- packageKey=").append(packageKey).append(",\n");

        if (operation != null) {
            sb.append("- operation=").append(operation).append(",\n");
        } else {
            sb.append("-no operation,\n");
        }

        if (token != null) {
            sb.append("-:").append(token);
        } else {
            sb.append("- no token,\n");
        }

        if (routing != null) {
            sb.append("-:").append(routing);
        } else {
            sb.append("- no routing,\n");
        }

        sb.append("- serviceId=").append(serviceId).append(",\n");
        sb.append("- endpointId=").append(endpointId).append(",\n");
        sb.append("- remoteAddr=").append(remoteAddress);

        return sb.toString();
    }



    @JsonIgnore
    @SuppressWarnings("unchecked")
    public Map<String, ?> getParameterGroup(String name) {
        if (params != null) {
            return (Map<String, ?>) params.get(name);
        }
        return null;
    }

    /**
     * Shrinks null nested objects if they don't have any fields filled in.
     */
    public void shrinkNullObjects() {
        if (request != null && request.containsOnlyNulls()) {
            request = null;
        }
        if (response != null && response.containsOnlyNulls()) {
            response = null;
        }

        // TODO: This may need to be expanded on other objects as well.
    }

    public String explainDifferenceFrom(SidecarInput another) {
        StringBuilder sb = new StringBuilder();

        reportDifference(sb, "masheryMessageId", another, SidecarInput::getMasheryMessageId);
        reportDifference(sb, "point", another, SidecarInput::getPoint);
        reportDifference(sb, "synchronicity", another, SidecarInput::getSynchronicity);
        reportDifference(sb, "packageKey", another, SidecarInput::getPackageKey);
        reportDifference(sb, "serviceId", another, SidecarInput::getServiceId);
        reportDifference(sb, "endpointId", another, SidecarInput::getEndpointId);
        reportDifference(sb, "params", another, SidecarInput::getParams);
        reportDifference(sb, "ext", another, SidecarInput::getExtendedElements);
        reportDifference(sb, "request", another, SidecarInput::getRequest);
        reportDifference(sb, "response", another, SidecarInput::getResponse);
        reportDifference(sb, "eavs", another, SidecarInput::getEavs);
        reportDifference(sb, "packageKeyEAVs", another, SidecarInput::getPackageKeyEAVs);
        reportDifference(sb, "operation", another, SidecarInput::getOperation);
        reportDifference(sb, "token", another, SidecarInput::getToken);
        reportDifference(sb, "routing", another, SidecarInput::getRouting);
        reportDifference(sb, "remoteAddress", another, SidecarInput::getRemoteAddress);

        return sb.toString();
    }

    private void reportDifference(StringBuilder sb, String key, SidecarInput rightInput, Function<SidecarInput, Object> extractor) {
        Object left = extractor.apply(this);
        Object right = extractor.apply(rightInput);

        if (left == right) {
            return;
        }

        if (!Objects.equals(left, right)) {
            sb.append(key).append(":\n")
                    .append("Expect:> ").append(left).append("\n")
                    .append("Actual:> ").append(right).append("\n");
        }
    }

    public static class Accessor {

        @SuppressWarnings("unchecked")
        public static Map<String, Object> allocOrGetParameterGroup(SidecarInput si, String name) {
            final Object group = allocOrGetParams(si).computeIfAbsent(name, (n) -> new HashMap<String, Object>());

            if (group instanceof Map) {
                return (Map<String, Object>) group;
            } else {
                return null;
            }
        }

        public static Map<String, String> allocOrGetEAVs(SidecarInput si) {
            return allocOrGet(si::getEavs, si::setEavs, LinkedHashMap::new);
        }

        public static Map<String, String> allocOrGetPackageKeyEAVs(SidecarInput si) {
            return allocOrGet(si::getPackageKeyEAVs, si::setPackageKeyEAVs, LinkedHashMap::new);
        }

        public static SidecarInputOperation allocOrGetOperation(SidecarInput si) {
            return allocOrGet(si::getOperation, si::setOperation, SidecarInputOperation::new);
        }

        public static SidecarInputRouting allocOrGetRouting(SidecarInput si) {
            return allocOrGet(si::getRouting, si::setRouting, SidecarInputRouting::new);
        }

        public static SidecarInputToken allocOrGetToken(SidecarInput si) {
            return allocOrGet(si::getToken, si::setToken, SidecarInputToken::new);
        }

        public static SidecarInputHTTPMessage allocOrGetRequest(SidecarInput si) {
            return allocOrGet(si::getRequest, si::setRequest, SidecarInputHTTPMessage::new);
        }

        public static Map<String,Object> allocOrGetPayloadFragments(SidecarInputHTTPMessage msg) {
            return allocOrGet(msg::getPayloadFragments, msg::setPayloadFragments, LinkedHashMap::new);
        }

        public static SidecarInputHTTPResponseMessage allocOrGetResponse(SidecarInput si) {
            return allocOrGet(si::getResponse, si::setResponse, () -> new SidecarInputHTTPResponseMessage(200));
        }

        public static Map<String, Object> allocOrGetParams(SidecarInput si) {
            return allocOrGet(si::getParams, si::setParams, LinkedHashMap::new);
        }

        public static SidecarInputHTTPResponseMessage allocOrGetResponse(SidecarInput si, int code) {
            return allocOrGet(() -> {
                final SidecarInputHTTPResponseMessage resp = si.getResponse();
                if (resp != null) {
                    if (resp.getStatusCode() != code) {
                        resp.setStatusCode(code);
                    }
                    return resp;
                } else {
                    return null;
                }
            }, si::setResponse, () -> new SidecarInputHTTPResponseMessage(code));
        }

        public static SidecarInput deepClone(SidecarInput si) {
            if (si == null) {
                return null;
            }

            SidecarInput.SidecarInputBuilder bld = si.toBuilder();
            if (si.getRequest() != null) {
                bld.request(si.getRequest().deepClone());
            }
            if (si.getResponse() != null) {
                bld.response(si.getResponse().deepClone());
            }
            if (si.getOperation() != null) {
                bld.operation(si.getOperation().toBuilder().build());
            }
            if (si.getToken() != null) {
                bld.token(si.getToken().toBuilder().build());
            }
            if (si.getRouting() != null) {
                bld.routing(si.getRouting().toBuilder().build());
            }

            return bld.build();
        }

    }
}
