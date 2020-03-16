package com.airfranceklm.amt.sidecar.model.alcp.alg.hspav1;

import com.airfranceklm.amt.sidecar.identity.ClusterIdentity;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Header of the request message.
 */
public class RequestMessageHeader extends MessageHeader {

    @Getter @Setter
    private String areaId;
    @Getter @Setter
    private String keyId;

    static final Pattern parsePattern = Pattern.compile(String.format("([\\w-\\+]+):([\\w-\\+]+):(%s)", MessageHeader.parsePattern.toString()));

    public RequestMessageHeader() {
        super();
    }

    public RequestMessageHeader(HighSecurityProtectionAlgorithmV1 alg, ClusterIdentity ci, byte[] transmittedData) throws IOException {
        super(alg, ci, transmittedData);
        this.areaId = ci.getAreaId();
        this.keyId = ci.getKeyId();
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s", areaId, keyId, super.toString());
    }

    void from(String other) throws IllegalMessageHeaderException {
        Matcher m = parsePattern.matcher(other);
        if (!m.matches()) {
            throw new IllegalMessageHeaderException(String.format("String %s is not a valid header", other));
        } else {
            this.areaId = m.group(1);
            this.keyId = m.group(2);
            super.from(m.group(3));
        }
    }

    public static RequestMessageHeader parse(String str) throws IllegalMessageHeaderException {
        RequestMessageHeader retVal = new RequestMessageHeader();
        retVal.from(str);

        return retVal;
    }
}
