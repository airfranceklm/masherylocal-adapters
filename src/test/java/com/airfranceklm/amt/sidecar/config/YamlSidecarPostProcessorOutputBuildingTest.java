package com.airfranceklm.amt.sidecar.config;

import com.airfranceklm.amt.sidecar.model.SidecarPostProcessorOutput;
import org.junit.Test;

import java.util.Iterator;

import static com.airfranceklm.amt.sidecar.config.YamlConfigurationBuilder.*;
import static junit.framework.Assert.*;

public class YamlSidecarPostProcessorOutputBuildingTest extends CommonYamlSidecarOutputBuildingTest<SidecarPostProcessorOutput> {

    @Test
    public void testLoadingTerminateCommand() {
        super.testAllTerminateCommand();
    }

    @Test
    public void testLoadingModifyCommand() {
        testLoadingCommonModifyCommand();

    }

    @Test
    public void testModifyLoadingCode() {
        Iterator<Object> yamlDocs = loadAllYamlDocuments(getClass(), "/yaml/sidecarOutput/post/code.yaml");
        assertNotNull(yamlDocs);

        SidecarPostProcessorOutput so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getModify());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNotNull(so.getModify());
        assertEquals(301, so.getModify().getCode().intValue());

        so = getNextSidecarOutput(yamlDocs);
        assertNotNull(so);
        assertNull(so.getModify());
    }

    @Override
    protected SidecarPostProcessorOutput getNextSidecarOutput(Iterator<Object> yamlDocs) {
        return buildSidecarPostProcessorOutputFromYAML(nextYamlDocument(yamlDocs));
    }
}
