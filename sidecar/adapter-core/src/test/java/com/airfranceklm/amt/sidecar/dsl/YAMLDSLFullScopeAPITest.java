package com.airfranceklm.amt.sidecar.dsl;

import com.airfranceklm.amt.sidecar.JsonHelper;
import com.airfranceklm.amt.sidecar.config.afklyaml.YAMLEndpointConfiguration;
import com.airfranceklm.amt.sidecar.elements.NumericAlgorithms;
import com.airfranceklm.amt.sidecar.elements.NumericElements;
import com.airfranceklm.amt.sidecar.elements.ParameterizedStringElement;
import com.airfranceklm.amt.sidecar.elements.StringFilterAlgorithms;
import com.airfranceklm.amt.yaml.YamlHelper;
import org.junit.Test;

import java.util.Map;

import static com.airfranceklm.amt.sidecar.elements.DataElementFilterIntent.MatchRequired;
import static com.airfranceklm.amt.sidecar.elements.DataElementFilterIntent.MatchScopes;
import static com.airfranceklm.amt.sidecar.model.ElementDemand.elem;
import static com.airfranceklm.amt.sidecar.model.ElementFilterDemand.demandElementFilter;
import static com.airfranceklm.amt.sidecar.model.MaxPayloadSizeSetting.blockPayloadsExceeding;
import static com.airfranceklm.amt.sidecar.model.MaxPayloadSizeSetting.noopPayloadsExceeding;
import static com.airfranceklm.amt.sidecar.model.SidecarSynchronicity.Event;
import static com.airfranceklm.amt.sidecar.model.SidecarSynchronicity.RequestResponse;

/**
 * A test that checks that a full scope of the YAML configuration can be build with YAML DSL.
 */
public class YAMLDSLFullScopeAPITest {

    @Test
    public void testCanGenerateFullYAMLDefinition() {
        YAMLEndpointConfiguration rootCfg = new YAMLEndpointConfiguration("aServiceId", "anEndpointId");

        final ParameterizedStringElement reqHeaderElem = ParameterizedStringElement.RequestHeader;
        final NumericElements respCode = NumericElements.ResponseCode;

        rootCfg.preProcess((dsl) -> {
            dsl.preflight((preflightDSL) -> {
                preflightDSL.preflightEnabled()
                        .demandElement(elem(reqHeaderElem.getElementName(), "accept"))
                        .demandElement(elem(reqHeaderElem.getElementName(), "content-type").filtered((f) -> f.intent(MatchScopes).algorithm(StringFilterAlgorithms.NonEmpty)))
                        .timeout("2.5s")
                        .limitRequestSize(blockPayloadsExceeding(12000))
                        .stack((c) -> c.name("demoStack").param("a", "b").param("c", "d"))
                        .demandElement((demandBuild) -> {
                            demandBuild.name(reqHeaderElem.getElementName()).parameter("x-afklm-market")
                                    .filter(demandElementFilter()
                                            .intent(MatchScopes)
                                            .algorithm(StringFilterAlgorithms.OneOfI)
                                            .expression("US|NL|FR")
                                            .label("M1")
                                            .build())
                                    .filter(demandElementFilter()
                                            .intent(MatchScopes)
                                            .algorithm(StringFilterAlgorithms.OneOfI)
                                            .expression("CA|DE|IN")
                                            .label("M2")
                                            .build());
                        })
                ;
            });

            dsl.sidecar((sidecarDSL) -> {
                sidecarDSL.withIdempotentSupport()
                        .limitRequestSize(noopPayloadsExceeding(90000))
                        .timeout("0.123m")
                        .failsafe()
                        .demandElement(elem(reqHeaderElem.getElementName(), "accept").filtered((f) -> f.algorithm(StringFilterAlgorithms.Json)))
                        .demandElement(elem(reqHeaderElem.getElementName(), "content-type").filtered((f) -> f.intent(MatchRequired).algorithm(StringFilterAlgorithms.Json)))
                        .stack("demoStack")
                        .param("A", "B")
                        .param("C", 12345);
            });

            dsl.staticModification((sm) -> {
                sm.modify().changeRoute()
                        .toHost("aa.bb.cc")
                        .toFile("/index?query=123");
            });
        }).postProcess((dsl) -> {
            dsl.synchronicity(Event)
                    .timeout("3.6s")
                    .failsafe()
                    .param("PX", "PBA")
                    .stack((stackCfg) -> { stackCfg.name("demoStack").param("pStack", "pValue").param("pStack1", "pValue1");})
                    .demandElement(elem(reqHeaderElem.getElementName(), "content-type"))
                    .demandElement(elem(reqHeaderElem.getElementName(), "content-encoding"))
                    .demandElement(elem(respCode).filtered((f) -> f.algorithm(NumericAlgorithms.HttpOk)));
        });

        System.out.println(YamlHelper.yamlStringOf(JsonHelper.convert(rootCfg, Map.class)));
        System.out.println("---");
    }

    @Test
    public void testCanGenerateInMemoryStore() {
        YAMLEndpointConfiguration rootCfg = new YAMLEndpointConfiguration("aServiceId", "anEndpointId");

        rootCfg.setServiceId("aServiceId");
        rootCfg.setEndpointId("anEndpointId");

        rootCfg.forInput((c) -> {
            c.synchronicity(RequestResponse)
                    .packageKey("absc");
        }).replyWith((c) -> {
            c.terminate()
                    .withCode(543)
                    .withMessage("Sample message");
        });

        System.out.println(YamlHelper.yamlStringOf(JsonHelper.convert(rootCfg, Map.class)));
        System.out.println("---");
    }
}
