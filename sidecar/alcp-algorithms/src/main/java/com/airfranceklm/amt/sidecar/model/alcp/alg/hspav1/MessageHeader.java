package com.airfranceklm.amt.sidecar.model.alcp.alg.hspav1;

import com.airfranceklm.amt.sidecar.model.alcp.ALCPAlgorithm;
import com.airfranceklm.amt.sidecar.identity.PartyIdentity;
import lombok.Getter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageHeader {

    private static final String shortHeaderFormat = "%d:%s";

    static final Pattern parsePattern = Pattern.compile("(\\d+):([A-Za-z0-9\\\\+/=]+)");

    @Getter
    long epochSeconds;
    @Getter
    byte[] authenticationSignatureBytes;
    @Getter
    String authSignature;

    MessageHeader() {
    }

    /**
     * Constructor that should be used by the application class to derive
     * @param ci cluster identify of the message
     * @throws IOException if the header cannot be parsed
     */
    public MessageHeader(HighSecurityProtectionAlgorithmV1 alg, PartyIdentity ci, byte[] transmittedData) throws IOException {
        this.epochSeconds = alg.epochSecondsNow();

        authenticationSignatureBytes = alg.sign(ci,
                String.valueOf(this.epochSeconds).getBytes(),
                transmittedData);
        authSignature = ALCPAlgorithm.toBase64(authenticationSignatureBytes);
    }

    @Override
    public String toString() {
        return String.format(shortHeaderFormat, epochSeconds, authSignature);
    }

    void from(String other) throws IllegalMessageHeaderException {
        Matcher m = parsePattern.matcher(other);
        if (!m.matches()) {
            throw new IllegalMessageHeaderException(String.format("String %s is not a valid header", other));
        } else {
            this.epochSeconds = Long.parseLong(m.group(1));
            this.authSignature = m.group(2);

            // Support for the null raw signatures.
            if (!"-".equalsIgnoreCase(this.authSignature)) {
                this.authenticationSignatureBytes = ALCPAlgorithm.fromBase64(this.authSignature);
            }
        }
    }

    public static MessageHeader parse(String str) throws IllegalMessageHeaderException {
        MessageHeader retVal = new MessageHeader();
        retVal.from(str);

        return retVal;
    }
}
