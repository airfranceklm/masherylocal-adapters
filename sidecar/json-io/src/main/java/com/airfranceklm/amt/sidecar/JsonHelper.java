package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import com.airfranceklm.amt.sidecar.model.alcp.EncryptedMessage;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.*;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Collection of helper static methods to work with Jackson.
 */
@Slf4j
public class JsonHelper {

    private static final int GZIP_FOR_TRANSPORT_THRESHOLD = 512;

    private static DefaultJsonUmarshaller defaultUnmarshaller;

    static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
    }

    private static ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

    private static DateTimeFormatter jsonFormat = ISODateTimeFormat.dateTimeParser();

    public static <T> T convertValue(Object from, Class<T> to) {
        return objectMapper.convertValue(from, to);
    }

    public static String writeValueAsString(Object v) throws JsonProcessingException {
        return objectMapper.writeValueAsString(v);
    }

    /**
     * Parses the JSON date
     *
     * @param date date to parse
     * @return instance of the parsed date.
     */
    public static Date parseJSONDate(String date) {
        return jsonFormat.parseDateTime(date).toDate();
    }

    /**
     * Helper method to convert a well-formed JSON string into a general-purpose map.
     *
     * @param json JSON string
     * @return converted map.
     */
    public static JsonNode parse(String json) {
        if (json == null) {
            return null;
        }

        try {
            return objectMapper.readValue(json, JsonNode.class);
        } catch (IOException ex) {
            return null;
        }
    }

    public static <T> T parse(String json, Class<T> clazz) throws IOException {
        if (json == null) {
            return null;
        }

        return objectMapper.readValue(json, clazz);
    }

    public static <T> T parse(File json, Class<T> clazz) throws IOException {
        if (json == null) {
            return null;
        }

        return objectMapper.readValue(json, clazz);
    }

    public static <T> T parse(InputStream json, Class<T> clazz) throws IOException {
        if (json == null) {
            return null;
        }

        return objectMapper.readValue(json, clazz);
    }

    public static ObjectNode readTree(String str) {
        try {
            return (ObjectNode) objectMapper.readTree(str);
        } catch (IOException ex) {
            return null;
        }
    }

    public static SidecarInput toSidecarInput(String value) throws IOException {
        if (value == null) {
            return null;
        } else {
            try {
                return objectMapper.readValue(value,
                        SidecarInput.class);
            } catch (JsonMappingException ex) {
                log.error(String.format("Function returned JSON that cannot be converted to the output: %s", value));
                throw new IOException(String.format("Cannot unmarshal JSON as SidecarInput output (%s)", ex.getMessage()));
            }
        }
    }

    /**
     * Convert a raw string value into the Lambda sidecar output
     *
     * @param lambdaRawValue lambda sidecar output value
     * @return marshalled object
     * @throws IOException if the marshalling cannot be successfully completed.
     */
    public static SidecarPreProcessorOutput toSidecarPreProcessorOutput(String lambdaRawValue) throws IOException {
        if (lambdaRawValue == null) {
            return null;
        }

        try {
            final JsonSidecarPreProcessorOutput retVal = objectMapper.readValue(lambdaRawValue,
                    JsonSidecarPreProcessorOutput.class);

            checkSidecarOutputTypeSafety(retVal);

            return retVal;
        } catch (JsonMappingException ex) {
            log.error(String.format("Function returned JSON that cannot be converted to the output: %s", lambdaRawValue));
            throw new IOException(String.format("Cannot unmarshal JSON as PreProcessor output (%s)", ex.getMessage()));
        }
    }

    /**
     * Helper class that converts encrypted messaged carrying strings into objects that carry JSON data.
     *
     * @param msg      original message
     * @param carryCls class of type <code>T</code> of the JSON message
     * @param <T>      Type
     * @return Instance of object with the string converted to JSON
     * @throws IOException if conversion is not possible.
     */
    public static <T> EncryptedMessage<T> toMessageCarrying(EncryptedMessage<String> msg, Class<T> carryCls) throws IOException {
        try {
            return new EncryptedMessage<>(msg.getSynchronicity()
                    , objectMapper.readValue(msg.getPayload(), carryCls)
                    , msg.getContext());
        } catch (JsonMappingException ex) {
            throw new IOException(String.format("Unmarshalling is not possible: %s", ex.getMessage()));
        }
    }

    /**
     * Convert a raw string value into the Lambda sidecar output
     *
     * @param lambdaRawValue lambda sidecar output value
     * @return marshalled object
     * @throws IOException if the marshalling cannot be successfully completed.
     */
    public static SidecarPostProcessorOutput toSidecarPostProcessorOutput(String lambdaRawValue) throws IOException {
        if (lambdaRawValue == null) {
            return null;
        }

        try {
            final JsonSidecarPostProcessorOutput retVal = objectMapper.readValue(lambdaRawValue,
                    JsonSidecarPostProcessorOutput.class);

            checkSidecarPostOutputTypeSafety(retVal);

            return retVal;
        } catch (JsonMappingException ex) {
            log.error(String.format("Function returned JSON that cannot be converted to the output: %s", lambdaRawValue));
            throw new IOException("Cannot unmarshal JSON as PreProcessor output");
        }
    }

    private static void checkSidecarPostOutputTypeSafety(JsonSidecarPostProcessorOutput retVal) throws IOException {
        if (retVal != null) {
            if (retVal.getModify() != null) {
                retVal.getModify().checkTypeSafety();
            }
        }
    }

    private static void checkSidecarOutputTypeSafety(JsonSidecarPreProcessorOutput retVal) throws IOException {
        if (retVal != null) {
            if (retVal.getModify() != null) {
                retVal.getModify().checkTypeSafety();
            }
        }
    }

    public static String toJSON(Object obj) {
        return toJSON(obj, false);
    }

    /**
     * Restores the JSON object from the transport-optimized JSON representation. In essence, this method authomatically
     * detects and unpacks the GZIP-encoded objects.
     *
     * @param buf buffer received at the transport level
     * @param clz expected class representing the JSON object;
     * @param <T> type of the object being expected
     * @return instance of the object, if read successfully.
     * @throws IOException if unmarshalling error occurs.
     */
    public static <T> T readTransportOptimizedJSON(byte[] buf, Class<T> clz) throws IOException {
        String unmarshalStr = readTransportOptimizedString(buf);
        if (unmarshalStr == null) {
            return null;
        }

        T retVal = objectMapper.readValue(unmarshalStr, clz);
        if (retVal instanceof JsonSidecarPreProcessorOutput) {
            checkSidecarOutputTypeSafety((JsonSidecarPreProcessorOutput) retVal);
        } else if (retVal instanceof JsonSidecarPostProcessorOutput) {
            checkSidecarPostOutputTypeSafety((JsonSidecarPostProcessorOutput) retVal);
        }

        return retVal;
    }

    public static ObjectNode readTransportOptimizedJSONForModification(byte[] buf) throws IOException {
        JsonNode unmarshalStr = readTransportOptimizedJSON(buf, JsonNode.class);
        if (unmarshalStr instanceof ObjectNode) {
            return ((ObjectNode) unmarshalStr);
        } else {
            return null;
        }
    }

    /**
     * A shortcut for <code>readTransportOptimizedJSON(buf, JsonNode.class)</code>
     * @param buf buffer to read
     * @return {@link JsonNode} of parsed data
     * @throws IOException if json cannot be unmarshalled
     */
    public static JsonNode readTransportOptimizedJSON(byte[] buf) throws IOException {
        return readTransportOptimizedJSON(buf, JsonNode.class);
    }

    /**
     * Auto-guesses byte string as being gzipped and unzipps it.
     *
     * @param buf received bytes
     * @return String being transmitted
     * @throws IOException if the string could not be read or decompressed.
     */
    public static String readTransportOptimizedString(byte[] buf) throws IOException {
        if (buf == null) {
            return null;
        }

        String unmarshalStr;

        if (isGzipped(buf)) {
            try (GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(buf))) {
                ByteArrayOutputStream decompressed = new ByteArrayOutputStream(2048);
                byte[] readBuf = new byte[2048];
                int k = 0;
                while ((k = gz.read(readBuf)) > 0) {
                    decompressed.write(readBuf, 0, k);
                }

                unmarshalStr = decompressed.toString();
            }
        } else {
            unmarshalStr = new String(buf);
        }
        return unmarshalStr;
    }

    public static boolean isGzipped(byte[] buf) {
        return buf != null && buf.length > 2 && buf[0] == (byte) 0x1f && buf[1] == (byte) 0x8b;
    }

    /**
     * Converts an object to a serialized form. For small objects, the output will be an UTF-8 encoded string.
     * However, should an marshalled string be longer than a reasonable threshold, then a GZip compression will
     * be applied on the output bytes.
     *
     * @param obj object to be serialized
     * @return bytes that need sending
     * @throws IOException in case marshalling will fail.
     */
    public static byte[] toTransportOptimizedJSON(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        objectMapper.writeValue(baos, obj);
        if (baos.size() > GZIP_FOR_TRANSPORT_THRESHOLD) {
            ByteArrayOutputStream gz = new ByteArrayOutputStream();
            try (GZIPOutputStream compr = new GZIPOutputStream(gz)) {
                compr.write(baos.toByteArray());
            }

            return gz.toByteArray();
        } else {
            return baos.toByteArray();
        }

    }

    /**
     * Produces the Base64-encoded JSON.
     *
     * @param obj
     * @return
     */
    public static String toBase64JSON(Object obj) {
        return new String(Base64.getEncoder().encode(toJSON(obj).getBytes()));
    }

    public static JsonNode fromMap(Map source) {
        return objectMapper.convertValue(source, JsonNode.class);
    }

    /**
     * Converts an object to a Map structure
     *
     * @param source source object
     * @return null if source is null, otherwise the object representation of this object.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, ?> toMap(Object source) {
        if (source == null) {
            return null;
        }

        return (Map<String, ?>) objectMapper.convertValue(source, Map.class);
    }

    /**
     * Converts a value to the specified type.
     *
     * @param o     object ot convert
     * @param clazz class
     * @param <T>   Type
     * @return instance of the class.
     */
    public static <T> T convert(Object o, Class<T> clazz) {
        if (o == null) {
            return null;
        }

        try {
            return objectMapper.convertValue(o, clazz);
        } catch (IllegalArgumentException ex) {
            // At times you need to see the errors that Jackson is writing. In this case, uncomment the following
            // line to make the error visible. DO NOT commit the code with the line below UNcommented.
//             ex.printStackTrace();
            log.error(String.format("Cannot unmarshal object %s: %s", o, ex.getMessage()));
            return null;
        }
    }

    public static String toPrettyJSON(Object obj) throws JsonProcessingException {
        return writer.writeValueAsString(obj);
    }

    public static byte[] toGzippedJson(Object obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream sink = baos;
        try {
            sink = new GZIPOutputStream(baos);

            objectMapper.writeValue(sink, obj);
            sink.close();

            return baos.toByteArray();
        } catch (IOException e) {
            return null;
        } finally {
            try {
                sink.close();
            } catch (IOException ex) {
                // Can't do anything about it.
            }
        }
    }

    public static String toJSON(Object obj, boolean compress) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream sink = baos;
        try {
            if (compress) {
                sink = new GZIPOutputStream(baos);
            }
            objectMapper.writeValue(sink, obj);
            sink.close();

            return compress ? new String(Base64.getEncoder().encode(baos.toByteArray())) : baos.toString();
        } catch (IOException e) {
            return null;
        } finally {
            try {
                sink.close();
            } catch (IOException ex) {
                // Can't do anything about it.
            }
        }
    }

    public static JsonIO getDefaultUnmarshaller() {
        if (defaultUnmarshaller == null) {
            defaultUnmarshaller = new DefaultJsonUmarshaller();
        }
        return defaultUnmarshaller;
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> getUnmarshallingImplementation(Class<T> cls) {
        if (SidecarPreProcessorOutput.class.equals(cls)) {
            return (Class<T>) JsonSidecarPreProcessorOutput.class;
        } else if (SidecarPostProcessorOutput.class.equals(cls)) {
            return (Class<T>) JsonSidecarPostProcessorOutput.class;
        } else {
            return cls;
        }
    }

    public static void replacePath(ObjectNode json, String path, Object newValue) {
        if (path == null) {
            return;
        }

        ObjectNodeAndProperty onap = getJsonPointerTarget(json, path);
        if (onap.isAddressed()) {
            onap.json.set(onap.property, objectMapper.convertValue(newValue, JsonNode.class));
        }

    }

    protected static ObjectNodeAndProperty getJsonPointerTarget(@NonNull ObjectNode json, @NonNull String path) {
        ObjectNodeAndProperty onap = new ObjectNodeAndProperty();
        onap.json = json;

        String lookupPath = null;

        int idx = path.lastIndexOf("/");
        if (idx < 0) {
            onap.property = path;
        } else if (idx == 0 && path.length() > 1) {
            onap.property = path.substring(1);
        } else if (idx > 0) {
            lookupPath = path.substring(0, idx);

            onap.property = path.substring(idx + 1);
        }

        if (lookupPath != null) {
            final JsonNode at = json.at(lookupPath);
            onap.json = at instanceof ObjectNode ? (ObjectNode) at : null;
        }
        return onap;
    }

    /**
     * Removes a path from the JSON object.
     *
     * @param on   ObjectNode
     * @param path path to be removed
     */
    public static void remove(ObjectNode on, String path) {
        ObjectNodeAndProperty onap = getJsonPointerTarget(on, path);
        if (onap.isAddressed()) {
            onap.json.remove(onap.property);
        }
    }

    static class ObjectNodeAndProperty {
        ObjectNode json;
        String property;

        public boolean isAddressed() {
            return json != null && property != null;
        }
    }
}
