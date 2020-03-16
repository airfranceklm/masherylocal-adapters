package com.airfranceklm.amt.sidecar.elements;

import com.airfranceklm.amt.sidecar.model.ElementDemand;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.JsonHelper;
import com.airfranceklm.amt.testsupport.BasicDSL;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCase;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestSupport;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.airfranceklm.amt.sidecar.elements.DataElementRelevance.Invoke;
import static com.airfranceklm.amt.sidecar.elements.ObjectElements.UserContextField;
import static com.airfranceklm.amt.sidecar.model.SidecarInput.Accessor.allocOrGetParameterGroup;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildAuthorizationContext;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UserContextStringFieldElementTest extends MasheryProcessorTestSupport<MasheryProcessorTestCase> {

    private static String jsonString;
    private static BasicDSL baseDSL;

    @BeforeClass
    public static void init() {
        Map<String,Object> map = new HashMap<>();
        map.put("a", "string");
        map.put("b", 125);

        jsonString = JsonHelper.toJSON(map);

        baseDSL = BasicDSL.make();
        baseDSL.expr((tc) -> buildAuthorizationContext(tc, (c) -> c.userContext(jsonString)));
    }


    @Test
    public void testBasicExtraction() throws DataElementException {
        PreProcessEvent ppe = createPreProcessMock(baseDSL.build());
        replayAll();

        ElementDemand cfg = ElementDemand.elem(UserContextField, "a");
        DataElement<? super PreProcessEvent, ?> elem = StandardElementsFactory.createForPreProcessor(cfg);
        assertNotNull(elem);

        SidecarInvocationData sid = new SidecarInvocationData();
        DataElementRelevance der = elem.extract(ppe, sid);

        assertEquals(Invoke, der);
        assertNotNull(sid.getInput().getParams());
        assertNotNull(sid.getInput().getParams().get("userContextField"));
        assertEquals("string", sid.getInput().getParameterGroup("userContextField").get("a"));
    }

    @Test
    public void testBasicExtractionConflictingType() throws DataElementException {
        PreProcessEvent ppe = createPreProcessMock(baseDSL.build());
        replayAll();

        ElementDemand cfg = ElementDemand.elem(UserContextField, "b");
        DataElement<? super PreProcessEvent, ?> elem = StandardElementsFactory.createForPreProcessor(cfg);
        assertNotNull(elem);

        SidecarInvocationData sid = new SidecarInvocationData();
        DataElementRelevance der = elem.extract(ppe, sid);

        assertEquals(Invoke, der);
        assertNotNull(sid.getInput().getParams());
        assertNotNull(sid.getInput().getParams().get("userContextField"));
        assertNotNull(allocOrGetParameterGroup(sid.getInput(), "userContextField"));
        assertNull(allocOrGetParameterGroup(sid.getInput(), "userContextField").get("a"));
    }

    @Test
    public void testBasicExtractionWithMissingField() throws DataElementException {
        PreProcessEvent ppe = createPreProcessMock(BasicDSL.make().build());
        replayAll();

        ElementDemand cfg = ElementDemand.elem(UserContextField, "a");
        DataElement<? super PreProcessEvent, ?> elem = StandardElementsFactory.createForPreProcessor(cfg);
        assertNotNull(elem);

        SidecarInvocationData sid = new SidecarInvocationData();
        DataElementRelevance der = elem.extract(ppe, sid);

        assertEquals(Invoke, der);
        assertNotNull(sid.getInput().getParams());
        assertNull(sid.getInput().getParams().get("a"));
    }
}
