package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.impl.model.SidecarPostProcessorOutputImpl;
import com.airfranceklm.amt.sidecar.impl.model.SidecarPreProcessorOutputImpl;
import com.airfranceklm.amt.sidecar.model.SidecarPostProcessorOutput;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class JsonHelper {
    private static Logger log = LoggerFactory.getLogger(JsonHelper.class);

    public static final ObjectMapper objectMapper = new ObjectMapper();

    private static ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

    private static DateTimeFormatter jsonFormat = ISODateTimeFormat.dateTimeParser();

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
     * Convert a raw string value into the Lambda sidecar output
     *
     * @param lambdaRawValue lambda sidecar output value
     * @return marshalled object
     * @throws IOException if the marshalling cannot be successfully completed.
     */
    static SidecarPreProcessorOutput toSidecarPreProcessorOutput(String lambdaRawValue) throws IOException {
        try {
            final SidecarPreProcessorOutputImpl retVal = objectMapper.readValue(lambdaRawValue,
                    SidecarPreProcessorOutputImpl.class);

            if (retVal.getModify() != null) {
                retVal.getModify().checkTypeSafety();
            }

            return retVal;
        } catch (JsonMappingException ex) {
            log.error(String.format("Function returned JSON that cannot be converted to the output: %s", lambdaRawValue));
            throw new IOException("Cannot unmarshal JSON as PreProcessor output");
        }
    }

    /**
     * Convert a raw string value into the Lambda sidecar output
     *
     * @param lambdaRawValue lambda sidecar output value
     * @return marshalled object
     * @throws IOException if the marshalling cannot be successfully completed.
     */
    static SidecarPostProcessorOutput toSidecarPostProcessorOutput(String lambdaRawValue) throws IOException {
        try {
            final SidecarPostProcessorOutputImpl retVal = objectMapper.readValue(lambdaRawValue,
                    SidecarPostProcessorOutputImpl.class);

            if (retVal.getModify() != null) {
                retVal.getModify().checkTypeSafety();
            }

            return retVal;
        } catch (JsonMappingException ex) {
            log.error(String.format("Function returned JSON that cannot be converted to the output: %s", lambdaRawValue));
            throw new IOException("Cannot unmarshal JSON as PreProcessor output");
        }
    }

    public static String toJSON(Object obj) {
        return toJSON(obj, false);
    }

    public static JsonNode fromMap(Map source) {
        return objectMapper.convertValue(source, JsonNode.class);
    }

    public static String toPrettyJSON(Object obj) throws JsonProcessingException {
        return  writer.writeValueAsString(obj);
    }

    static String toJSON(Object obj, boolean compress) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream sink = baos;
        try {
            if (compress) {
                sink = new GZIPOutputStream(baos);
            }
            objectMapper.writeValue(sink, obj);
            sink.close();

            return compress ? Base64.encode(baos.toByteArray()) : baos.toString();
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
}
