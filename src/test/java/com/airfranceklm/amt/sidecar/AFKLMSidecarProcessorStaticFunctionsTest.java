package com.airfranceklm.amt.sidecar;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AFKLMSidecarProcessorStaticFunctionsTest {

    @Test
    public void testSupportForTextMimeTypes() {
        assertTrue(AFKLMSidecarProcessor.isTextMimeType("text/plain"));
        assertTrue(AFKLMSidecarProcessor.isTextMimeType("text/javascript"));
        assertTrue(AFKLMSidecarProcessor.isTextMimeType("text/whatever"));

        assertTrue(AFKLMSidecarProcessor.isTextMimeType("application/json"));
        assertTrue(AFKLMSidecarProcessor.isTextMimeType("application/ld+json"));
        assertTrue(AFKLMSidecarProcessor.isTextMimeType("application/json+hal"));
        assertTrue(AFKLMSidecarProcessor.isTextMimeType("application/json+hal-ext"));
        assertTrue(AFKLMSidecarProcessor.isTextMimeType("application/vnd.api+json"));

        assertTrue(AFKLMSidecarProcessor.isTextMimeType("application/javascript"));
        assertTrue(AFKLMSidecarProcessor.isTextMimeType("application/javascript+hal"));
        assertTrue(AFKLMSidecarProcessor.isTextMimeType("application/javascript+hal-ext"));

        assertTrue(AFKLMSidecarProcessor.isTextMimeType("application/yaml"));
        assertTrue(AFKLMSidecarProcessor.isTextMimeType("application/yaml+hal"));
        assertTrue(AFKLMSidecarProcessor.isTextMimeType("application/yaml+hal-ext"));


        assertTrue(AFKLMSidecarProcessor.isTextMimeType("application/x-www-form-urlencoded"));
        assertTrue(AFKLMSidecarProcessor.isTextMimeType("application/xml"));
        assertTrue(AFKLMSidecarProcessor.isTextMimeType("application/graphql"));

        assertTrue(AFKLMSidecarProcessor.isTextMimeType("application/xhtml"));
        assertTrue(AFKLMSidecarProcessor.isTextMimeType("application/xhtml+xml"));
    }

    @Test
    public void testEncodingInference() {
        assertEquals("utf-8", AFKLMSidecarProcessor.inferTextEncoding("application/text"));
        assertEquals("utf-8", AFKLMSidecarProcessor.inferTextEncoding("application/text; charset=utf-8"));
        assertEquals("utf-8", AFKLMSidecarProcessor.inferTextEncoding("application/text; charset = utf-8"));
        assertEquals("ascii", AFKLMSidecarProcessor.inferTextEncoding("application/text; charset=ascii"));
        assertEquals("custom", AFKLMSidecarProcessor.inferTextEncoding("application/text; charset=custom"));
    }
}
