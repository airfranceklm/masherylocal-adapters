package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.testsupport.MasheryProcessorTestSuite;
import com.airfranceklm.amt.testsupport.TestModelInheritanceVisitor;

import static com.airfranceklm.amt.testsupport.Mocks.copyIfNull;

public class SidecarTestModelInheritanceVisitor extends TestModelInheritanceVisitor<SidecarInvocationTestCase> {

    public static final String FEATURE_SIDECAR_PREPOCESS_REQ = "sidecar pre-processor request";
    public static final String FEATURE_SIDECAR_PREFLIGHT_REQ = "sidecar preflight request";
    public static final String FEATURE_SIDECAR_POSTPROCES_REQ = "sidecar post-processor request";

    public SidecarTestModelInheritanceVisitor(MasheryProcessorTestSuite<SidecarInvocationTestCase> suite) {
        super(suite);
    }

    @Override
    protected void doInherit(SidecarInvocationTestCase self, SidecarInvocationTestCase other, String feature) {
        switch (feature) {
            case FEATURE_SIDECAR_PREPOCESS_REQ:
                copyIfNull(self::getPreProcessorInput
                        , other::getPreProcessorInput
                        , (obj) -> self.setPreProcessorInput(SidecarInput.Accessor.deepClone(obj)));
                break;
            case FEATURE_SIDECAR_PREFLIGHT_REQ:
                copyIfNull(self::getPreflightInput
                        , other::getPreflightInput
                        , (obj) -> self.setPreflightInput(SidecarInput.Accessor.deepClone(obj)));
                break;
            case FEATURE_SIDECAR_POSTPROCES_REQ:
                copyIfNull(self::getPostProcessorInput
                        , other::getPostProcessorInput
                        , (obj) -> self.setPostProcessorInput(SidecarInput.Accessor.deepClone(obj)));
                break;
            default:
                super.doInherit(self, other, feature);
        }
    }
}
