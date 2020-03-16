package com.airfranceklm.amt.sidecar.model.json;

import com.airfranceklm.amt.sidecar.JsonHelper;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarSynchronicity;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import static com.airfranceklm.amt.sidecar.model.json.JsonAbstractSidecarOutput.allocOrGetTerminate;

public class SampleOut {
    @Test
    public void sampleOutputTest() throws JsonProcessingException {
        JsonSidecarPreProcessorOutput o = new JsonSidecarPreProcessorOutput();
        allocOrGetTerminate(o).with((c) -> {
            c.setStatusCode(400);
            c.setMessage("Bad request");
        });

        System.out.println(JsonHelper.toPrettyJSON(o));
    }

}
