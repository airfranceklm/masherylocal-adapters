package com.airfranceklm.amt.sidecar.config;

import com.airfranceklm.amt.sidecar.impl.model.SidecarPreProcessorOutputImpl;
import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import org.junit.Test;

import java.util.Iterator;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.*;
import static junit.framework.Assert.*;

public class YamlSidecarPreProcessorOutputBuildingTest
        extends CommonYamlSidecarOutputBuildingTest<SidecarPreProcessorOutput> {

    @Test
    public void testLoadingTerminateCommand() {
        super.testAllTerminateCommand();
    }

    @Test
    public void testLoadingModifyCommand() {
        super.testLoadingCommonModifyCommand();
        testModifyLoadingChangeRoute();
        testModifyLoadingCompleted();
    }


    @Test
    public void testModifyLoadingChangeRoute() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/pre/changeRoute.yaml");
        assertNotNull(yamlDocs);

        SidecarPreProcessorOutput so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getModify());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNotNull(so.getModify());
        assertNotNull(so.getModify().getChangeRoute());
        assertEquals("aHost", so.getModify().getChangeRoute().getHost());
        assertEquals("aFile", so.getModify().getChangeRoute().getFile());
        assertEquals("get", so.getModify().getChangeRoute().getHttpVerb());
        assertEquals("aUri", so.getModify().getChangeRoute().getUri());
        assertEquals(new Integer(4567), so.getModify().getChangeRoute().getPort());

        // All nested elements are null in this scenario.
        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getModify());
    }

    @Test
    public void testLoadingRelayParams() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/pre/relayParams.yaml");
        assertNotNull(yamlDocs);

        SidecarPreProcessorOutput so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getRelayParams());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNotNull(so.getRelayParams());
        assertEquals(3, so.getRelayParams().size());
        assertEquals("string", so.getRelayParams().get("a"));
        assertEquals(32, so.getRelayParams().get("b"));
        assertEquals(false, so.getRelayParams().get("c"));
    }

    //--------------------------------------------------------
    // Modification part.





    @Test
    public void testModifyLoadingCompleted() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/pre/completed.yaml");
        assertNotNull(yamlDocs);

        // Case 1: empty code
        SidecarPreProcessorOutput so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getModify());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getModify());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNotNull(so.getModify());
        assertEquals(new Integer(202), so.getModify().getCompleteWithCode());
    }



    protected SidecarPreProcessorOutputImpl getNextSidecarOutput(Iterator<Object> yamlDocs) {
        return buildSidecarPreProcessorOutputFromYAML(nextYamlDocument(yamlDocs));
    }


}
