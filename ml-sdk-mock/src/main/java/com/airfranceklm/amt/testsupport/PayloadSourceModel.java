package com.airfranceklm.amt.testsupport;

import com.airfranceklm.amtml.payload.PayloadOperations;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mashery.http.io.ContentProducer;
import com.mashery.http.io.ContentSource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import static com.airfranceklm.amt.testsupport.Mocks.copyIfNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@NoArgsConstructor
@AllArgsConstructor
public class PayloadSourceModel extends RequestCaseDatum

{
    @Getter
    @Setter
    protected long payloadLength;
    @JsonProperty("payload")
    @Getter @Setter
    protected String payload;
    @JsonProperty("binary payload")
    @Getter @Setter
    protected String base64BinaryPayload;
    @Getter @Setter
    protected byte[] binaryPayload;
    @Getter @Setter
    protected Class<?> payloadOwner;
    @Getter @Setter
    protected String payloadResource;

    public PayloadSourceModel(PayloadSourceModel other) {
        deepCopyFrom(other);
    }

    public PayloadSourceModel deepCopyFrom(PayloadSourceModel another) {
        this.payloadLength = another.payloadLength;
        this.payload = another.payload;
        this.base64BinaryPayload = another.base64BinaryPayload;
        this.binaryPayload = another.binaryPayload;
        this.payloadOwner = another.payloadOwner;
        this.payloadResource = another.payloadResource;

        return this;
    }

    public void syncPayloadLength() {
        if (payload !=null) {
            payloadLength = payload.length();
        } else if (base64BinaryPayload != null) {
            payloadLength = Base64.getDecoder().decode(base64BinaryPayload).length;
        } else if (payloadOwner != null && payloadResource != null) {
            try (InputStream is = payloadOwner.getResourceAsStream(payloadResource)) {
                payloadLength = is.available();
            } catch (IOException ex) {
                fail(ex.getMessage());
            }
        }
    }

    public boolean specifiesPayload() {
        if (payloadLength <= 0) {
            syncPayloadLength();
        }

        return payloadLength > 0;
    }

    public ContentProducer producer() {
        return PayloadOperations.produceFromBinary(resolvePayload());
    }

    public ContentSource source(boolean repeatable) {
        return PayloadOperations.source(resolvePayload(), repeatable);
    }

    protected byte[] resolvePayload() {
        byte[] buf = null;
        if (payload != null) {
            buf = payload.getBytes();
        } else if (binaryPayload != null) {
            buf = binaryPayload;
        } else if (base64BinaryPayload != null) {
            buf = Base64.getDecoder().decode(base64BinaryPayload);
        } else if (payloadOwner != null && payloadResource != null) {
            InputStream is = payloadOwner.getResourceAsStream(payloadResource);
            assertNotNull(is);
            try {
                buf = PayloadOperations.getContentsOf(is);
            } catch (IOException ex) {
                fail(ex.getMessage());
            }
        }
        return buf;
    }

    public static PayloadSourceModel deepClone(PayloadSourceModel another) {
        if (another != null) {
            PayloadSourceModel retVal = new PayloadSourceModel();
            retVal.deepCopyFrom(another);
            return retVal;
        } else {
            return null;
        }
    }

    protected PayloadSourceModel inheritFrom(PayloadSourceModel another) {
        copyIfNull(this::getPayload, another::getPayload, this::setPayload);

        copyIfNull(this::getBase64BinaryPayload, another::getBase64BinaryPayload, this::setBase64BinaryPayload);
        copyIfNull(this::getBinaryPayload, another::getBinaryPayload, this::setBinaryPayload);
        copyIfNull(this::getPayloadOwner, another::getPayloadOwner, this::setPayloadOwner);
        copyIfNull(this::getPayloadResource, another::getPayloadResource, this::setPayloadResource);

        return this;
    }
}
