package com.airfranceklm.amt.sidecar.model.impl;

import com.airfranceklm.amt.sidecar.config.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.config.SidecarSynchronicity;
import com.airfranceklm.amt.sidecar.impl.model.*;
import com.airfranceklm.amt.sidecar.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.*;

import static com.airfranceklm.amt.sidecar.JsonHelper.fromMap;
import static com.airfranceklm.amt.sidecar.JsonHelper.toPrettyJSON;

public class ExamplesPrinter {
    @Test
    public void printFullSidecarInput() throws JsonProcessingException {
        SidecarInput input = new SidecarInput();
        input.setMasheryMessageId("mashery-message-uuid");
        input.setPoint(SidecarInputPoint.PreProcessor);
        input.setSynchronicity(SidecarSynchronicity.RequestResponse);

        input.setPackageKey("PackageKey");
        input.setServiceId("serviceId");
        input.setEndpointId("endpointId");
        input.setParams(new LinkedHashMap<>());
        input.getParams().put("StringParam", "StringValue");
        input.getParams().put("NumberParam", 42);
        input.getParams().put("BooleanParam", true);

        Map<String,String> complexParams = new HashMap<>();
        complexParams.put("A", "lorem");
        complexParams.put("B", "ypsum");
        input.getParams().put("Object", complexParams);

        input.setRequest(new SidecarInputHTTPMessage());
        input.getRequest().addHeader("content-type", "application/json");
        input.getRequest().addHeader("x-markets", "NL|FR");

        input.getRequest().setPayloadLength(5674);
        input.getRequest().setPayload("This is a short version of string having 5674 bytes");
        input.getRequest().setPayloadBase64Encoded(false);

        input.getRequest().setPayloadExcerpts(new LinkedHashMap<>());
        input.getRequest().getPayloadExcerpts().put("Exp-A", "Extracted portion");

        input.setResponse(new SidecarInputHTTPResponseMessage());
        input.getResponse().setResponseCode(201);
        input.getResponse().setPayload("This is a short version of response having 43964 bytes");
        input.getResponse().setPayloadBase64Encoded(false);
        input.getResponse().setPayloadLength(43964);
        input.getResponse().addHeader("content-type", "application/json");
        input.getResponse().addHeader("x-operational-costs", "B835650-1");

        input.getResponse().setPayloadExcerpts(new LinkedHashMap<>());
        input.getResponse().getPayloadExcerpts().put("Exp-R", "Extracted portion from response");

        input.setEavs(new LinkedHashMap<>());
        input.getEavs().put("Eav_A", "Value_A");
        input.getEavs().put("Eav_B", "Value_B");
        input.getEavs().put("Eav_C", "Value_C");

        input.setPackageKeyEAVs(new LinkedHashMap<>());
        input.getPackageKeyEAVs().put("Eav_D", "Value_D");
        input.getPackageKeyEAVs().put("Eav_E", "Value_E");
        input.getPackageKeyEAVs().put("Eav_F", "Value_F");

        input.setOperation(new SidecarInputOperation());
        input.getOperation().setHttpVerb("GET");
        input.getOperation().setPath("path/to/op");
        input.getOperation().setQuery(new LinkedHashMap<>());
        input.getOperation().getQuery().put("m", "b737-800");
        input.getOperation().getQuery().put("depT", "LFPG");
        input.getOperation().getQuery().put("dest", "EHAM");
        input.getOperation().setUri("https://api-unittest.airfranceklm.com/travel/unittest/path/to/op?m=b737-800&dept=LFPG&dest=EHAM");

        input.setToken(new SidecarInputToken());
        input.getToken().setGrantType("password");
        input.getToken().setScope("Role1 Role2 Role3");
        input.getToken().setUserContext("User Context Value");
        input.getToken().setBearerToken("adfalkdsfjakjfdajlkjrer");
        input.getToken().setExpires(new Date());

        input.setRouting(new SidecarInputRouting());
        input.getRouting().setHttpVerb("GET");
        input.getRouting().setUri("http://klm-backend.departure.klm.cpom/backend-route/path/to/op?m=b737-800&dept=LFPG&dest=EHAM");

        input.setRemoteAddress("192.168.145.32");


        System.out.println(toPrettyJSON(input));
    }

    @Test
    public void testPrintPreProcessorOutput() throws JsonProcessingException {
        Map<String,String> relay = new LinkedHashMap<>();
        relay.put("cache-key", "324kfknkdjkjk5j5");
        relay.put("cache-region", "EU");

        Map<String,String> j = new LinkedHashMap<>();
        j.put("a", "b");
        j.put("c", "d");

        SidecarPreProcessorOutputImpl output = new SidecarPreProcessorOutputImpl();
        output.setTerminate(new SidecarOutputTerminationCommandImpl());
        output.getTerminate().setMessage("Termination message");
        output.getTerminate().setCode(403);
        output.getTerminate().setHeaders(new LinkedHashMap<>());
        output.getTerminate().getHeaders().put("x-afklm-error", "B0-832-J");

        output.getTerminate().setJson(fromMap(j));
        output.getTerminate().setPayload("Custom payload");
        output.getTerminate().setBase64Encoded(false);

        output.setModify(new RequestModificationCommandImpl());
        output.getModify().setCompleteWithCode(201);
        output.getModify().setAddHeaders(new LinkedHashMap<>());
        output.getModify().getAddHeaders().put("x-afklm-level", "44");
        output.getModify().getAddHeaders().put("x-afklm-bearing", "326 degrees of inner turbulence");

        output.getModify().setDropHeaders(new ArrayList<>());
        output.getModify().getDropHeaders().add("authorization");
        output.getModify().getDropHeaders().add("x-afklm-market");

        output.getModify().setPayload("Set replacement payload");
        output.getModify().setBase64Encoded(false);
        output.getModify().setJson(fromMap(j));

        output.getModify().setChangeRoute(new RequestRoutingChangeBeanImpl());
        output.getModify().getChangeRoute().setFile("file?queryString");
        output.getModify().getChangeRoute().setHost("newHost");
        output.getModify().getChangeRoute().setHttpVerb("POST");
        output.getModify().getChangeRoute().setPort(3455);
        output.getModify().getChangeRoute().setUri("http://new-uri:432455/travel/custom/backend");

        output.setUnchangedUntil(new Date());

        output.setRelay(fromMap(relay));

        System.out.println(toPrettyJSON(output));
    }

    @Test
    public void testPrintPostProcessorOutput() throws JsonProcessingException {
        Map<String,String> j = new LinkedHashMap<>();
        j.put("a", "b");
        j.put("c", "d");

        SidecarPostProcessorOutputImpl output = new SidecarPostProcessorOutputImpl();
        output.setTerminate(new SidecarOutputTerminationCommandImpl());
        output.getTerminate().setMessage("Termination message");
        output.getTerminate().setCode(403);
        output.getTerminate().setHeaders(new LinkedHashMap<>());
        output.getTerminate().getHeaders().put("x-afklm-error", "B0-832-J");

        output.getTerminate().setJson(fromMap(j));
        output.getTerminate().setPayload("Custom payload");
        output.getTerminate().setBase64Encoded(false);

        output.setModify(new ResponseModificationCommandImpl());
        output.getModify().setCode(299);

        output.getModify().setAddHeaders(new LinkedHashMap<>());
        output.getModify().getAddHeaders().put("x-afklm-level", "44");
        output.getModify().getAddHeaders().put("x-afklm-bearing", "326 degrees of inner turbulence");

        output.getModify().setDropHeaders(new ArrayList<>());
        output.getModify().getDropHeaders().add("authorization");
        output.getModify().getDropHeaders().add("x-afklm-market");

        output.getModify().setPayload("Set replacement payload");
        output.getModify().setBase64Encoded(false);
        output.getModify().setJson(fromMap(j));

        output.setUnchangedUntil(new Date());

        System.out.println(toPrettyJSON(output));
    }
}
