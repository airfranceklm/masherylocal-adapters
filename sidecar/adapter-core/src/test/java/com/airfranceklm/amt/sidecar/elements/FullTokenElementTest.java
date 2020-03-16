package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.JsonHelper;
import com.airfranceklm.amt.testsupport.*;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.Invoke;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FullTokenElementTest extends MasheryProcessorTestSupport<MasheryProcessorTestCase> {
    private static DSL<MasheryProcessorTestCase> dsl;
    private static Date refDate;

    @BeforeClass
    public static void initDSL() {
        dsl = BasicDSL.make();
        refDate = JsonHelper.parseJSONDate("2019-10-10T22:23:24Z");

        dsl.expr((tc) -> {
            MasheryProcessorTestCaseAccessor.buildAuthorizationContext(tc, (cfg) -> {
                cfg.userContext("uc1 uc2 uc3")
                        .scope("p1 p2 p3")
                        .grantType("password")
                        .expires(refDate);
            });
        });

    }

    @Test
    public void testBasicExtraction() throws DataElementException {
        PreProcessEvent ppe = createPreProcessMock(dsl.build());
        replayAll();

        ElementDemand cfg = new ElementDemand("+token");
        DataElement<? super PreProcessEvent, ?> elem = StandardElementsFactory.createForPreProcessor(cfg);
        assertNotNull(elem);

        SidecarInvocationData sid = new SidecarInvocationData();
        DataElementRelevance der = elem.extract(ppe, sid);

        assertEquals(Invoke, der);
        assertNotNull(sid.getInput().getToken());
        assertEquals("uc1 uc2 uc3", sid.getInput().getToken().getUserContext());
        assertEquals("p1 p2 p3", sid.getInput().getToken().getScope());
        assertEquals("password", sid.getInput().getToken().getGrantType());
        assertEquals(refDate, sid.getInput().getToken().getExpires());
        assertEquals("unit-test-token", sid.getInput().getToken().getBearerToken());
    }
}
