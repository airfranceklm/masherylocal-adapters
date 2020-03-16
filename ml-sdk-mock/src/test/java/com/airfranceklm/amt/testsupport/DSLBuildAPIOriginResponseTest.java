package com.airfranceklm.amt.testsupport;

import com.airfranceklm.amtml.payload.PayloadOperations;
import com.mashery.http.io.ContentSource;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.buildAPIOriginResponse;
import static org.junit.Assert.*;

public class DSLBuildAPIOriginResponseTest extends MasheryProcessorTestSupport<MasheryProcessorTestCase> {

    DSL<MasheryProcessorTestCase> dsl;

    @Before
    public void initDsl() {
        dsl =  BasicDSL.make().expr((tc) -> {
            buildAPIOriginResponse(tc, (cfg) -> cfg.statusCode(201)
                    .header("content-type", "application/json")
                    .payload("STRING+PAYLOAD"));
        });
    }

    @Test
    public void testPostProcessorEvent() throws IOException {
        PostProcessEvent ppe = createPostProcessMock(dsl.build());
        replayAll();

        assertNotNull(ppe);
        assertNotNull(ppe.getClientResponse());
        assertNotNull(ppe.getClientResponse().getBody());

        ContentSource cs = ppe.getClientResponse().getBody();
        assertFalse(cs.isRepeatable());
        assertEquals("STRING+PAYLOAD", PayloadOperations.getContentOf(cs));

        verifyAll();
    }

}
