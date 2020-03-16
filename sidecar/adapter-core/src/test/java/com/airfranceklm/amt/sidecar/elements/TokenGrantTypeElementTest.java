package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.model.ElementFilterDemand;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.testsupport.*;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.*;
import static com.airfranceklm.amt.sidecar.elements.DataElementFilterIntent.*;
import static com.airfranceklm.amt.sidecar.elements.StringFilterAlgorithms.DslExpression;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildAuthorizationContext;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TokenGrantTypeElementTest extends MasheryProcessorTestSupport<MasheryProcessorTestCase> {

    static BasicDSL dsl;
    static BasicDSL emptyCase;

    @BeforeClass
    public static void initDSL() {
        emptyCase = BasicDSL.make();
        emptyCase.expr((tc) -> buildAuthorizationContext(tc, (cfg) -> {}));
        
        dsl = emptyCase.duplicate();

        dsl.expr((tc) -> buildAuthorizationContext(tc, (cfg) -> cfg.grantType("password")));
    }

    @Test
    public void testExtractionOfGrantType() throws DataElementException {
       ElementDemand cfg = new ElementDemand("grantType");
        assertConfiguration(dsl, cfg, Invoke, "password", null);
    }

    @Test
    public void testExtractionOfGrantTypeFromMissingContext() throws DataElementException {
        ElementDemand cfg = new ElementDemand("grantType");
            assertConfiguration(emptyCase, cfg, Invoke, null, null);
    }



    @Test
    public void testExtractionOfScopedElement() throws DataElementException {
        ElementDemand cfg = new ElementDemand("grantType");
        cfg.addFilter(new ElementFilterDemand(DslExpression, "password|jwt", MatchScopes));

        assertConfiguration(dsl, cfg, Invoke, "password", null);
    }

    @Test
    public void testExtractionOfScopedElementWithLabel() throws DataElementException {
        ElementDemand cfg = new ElementDemand("grantType");
        cfg.addFilter(new ElementFilterDemand(DslExpression, "jwt",  MatchScopes, "jwt"));
        cfg.addFilter(new ElementFilterDemand(DslExpression,"password", MatchScopes, "password"));

        assertConfiguration(dsl, cfg, Invoke, "password", "password");
    }

    @Test
    public void testExtractionOfDeScopedElement() throws DataElementException {
        ElementDemand cfg = new ElementDemand("grantType");
        cfg.addFilter(new ElementFilterDemand(DslExpression, "password|jwt", MatchDescopes));

        assertConfiguration(dsl, cfg, Noop, null, null);
    }

    @Test
    public void testExtractionOfMatchRequired() throws DataElementException {
        BasicDSL caseDSL = dsl.duplicate();
        caseDSL.expr((tc) -> buildAuthorizationContext(tc, (c) -> c.grantType("c1")));

        ElementDemand cfg = new ElementDemand("grantType");
        cfg.addFilter(new ElementFilterDemand(DslExpression, "c1|c2", MatchRequired));

        assertConfiguration(caseDSL, cfg, Invoke, "c1", null);
    }

    @Test
    public void testExtractionOfMatchProhibits() throws DataElementException {
        BasicDSL caseDSL = dsl.duplicate();
        caseDSL.expr((tc) -> buildAuthorizationContext(tc, (c) -> c.grantType("c1")));

        ElementDemand cfg = new ElementDemand("grantType");
        cfg.addFilter(new ElementFilterDemand(DslExpression,  "c1|c2", MatchProhibits));

        assertConfiguration(caseDSL, cfg, ClientError, null, null);
    }

    private void assertConfiguration(BasicDSL caseDSL, ElementDemand cfg, DataElementRelevance expRelevance, String expValue, String label) throws DataElementException {
        PreProcessEvent ppe = createPreProcessMock(caseDSL.build());
        replayAll();

        DataElement<? super PreProcessEvent, ?> elem = StandardElementsFactory.createForPreProcessor(cfg);
        assertNotNull(elem);

        SidecarInvocationData sid = new SidecarInvocationData();
        DataElementRelevance der = elem.extract(ppe, sid);
        assertEquals(expRelevance, der);

        if (expRelevance == Invoke) {
            assertNotNull(sid.getInput().getToken());
            assertEquals(expValue, sid.getInput().getToken().getGrantType());

            if (label != null) {
                assertNotNull(sid.getInput().getParams());
                assertEquals(label, sid.getInput().getParams().get("grantTypeLabel"));
            }
        } else {
            if (sid.getInput().getToken() != null) {
                assertNull(sid.getInput().getToken());
            }
        }
    }
}
