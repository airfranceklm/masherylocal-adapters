package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.config.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.config.SidecarSynchronicity;
import org.junit.BeforeClass;
import org.junit.Test;

public class AFKLMSidecarPayloadExpansionTest extends AFKLMSidecarMockSupport {

    private static SidecarTestDSL textCase;
    private static SidecarTestDSL binaryCase;

    private static SidecarTestDSL responseCase;

    private static final String STRING_PAYLOAD = "This is a sample payload";

    @BeforeClass
    public static void initBaseCase() {
        textCase = SidecarTestDSL.make();
        textCase.configureAPIClientRequest((dsl) -> {
            dsl.from("192.168.0.1");
            dsl.withUri("https://api-unittest.airfranceklm.com/some/test/apiMethod");
        }).configureEndpointData((dsl) -> {
            dsl.identifyAs("serviceId", "endpointId", "endpointName");
            dsl.enpointUri("https://api-unittest.airfranceklm.com/some/test");

            dsl.preProcessor()
                    .param("expand-input", "requestPayload");
            dsl.postProcessor()
                    .param("expand-input", "requestPayload,responsePayload");
        }).configureAPIOriginRequest((dsl) -> {
            dsl.withOriginalURI("https://some-backend.klm.com/backend/apiMethod");
        }).configurePackageKey((dsl) -> {
            dsl.key("packageKey")
                    .application("app");
        });


        binaryCase = textCase.duplicate();

        textCase.configureAPIClientRequest((dsl) -> {
            dsl.withPayload(STRING_PAYLOAD);
        });

        binaryCase.configureAPIClientRequest(dsl -> {
            dsl.withPayloadFromResource(AFKLMSidecarPayloadExpansionTest.class, "/payloads/sidecar-functional-flow.png");
        });

        initPostProcessorBaseCase();
    }

    static void initPostProcessorBaseCase() {
        responseCase = textCase.duplicate();
        responseCase.configureAPIClientRequest((dsl) -> {
            dsl.withHeader("content-type", "application/json");
        }).configureAPIOriginResponse((dsl) -> {
            dsl.withCode(201)
                    .withHeader("content-type", "application/json")
                    .withPayload(STRING_PAYLOAD);
        });

        responseCase.configureSidecarInput((dsl) -> {
            dsl.withUnitTestMessageId()
                    .synchronicity(SidecarSynchronicity.Event)
                    .serviceId("serviceId")
                    .endpointId("endpointId")
                    .packageKey("packageKey")
                    .point(SidecarInputPoint.PostProcessor);

            dsl.request((reqDsl) -> {
                reqDsl.withPayload(STRING_PAYLOAD);
            });

            dsl.response((respDsl) -> {
                respDsl.withCode(201)
                        .withPayload(STRING_PAYLOAD);
            });
        });
    }

    // -------------------------------------------------------------------------
    // Pre-processor

    @Test
    public void testStringContentTypeWithoutExplicitUTF8() {
        SidecarTestDSL caseDSL = textCase.duplicate();
        caseDSL.configureAPIClientRequest((dsl) -> {
            dsl.withHeader("content-type", "application/json")
                    .withPayload(STRING_PAYLOAD);
        });

        configureStringPreProcessorRequest(caseDSL);

        verifyPreProcessorRequestCase(caseDSL.build());
    }

    @Test
    public void testExtractionOfRequestWithoutPayloadContent() {
        SidecarTestDSL caseDSL = textCase.duplicate();
        caseDSL.configureAPIClientRequest((dsl) -> {
            dsl.withVerb("GET")
            .withoutPayload();
        });

        caseDSL.configureSidecarInput((dsl) -> {
            dsl.request().withoutPayload();
        });

        caseDSL.configureSidecarInput((dsl) -> {
            dsl.withUnitTestMessageId()
                    .synchronicity(SidecarSynchronicity.Event)
                    .serviceId("serviceId")
                    .endpointId("endpointId")
                    .packageKey("packageKey")
                    .point(SidecarInputPoint.PreProcessor);

            dsl.withEmptyRequest();
        });

        verifyPreProcessorRequestCase(caseDSL.build());

    }

    @Test
    public void testStringContentTypeWithExplicitUTF8() {
        SidecarTestDSL caseDSL = textCase.duplicate();
        caseDSL.configureAPIClientRequest((dsl) -> {
            dsl.withHeader("content-type", "application/json; charset=utf-8")
                    .withPayload(STRING_PAYLOAD);
        });

        configureStringPreProcessorRequest(caseDSL);

        verifyPreProcessorRequestCase(caseDSL.build());
    }

    @Test
    public void testStringContentTypeWithContentEncoding() {
        SidecarTestDSL caseDSL = textCase.duplicate();
        caseDSL.configureAPIClientRequest((dsl) -> {
            dsl.withHeader("content-type", "application/json; charset=utf-8")
                    .withHeader("content-encoding", "strangeEncoding")
                    .withPayload(STRING_PAYLOAD);
        });

        configureStringPreProcessorRequest(caseDSL);
        configureBase64String(caseDSL);

        verifyPreProcessorRequestCase(caseDSL.build());
    }

    @Test
    public void testStringContentTypeWithContentTransferEncoding() {
        SidecarTestDSL caseDSL = textCase.duplicate();
        caseDSL.configureAPIClientRequest((dsl) -> {
            dsl.withHeader("content-type", "application/json; charset=utf-8")
                    .withHeader("content-transfer-encoding", "ascii")
                    .withPayload(STRING_PAYLOAD);
        });

        configureStringPreProcessorRequest(caseDSL);
        configureBase64String(caseDSL);

        verifyPreProcessorRequestCase(caseDSL.build());
    }

    @Test
    public void testStringContentTypeWithNonSupportedType() {
        SidecarTestDSL caseDSL = textCase.duplicate();
        caseDSL.configureAPIClientRequest((dsl) -> {
            dsl.withHeader("content-type", "application/x-json; charset=utf-8")
                    .withPayload(STRING_PAYLOAD);
        });

        configureStringPreProcessorRequest(caseDSL);
        configureBase64String(caseDSL);

        verifyPreProcessorRequestCase(caseDSL.build());
    }

    private void configureBase64String(SidecarTestDSL caseDSL) {
        caseDSL.configureSidecarInput((dsl) -> {
            dsl.request().withPayload("VGhpcyBpcyBhIHNhbXBsZSBwYXlsb2Fk")
                    .base64Encoded(true)
                    .withPayloadLength(STRING_PAYLOAD.length());
        });
    }

    @Test
    public void testImageLoading() {
        SidecarTestDSL caseDSL = binaryCase.duplicate();

        caseDSL.configureAPIClientRequest((dsl) -> {
            dsl.withPayloadFromResource(AFKLMSidecarPayloadExpansionTest.class, "/payloads/random-bytes.png");
        }).configureAPIClientRequest((dsl) -> {
            dsl.withHeader("content-type", "image/png");
        });

        caseDSL.configureSidecarInput((dsl) -> {
            dsl.withUnitTestMessageId()
                    .synchronicity(SidecarSynchronicity.Event)
                    .serviceId("serviceId")
                    .endpointId("endpointId")
                    .packageKey("packageKey")
                    .point(SidecarInputPoint.PreProcessor);

            dsl.request((reqDsl) -> {
                reqDsl.withPayload("f/YPga2lxdbRIUaxwG8kVQIkDMnD0KFD4+LiiKG2HDx4cMmSJcQomL0XHlh1ni4US4j9PY5dd1+25wIxVOPApYd2PX4nRsEcvvp4/aHrxFABBweHPXv2EIOJeHl5qfI=")
                        .base64Encoded(true)
                        .withPayloadLength(95);
            });
        });

    }

    // ----------------------------------------------------------------------
    // Post-processor tests

    @Test
    public void testFullPayloadExtractionAtPostprocessorAsText() {
        verifyPostProcessorRequestCase(responseCase.duplicate().postProcessorCase().build());
    }

    @Test
    public void testFullPayloadExtractionAtPostprocessorWithGZipCompression() {
        SidecarTestDSL caseDSl = responseCase.duplicate().postProcessorCase();
        caseDSl.configureAPIOriginResponse((dsl) -> {
            dsl.withHeader("content-encoding", "gzip");
        });
        caseDSl.configureSidecarInput((dsl) -> {
            dsl.response((respDsl) -> {
                respDsl.withPayload("VGhpcyBpcyBhIHNhbXBsZSBwYXlsb2Fk")
                        .withPayloadLength(STRING_PAYLOAD.length())
                        .base64Encoded(true);
            });
        });

        verifyPostProcessorRequestCase(caseDSl.build());
    }

    @Test
    public void testFullPayloadExtractionAtPostprocessorWithBinaryResponse() {
        SidecarTestDSL caseDSl = responseCase.duplicate().postProcessorCase();
        caseDSl.configureAPIOriginResponse((dsl) -> {
            dsl.withHeader("content-type", "image/custom")
                    .withPayloadFromResource(AFKLMSidecarPayloadExpansionTest.class, "/payloads/random-bytes.png");

        });
        caseDSl.configureSidecarInput((dsl) -> {
            dsl.response((respDsl) -> {
                respDsl.withPayload("f/YPga2lxdbRIUaxwG8kVQIkDMnD0KFD4+LiiKG2HDx4cMmSJcQomL0XHlh1ni4US4j9PY5dd1+25wIxVOPApYd2PX4nRsEcvvp4/aHrxFABBweHPXv2EIOJeHl5qfI=")
                        .base64Encoded(true)
                        .withPayloadLength(95);
            });
        });

        verifyPostProcessorRequestCase(caseDSl.build());
    }

    @Test
    public void testFullPayloadExtractionAtPostprocessorWithContentTransferEncoding() {
        SidecarTestDSL caseDSL = textCase.duplicate();
        caseDSL.configureAPIClientRequest((dsl) -> {
            dsl.withHeader("content-type", "application/json");
        }).configureAPIOriginResponse((dsl) -> {
            dsl.withCode(201)
                    .withHeader("content-type", "application/json")
                    .withPayload(STRING_PAYLOAD);
        });

        caseDSL.configureSidecarInput((dsl) -> {
            dsl.withUnitTestMessageId()
                    .synchronicity(SidecarSynchronicity.Event)
                    .serviceId("serviceId")
                    .endpointId("endpointId")
                    .packageKey("packageKey")
                    .point(SidecarInputPoint.PostProcessor);

            dsl.request((reqDsl) -> {
                reqDsl.withPayload(STRING_PAYLOAD);
            });

            dsl.response((respDsl) -> {
                respDsl.withCode(201)
                        .withPayload(STRING_PAYLOAD);
            });
        });

        verifyPostProcessorRequestCase(caseDSL.postProcessorCase().build());
    }

    // ----------------------------------------------------------------------
    // Private methods

    private void configureStringPreProcessorRequest(SidecarTestDSL caseDSL) {
        caseDSL.configureSidecarInput((dsl) -> {
            dsl.withUnitTestMessageId()
                    .synchronicity(SidecarSynchronicity.Event)
                    .serviceId("serviceId")
                    .endpointId("endpointId")
                    .packageKey("packageKey")
                    .point(SidecarInputPoint.PreProcessor);

            dsl.request((reqDsl) -> {
                reqDsl.withPayload(STRING_PAYLOAD);
            });
        });
    }


}
