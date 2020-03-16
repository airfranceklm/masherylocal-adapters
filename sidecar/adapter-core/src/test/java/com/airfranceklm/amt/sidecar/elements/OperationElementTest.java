package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarInputOperation;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildClientRequest;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildEndpoint;

public class OperationElementTest extends ElementTestCase<SidecarInputOperation> {

    private static BasicDSL dsl;

    @BeforeClass
    public static void init() {
        dsl = BasicDSL.make();
        dsl.expr((tc) -> {
            buildEndpoint(tc, (cfg) -> {
                cfg.endpointURI("https://unit-tests.airfranceklm.com/travel/endpoint");
            });

            buildClientRequest(tc, (cfg) -> {
                cfg.resource("/path/to/op")
                        .queryParam("q1", "p1")
                        .httpVerb("GET");
            });
        });
    }

    @Test
    public void testResourcePathExtraction() throws DataElementException {
        ElementDemand cfg = new ElementDemand(ObjectElements.Operation);

        SidecarInputOperation opToExtract = new SidecarInputOperation();
        // URI will not be set.
        opToExtract.setPath("/path/to/op");
        opToExtract.setHttpVerb("get");

        Map<String,String> query = new HashMap<>();
        query.put("q1", "p1");

        opToExtract.setQuery(query);

        assertAcceptedForPreAndPost(dsl, cfg, opToExtract, SidecarInput::getOperation);
    }

    @Test
    public void testFullOperationExtraction() throws DataElementException {
        ElementDemand cfg = new ElementDemand(ObjectElements.FullOperation);

        SidecarInputOperation opToExtract = new SidecarInputOperation();
        opToExtract.setUri("https://unit-tests.airfranceklm.com/travel/endpoint/path/to/op?q1=p1");
        opToExtract.setPath("/path/to/op");
        opToExtract.setHttpVerb("get");

        Map<String,String> query = new HashMap<>();
        query.put("q1", "p1");

        opToExtract.setQuery(query);

        assertAcceptedForPreAndPost(dsl, cfg, opToExtract, SidecarInput::getOperation);
    }


}
