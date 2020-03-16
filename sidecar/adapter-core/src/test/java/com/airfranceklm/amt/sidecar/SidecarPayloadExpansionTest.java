package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.dsl.SidecarTestDSL;
import com.airfranceklm.amt.sidecar.model.SidecarInputHTTPMessage;
import com.airfranceklm.amt.sidecar.model.SidecarInputHTTPResponseMessage;
import com.airfranceklm.amt.sidecar.model.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.model.SidecarSynchronicity;
import com.airfranceklm.amt.testsupport.DSL;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.sidecar.SidecarInvocationTestCaseAccessor.*;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.*;

public class SidecarPayloadExpansionTest extends SidecarMockSupport {

    private static SidecarTestDSL textCase;
    private static SidecarTestDSL binaryCase;

    private static SidecarTestDSL responseCase;

    private static final String STRING_PAYLOAD = "This is a sample payload";

    @BeforeClass
    public static void initBaseCase() {
        textCase = SidecarTestDSL.make();
        textCase.identifyEndpoint();

        textCase.expr((tc) -> {
            buildEndpoint(tc, (cfg) -> {
                cfg.endpointURI("https://api-unittest.airfranceklm.com/some/test")
                        .preProcessParam("elements", "requestPayload")
                        .postProcessParam("elements", "requestPayload,responsePayload");
            });

            buildClientRequest(tc, (cfg) -> {
                cfg.remoteAddr("192.168.0.1").resource("/apiMethod").payload(STRING_PAYLOAD);
            });

            buildApplication(tc, (cfg) -> cfg.name("app"));
            buildPackageKey(tc, (cfg) -> cfg.packageKey("packageKey"));
        });


        binaryCase = textCase.duplicate();
        binaryCase.expr((tc) -> {
            buildClientRequest(tc, (cfg) -> cfg.payload(null)
                    .payloadOwner(SidecarPayloadExpansionTest.class)
                    .payloadResource("/payloads/sidecar-functional-flow.png"));
        });


        initPostProcessorBaseCase();
    }

    static void initPostProcessorBaseCase() {
        responseCase = textCase.duplicate();

        responseCase.expr((tc) -> {
            buildClientRequest(tc, (cfg) -> {
                cfg.header("content-type", "application/json");
            });

            buildAPIOriginResponse(tc, (cfg) -> cfg.statusCode(201)
                    .header("content-type", "application/json")
                    .payload(STRING_PAYLOAD));

            buildPostProcessorInput(tc, (cfg) -> {
                cfg.synchronicity(SidecarSynchronicity.Event)
                        .serviceId("aServiceId")
                        .endpointId("anEndpointId")
                        .point(SidecarInputPoint.PostProcessor);

                cfg.request(sidecarHTTPInput((h) -> h
                        .payload(STRING_PAYLOAD)));

                cfg.response(sidecarHTTPOutput((h) -> h.statusCode(201)
                        .payload(STRING_PAYLOAD)));
            });
        });

    }

    // -------------------------------------------------------------------------
    // Pre-processor

    @Test
    public void testStringContentTypeWithoutExplicitUTF8() {
        SidecarTestDSL caseDSL = textCase.duplicate();
        caseDSL.expr((tc) -> {
            buildClientRequest(tc, (cfg) -> cfg
                    .header("content-type", "application/json")
                    .payload(STRING_PAYLOAD));
        });

        configureStringPreProcessorRequest(caseDSL);

        verifyPreProcessorCase(caseDSL.build());
    }

    @Test
    public void testExtractionOfRequestWithoutPayloadContent() {
        SidecarTestDSL caseDSL = textCase.duplicate();
        caseDSL.expr((tc) -> {
            buildClientRequest(tc, (cfg) -> cfg.httpVerb("GET").payload(null));

            buildPreProcessorInput(tc, (cfg) -> {
                cfg.synchronicity(SidecarSynchronicity.Event)
                        .serviceId("aServiceId")
                        .endpointId("anEndpointId")
                        .point(SidecarInputPoint.PreProcessor)
                        .request(null);
            });


            /*
            caseDSL.configurePreProcessorInput((dsl) -> {
            dsl.synchronicity(SidecarSynchronicity.Event)
                    .serviceId("serviceId")
                    .endpointId("endpointId")
                    .point(SidecarInputPoint.PreProcessor);

            dsl.emptyRequest();
        });

             */
        });


        verifyPreProcessorCase(caseDSL.build());
    }

    @Test
    public void testStringContentTypeWithExplicitUTF8() {
        SidecarTestDSL caseDSL = textCase.duplicate();
        caseDSL.expr((tc) -> {
            buildClientRequest(tc, (cfg) -> cfg
                    .header("content-type", "application/json; charset=utf-8")
                    .payload(STRING_PAYLOAD));
        });

        configureStringPreProcessorRequest(caseDSL);

        verifyPreProcessorCase(caseDSL.build());
    }

    @Test
    public void testStringContentTypeWithContentEncoding() {
        SidecarTestDSL caseDSL = textCase.duplicate();
        caseDSL.expr((tc) -> {
            buildClientRequest(tc, (cfg) -> cfg
                    .header("content-type", "application/json; charset=utf-8")
                    .header("content-encoding", "strangeEncoding")
                    .payload(STRING_PAYLOAD));
        });


//        configureStringPreProcessorRequest(caseDSL);
        configureBase64String(caseDSL);

        verifyPreProcessorCase(caseDSL.build());
    }

    @Test
    public void testStringContentTypeWithContentTransferEncoding() {
        SidecarTestDSL caseDSL = textCase.duplicate();

        caseDSL.expr((tc) -> {
            buildClientRequest(tc, (cfg) -> cfg
                    .header("content-type", "application/json; charset=utf-8")
                    .header("content-transfer-encoding", "ascii")
                    .payload(STRING_PAYLOAD));
        });

        configureBase64String(caseDSL);

        verifyPreProcessorCase(caseDSL.build());
    }

    @Test
    public void testStringContentTypeWithNonSupportedType() {
        SidecarTestDSL caseDSL = textCase.duplicate();

        caseDSL.expr((tc) -> {
            buildClientRequest(tc, (cfg) -> cfg
                    .header("content-type", "application/x-json; charset=utf-8")
                    .payload(STRING_PAYLOAD));
        });

//        configureStringPreProcessorRequest(caseDSL);
        configureBase64String(caseDSL);

        verifyPreProcessorCase(caseDSL.build());
    }

    private void configureBase64String(SidecarTestDSL caseDSL) {
        caseDSL.expr((tc) -> {
            buildPreProcessorInput(tc, (cfg) -> {
                SidecarInputHTTPMessage im = sidecarHTTPInput((c) ->
                        c.payload("VGhpcyBpcyBhIHNhbXBsZSBwYXlsb2Fk")
                                .payloadBase64Encoded(true)
                                .payloadLength((long) STRING_PAYLOAD.length()));

                cfg.synchronicity(SidecarSynchronicity.Event)
                        .serviceId("aServiceId")
                        .endpointId("anEndpointId")
                        .point(SidecarInputPoint.PreProcessor)
                        .request(im);
            });
        });
    }


    @Test
    public void testImageLoading() {
        SidecarTestDSL caseDSL = binaryCase.duplicate();
        caseDSL.expr((tc) -> {
            buildClientRequest(tc, (cfg) -> {
                cfg.payloadOwner(SidecarPayloadExpansionTest.class)
                        .payloadResource("/payloads/random-bytes.png")
                        .header("content-type", "image/png");
            });

            buildPreProcessorInput(tc, (cfg) -> {

                SidecarInputHTTPMessage im = sidecarHTTPInput((c) -> {
                    c.payload("f/YPga2lxdbRIUaxwG8kVQIkDMnD0KFD4+LiiKG2HDx4cMmSJcQomL0XHlh1ni4US4j9PY5dd1+25wIxVOPApYd2PX4nRsEcvvp4/aHrxFABBweHPXv2EIOJeHl5qfI=")
                            .payloadBase64Encoded(true)
                            .payloadLength(95L);
                });

                cfg.synchronicity(SidecarSynchronicity.Event)
                        .serviceId("serviceId")
                        .endpointId("endpointId")
                        .request(im);
            });
        });
    }

    // ----------------------------------------------------------------------
    // Post-processor tests

    @Test
    public void testFullPayloadExtractionAtPostprocessorAsText() {
        verifyPostProcessorCase(responseCase.duplicate().postProcessorCase().build());
    }

    @Test
    public void testFullPayloadExtractionAtPostprocessorWithGZipCompression() {
        DSL<SidecarInvocationTestCase> caseDSl = responseCase.duplicate().postProcessorCase();
        caseDSl.expr((tc) -> {
            buildAPIOriginResponse(tc, (cfg) -> cfg.header("content-encoding", "gzip"));

            buildPostProcessorInput(tc, (cfg) -> {
                SidecarInputHTTPResponseMessage im = sidecarHTTPOutput((c) -> {
                    c.statusCode(201)
                            .payload("VGhpcyBpcyBhIHNhbXBsZSBwYXlsb2Fk")
                            .payloadLength((long) STRING_PAYLOAD.length())
                            .payloadBase64Encoded(true);
                });

                cfg.synchronicity(SidecarSynchronicity.Event)
                        .serviceId("aServiceId")
                        .endpointId("anEndpointId")
                        .response(im);
            });
        });

        verifyPostProcessorCase(caseDSl.build());
    }

    @Test
    public void testFullPayloadExtractionAtPostprocessorWithBinaryResponse() {
        DSL<SidecarInvocationTestCase> caseDSl = responseCase.duplicate().postProcessorCase();

        caseDSl.expr((tc) -> {
            buildAPIOriginResponse(tc, (cfg) -> {
                cfg.header("content-type", "image/custom")
                        .payload(null)
                        .payloadOwner(SidecarPayloadExpansionTest.class)
                        .payloadResource("/payloads/random-bytes.png");
            });

            buildPostProcessorInput(tc, (cfg) -> {
                SidecarInputHTTPResponseMessage im = sidecarHTTPOutput((c) -> {
                    c.statusCode(201)
                            .payload("f/YPga2lxdbRIUaxwG8kVQIkDMnD0KFD4+LiiKG2HDx4cMmSJcQomL0XHlh1ni4US4j9PY5dd1+25wIxVOPApYd2PX4nRsEcvvp4/aHrxFABBweHPXv2EIOJeHl5qfI=")
                            .payloadBase64Encoded(true)
                            .payloadLength(95L);
                });

                cfg.synchronicity(SidecarSynchronicity.Event)
                        .serviceId("aServiceId")
                        .endpointId("anEndpointId")
                        .response(im);
            });
        });


        verifyPostProcessorCase(caseDSl.build());
    }

    @Test
    public void testFullPayloadExtractionAtPostprocessorWithContentTransferEncoding() {
        DSL<SidecarInvocationTestCase> caseDSL = textCase.duplicate().postProcessorCase();

        caseDSL.expr((tc) -> {
            buildClientRequest(tc, (cfg) -> cfg.header("content-type", "application/json"));

            buildAPIOriginResponse(tc, (cfg) -> {
                cfg.statusCode(201)
                        .header("content-type", "application/json")
                                .payload(STRING_PAYLOAD);
            });

            buildPostProcessorInput(tc, (cfg) -> {
                cfg.synchronicity(SidecarSynchronicity.Event)
                        .serviceId("aServiceId")
                        .endpointId("anEndpointId")
                        .request(sidecarHTTPInput((c) -> c.payload(STRING_PAYLOAD)))
                        .response(sidecarHTTPOutput((c) -> c.statusCode(201).payload(STRING_PAYLOAD)));
            });
        });

        verifyPostProcessorCase(caseDSL.postProcessorCase().build());
    }

    // ----------------------------------------------------------------------
    // Private methods

    private void configureStringPreProcessorRequest(SidecarTestDSL caseDSL) {
        caseDSL.expr((tc) -> {
            buildPreProcessorInput(tc, (cfg) -> {
                cfg.synchronicity(SidecarSynchronicity.Event)
                        .serviceId("aServiceId")
                        .endpointId("anEndpointId")
                        .point(SidecarInputPoint.PreProcessor);

                cfg.request(sidecarHTTPInput((req) -> req.payload(STRING_PAYLOAD)));
            });
        });
    }


}
