package com.airfranceklm.amt.sidecar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class JsonHelperTest {

    Map<String,Object> container;

    @Before
    public void setup() {
        Map<String,String> n2 = new HashMap<>();
        n2.put("n2_a", "45");
        n2.put("n2_b", "46");

        Map<String,Object> n1 = new HashMap<>();
        n1.put("n2", n2);
        n1.put("n1_a", "23");

        container = new HashMap<>();
        container.put("n1", n1);
    }

    @Test
    public void testReplacingPath() throws JsonProcessingException {
        String str = JsonHelper.writeValueAsString(container);
        System.out.println(str);

        final JsonNode jn = JsonHelper.parse(str);
        final ObjectNode on = jn.deepCopy();

        JsonHelper.replacePath(on, "/n1/n2", container);
        System.out.println(JsonHelper.toPrettyJSON(on));

    }
}
