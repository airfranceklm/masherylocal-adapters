package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.testsupport.MasheryProcessorTestSuite;
import com.airfranceklm.amt.testsupport.TestModelVisitor;
import com.fasterxml.jackson.core.type.TypeReference;

import static org.junit.Assert.assertNotNull;

public class SidecarInvocationTestSuite extends MasheryProcessorTestSuite<SidecarInvocationTestCase> {

    public SidecarInvocationTestSuite() {
    }

    public SidecarInvocationTestSuite(Class<?> resource, String resName) {
        super(resource, resName);
    }

    @Override
    protected TypeReference<SidecarInvocationTestCase> getTestCaseTypeReference() {
        return new TypeReference<SidecarInvocationTestCase>() {};
    }

    @Override
    protected TestModelVisitor<SidecarInvocationTestCase> createInheritanceVisitor() {
        return new SidecarTestModelInheritanceVisitor(this);
    }


}
