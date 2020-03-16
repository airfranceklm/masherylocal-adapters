package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.JsonHelper;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import com.fasterxml.jackson.databind.node.TextNode;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.Invoke;
import static com.airfranceklm.amt.sidecar.elements.PayloadElements.ResponsePayloadFragment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ResponsePayloadFragmentsElementTest extends ElementTestCase<ProcessorEvent> {

    private static BasicDSL responseDSl;

    @BeforeClass
    public static void init() {

        Map<String, String> json = new HashMap<>();
        json.put("a", "A");
        json.put("b", "B");

        responseDSl = BasicDSL.make();
        responseDSl.expr((tc) -> {
            MasheryProcessorTestCaseAccessor.buildAPIOriginResponse(tc, (cfg) -> cfg
                    .header("Content-Type", "application/json")
                    .payload(JsonHelper.toJSON(json)));
        });
    }

    @Test
    public void testExtractionWithPlainTypeAtPostProcessor() throws DataElementException {
        ElementDemand cfg = new ElementDemand(ResponsePayloadFragment.getElementName(), "/a");
        SidecarInvocationData sid = extractFromPostProcessorEvent(responseDSl, cfg, Invoke);

        final Object o = sid.getInput().getResponse().getPayloadFragments().get("/a");
        assertTrue(o instanceof TextNode);
        assertEquals("A", ((TextNode) o).asText());
    }
}
