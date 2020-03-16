package com.airfranceklm.amtml.payload;

import com.mashery.http.HTTPHeaders;
import com.mashery.http.io.ContentProducer;
import com.mashery.http.io.ContentSource;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Payload operations
 */
public class PayloadOperations {

    public static ContentProducer produceFromString(String str) {
        return new ContentProducerImpl(str);
    }

    public static ContentProducer produceFromBase64String(String str) {
        return produceFromBase64String(str, UTF_8);
    }

    public static ContentProducer produceFromBase64String(String str, Charset ch) {
        return new BinaryContentProducerImpl(Base64.getDecoder().decode(str.getBytes(ch)));
    }

    public static ContentProducer produceFromBinary(byte[] data) {
        return new BinaryContentProducerImpl(data);
    }

    public static String getContentOf(ContentSource cs) throws IOException {
        return getContentOf(cs, UTF_8);
    }

    public static String getContentOf(ContentProducer cs) throws IOException {
        return getContentOf(cs, UTF_8);
    }

    public static String getContentOf(byte[] data) {
        return getContentOf(data, UTF_8);
    }

    public static String getContentOf(byte[] data, Charset cs) {
        return new String(data, cs);
    }

    /**
     * Gets the content of the stream using the specified charset
     *
     * @param cs      content source
     * @param charSet charset to use
     * @return read values of the content source
     * @throws IOException if an I/O error is thrown.
     */
    public static String getContentOf(ContentSource cs, Charset charSet) throws IOException {
        StringBuilder sb = new StringBuilder();
        Reader r = new InputStreamReader(cs.getInputStream(), charSet);
        CharBuffer cb = CharBuffer.allocate(10240);

        while (r.read(cb) > 0) {
            if (cb.length() == 0) {
                cb.rewind();
                sb.append(cb);
                cb.clear();
            }
        }

        int uPos = cb.position();
        if (uPos > 0) {
            cb.rewind();
            sb.append(cb, 0, uPos);
        }

        r.close();

        return sb.toString();
    }

    public static String getContentOf(ContentProducer cs, Charset charSet) throws IOException {
        if (cs == null || cs.getContentLength() == 0) {
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream((int)cs.getContentLength());
        cs.writeTo(baos);

        return new String(baos.toByteArray(), charSet);
    }

    public static byte[] getContentsOf(ContentSource cs) throws IOException {
        if (cs == null) {
            return null;
        } else {
            try (InputStream is = cs.getInputStream()) {
                return getContentsOf(is);
            }
        }
    }

    public static byte[] getContentsOf(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[10240];

        int k = 0;
        while ((k = is.read(buf)) > 0) {
            baos.write(buf, 0, k);
        }

        return baos.toByteArray();
    }

    /**
     * Returns the content of the stream as a Base-64 encoded array
     *
     * @param cs a non-null content source
     * @return Base64 representation of this array.
     * @throws IOException if an i/o error reading the data will occur.
     */
    public static String getBase64ContentOf(ContentSource cs) throws IOException {
        byte[] data = getContentsOf(cs);
        return getBase64Of(data);
    }

    public static String getBase64Of(byte[] data) {
        if (data == null) {
            return null;
        } else {
            return Base64.getEncoder().encodeToString(data);
        }
    }

    public static ContentSource source(String str) {
        final byte[] buf = str != null ? str.getBytes() : null;
        return source(buf);
    }

    public static ContentSource source(byte[] buf) {
        return new ContentSourceImpl(buf);
    }

    public static ContentSource source(byte[] buf, boolean isRepeatable) {
        return new ContentSourceImpl(buf, isRepeatable);
    }

    public static ContentSource source(InputStream is) {
        return new ContentSourceISImpl(is);
    }

    static String retrieveHeader(HTTPHeaders h, String val) {
        if (h != null) {
            String retVal = h.get(val);
            if (retVal == null) {
                retVal = h.get(val.toLowerCase());
            }

            return retVal;
        } else {
            return null;
        }
    }

    public static byte[] bodyContentIfJson(HTTPHeaders headers, ContentSource body) throws IOException {
        if (headers != null && body != null && bearsJson(headers, body)) {
            return getContentsOf(body);
        } else {
            return null;
        }
    }

    public static boolean bearsJson(HTTPHeaders headers, ContentSource body) {
        return isJsonMimeType(retrieveHeader(headers, "Content-Type"))
                && body.getContentLength() > 0;
    }

    static boolean isJsonMimeType(String pType) {
        if (pType == null) {
            return false;
        }
        String type = pType.toLowerCase();

        return type.startsWith("application/json")
                || type.startsWith("application/javascript")
                || type.equals("application/ld+json")
                || type.equals("application/vnd.api+json");
    }

    public static boolean isTextMimeType(String pType) {
        if (pType == null) return false;

        if (isJsonMimeType(pType)) {
            return true;
        }

        String type = pType.toLowerCase();

        if (type.startsWith("text/")) {
            return true;
        } else if (type.startsWith("application/yaml") || type.startsWith("application/x-yaml")) {
            return true;
        } else if (type.equals("application/x-www-form-urlencoded")) {
            return true;
        } else if (type.startsWith("application/xml") || type.startsWith("application/xhtml")) {
            return true;
        } else if (type.startsWith("application/graphql")) {
            return true;
        }

        return false;
    }
}
