package com.airfranceklm.amt.sidecar;

import org.junit.Test;

import static com.airfranceklm.amt.sidecar.AFKLMSidecarProcessor.inferTextEncoding;
import static com.airfranceklm.amt.sidecar.AFKLMSidecarProcessor.isTextMimeType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AFKLMSidecarProcessorStaticFunctionsTest {

    @Test
    public void testSupportForTextMimeTypes() {
        assertTrue(isTextMimeType("text/plain"));
        assertTrue(isTextMimeType("text/javascript"));
        assertTrue(isTextMimeType("text/whatever"));

        assertTrue(isTextMimeType("application/json"));
        assertTrue(isTextMimeType("application/ld+json"));
        assertTrue(isTextMimeType("application/json+hal"));
        assertTrue(isTextMimeType("application/json+hal-ext"));
        assertTrue(isTextMimeType("application/vnd.api+json"));

        assertTrue(isTextMimeType("application/javascript"));
        assertTrue(isTextMimeType("application/javascript+hal"));
        assertTrue(isTextMimeType("application/javascript+hal-ext"));

        assertTrue(isTextMimeType("application/yaml"));
        assertTrue(isTextMimeType("application/yaml+hal"));
        assertTrue(isTextMimeType("application/yaml+hal-ext"));


        assertTrue(isTextMimeType("application/x-www-form-urlencoded"));
        assertTrue(isTextMimeType("application/xml"));
        assertTrue(isTextMimeType("application/graphql"));

        assertTrue(isTextMimeType("application/xhtml"));
        assertTrue(isTextMimeType("application/xhtml+xml"));
    }

    @Test
    public void testEncodingInference() {
        assertEquals("utf-8", inferTextEncoding("application/json"));
        assertEquals("utf-8", inferTextEncoding("application/json; charset=utf-8"));
        assertEquals("utf-8", inferTextEncoding("application/json; charset = utf-8"));
        assertEquals("ascii", inferTextEncoding("application/json; charset=ascii"));
        assertEquals("custom", inferTextEncoding("application/json; charset=custom"));
    }
}
