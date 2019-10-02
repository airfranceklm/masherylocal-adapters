package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.config.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.config.SidecarSynchronicity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


/**
 * Input for the sidecar, capturing the parameters of the call.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SidecarInput {

    private static final byte[] COLON_BYTES = stdOf(":");
    private static  final byte[] LEFT_CURLY_BRACE = stdOf("{");
    private static final byte[] RIGHT_CURLY_BRACE = stdOf("}");
    private static final byte[] GT_THEN = stdOf(">");

    private String sidecarCallId;

    // Base elements of the input.
    private SidecarInputPoint point;
    private SidecarSynchronicity synchronicity;

    private String packageKey;
    private String serviceId;
    private String endpointId;
    private Map<String, Object> params;

    // -----------------------------------------------------
    private SidecarInputHTTPMessage request;
    private SidecarInputHTTPResponseMessage response;

    private Map<String, String> eavs;
    private Map<String, String> packageKeyEAVs;

    private SidecarInputOperation operation;
    private SidecarInputToken token;
    private SidecarInputRouting routing;

    private String remoteAddress;

    public SidecarInput() {
    }

    public SidecarInputToken getToken() {
        return token;
    }

    public void setToken(SidecarInputToken token) {
        this.token = token;
    }

    public String getSidecarCallId() {
        return sidecarCallId;
    }

    public void setSidecarCallId(String sidecarCallId) {
        this.sidecarCallId = sidecarCallId;
    }

    /**
     * Returns the sha256 checksum of the fields, excluding the message Id.
     * @return Sha-256 string representing the checksum of this input object.
     */
    public String getPayloadChecksum() {
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

            return Base64.getEncoder().encodeToString(md.digest());

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA256 is not supported");
        }
    }

    static void updateChecksumOfMap(MessageDigest md, String mapName, Map<String,?> map) {
        if (map != null) {
            md.update(stdOf(mapName));
            md.update(LEFT_CURLY_BRACE);

            List<String> keys = new ArrayList<>(map.keySet());

            Collections.sort(keys);
            for (String s: keys) {
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
        return mapName.getBytes();
    }

    /**
     * Adds the EAV to the sidecar input
     *
     * @param eavName  the name of the EAV
     * @param eavValue the value of the EAV
     */
    public void addApplicationEAV(String eavName, String eavValue) {
        if (eavs == null) {
            eavs = new HashMap<>();
        }
        eavs.put(eavName, eavValue);
    }

    public void addPackageKeyEAV(String eavName, String eavValue) {
        if (packageKeyEAVs == null) {
            packageKeyEAVs = new HashMap<>();
        }
        packageKeyEAVs.put(eavName, eavValue);
    }

    void addParam(String paramName, String paramValue) {
        ensureParamsMap();
        params.put(paramName, paramValue);
    }

    void addParam(String paramName, Object paramValue) {
        ensureParamsMap();
        params.put(paramName, paramValue);
    }

    private void ensureParamsMap() {
        if (params == null) {
            params = new HashMap<>();
        }
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void addAllParams(Map<String, Object> p) {
        ensureParamsMap();
        params.putAll(p);
    }

    public SidecarInputPoint getPoint() {
        return point;
    }

    public void setPoint(SidecarInputPoint point) {
        this.point = point;
    }

    public SidecarSynchronicity getSynchronicity() {
        return synchronicity;
    }

    public void setSynchronicity(SidecarSynchronicity synchronicity) {
        this.synchronicity = synchronicity;
    }


    public String getPackageKey() {
        return packageKey;
    }

    public void setPackageKey(String packageKey) {
        this.packageKey = packageKey;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public Map<String, String> getEavs() {
        return eavs;
    }

    public void setEavs(Map<String, String> eavs) {
        this.eavs = eavs;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setParams(Map<String, ? super Object> params) {
        this.params = params;
    }

    public Map<String, String> getPackageKeyEAVs() {
        return packageKeyEAVs;
    }

    public void setPackageKeyEAVs(Map<String, String> packageKeyEAVs) {
        this.packageKeyEAVs = packageKeyEAVs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SidecarInput that = (SidecarInput) o;
        return point == that.point &&
                synchronicity == that.synchronicity &&
                Objects.equals(params, that.params) &&
                Objects.equals(request, that.request) &&
                Objects.equals(response, that.response) &&
                Objects.equals(eavs, that.eavs) &&
                Objects.equals(packageKeyEAVs, that.packageKeyEAVs) &&
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
        return Objects.hash(point, synchronicity, params, request, response, eavs,
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
        sb.append("Sidecar input: ");
        sb.append(synchronicity).append(" type at ").append(point).append(" point: ");

        if (params != null) {
            sb.append("params={").append(mapToString(params)).append("},");
        } else {
            sb.append("no params,");
        }

        if (request != null) {
            sb.append(request).append("},");
        } else {
            sb.append("no request,");
        }

        if (response != null) {
            sb.append(response).append("},");
        } else {
            sb.append("no response,");
        }

        if (eavs != null) {
            sb.append("eavs={").append(mapToString(eavs)).append("},");
        } else {
            sb.append("no eavs,");
        }

        if (packageKeyEAVs != null) {
            sb.append("packageKeyEAVs={").append(mapToString(packageKeyEAVs)).append("},");
        } else {
            sb.append("no packageKeyEAVs,");
        }

        sb.append("packageKey=").append(packageKey).append(",");

        if (operation != null) {
            sb.append("operation=").append(operation).append(",");
        } else {
            sb.append("no operation, ");
        }

        if (token != null) {
            sb.append(token);
        } else {
            sb.append("no token,");
        }

        if (routing != null) {
            sb.append(routing);
        } else {
            sb.append("no routing,");
        }

        sb.append("serviceId=").append(serviceId).append(",");
        sb.append("endpointId=").append(endpointId).append(",");
        sb.append("remoteAddr=").append(remoteAddress).append(".");

        return sb.toString();
    }

    public SidecarInputOperation getOperation() {
        return operation;
    }

    public void setOperation(SidecarInputOperation operation) {
        this.operation = operation;
    }

    public SidecarInputRouting getRouting() {
        return routing;
    }

    public void setRouting(SidecarInputRouting routing) {
        this.routing = routing;
    }

    public SidecarInputHTTPMessage getRequest() {
        return request;
    }

    public void setRequest(SidecarInputHTTPMessage request) {
        this.request = request;
    }

    public SidecarInputHTTPResponseMessage getResponse() {
        return response;
    }

    public void setResponse(SidecarInputHTTPResponseMessage response) {
        this.response = response;
    }

    @JsonIgnore
    public SidecarInputHTTPMessage getOrCreateRequest() {
        if (request == null) {
            request = new SidecarInputHTTPMessage();
        }
        return request;
    }

    @JsonIgnore
    public SidecarInputHTTPResponseMessage getOrCreateResponse() {
        if (response == null) {
            response = new SidecarInputHTTPResponseMessage();
        }
        return response;
    }

    @JsonIgnore
    public SidecarInputOperation getOrCreateOperation() {
        if (operation == null) {
            operation = new SidecarInputOperation();
        }
        return operation;
    }

    /**
     * Shrinks null nested objects if they don't have any fields filled in.
     */
    public void shrinkNullObjects() {
        if (request != null && request.containsOnlyNulls()) {
            request = null;
        }
        // TODO: This may need to be expanded on other objects as well.
    }
}
