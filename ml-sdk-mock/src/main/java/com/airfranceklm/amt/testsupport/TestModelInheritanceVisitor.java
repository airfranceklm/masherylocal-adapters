package com.airfranceklm.amt.testsupport;

import java.util.function.Consumer;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.*;
import static com.airfranceklm.amt.testsupport.Mocks.allocOrGet;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class TestModelInheritanceVisitor<T extends MasheryProcessorTestCase> implements TestModelVisitor<T> {
    MasheryProcessorTestSuite<T> suite;

    public TestModelInheritanceVisitor(MasheryProcessorTestSuite<T> suite) {
        this.suite = suite;
    }

    private <M> void consumeNonNull(M object, Consumer<M> c) {
        if (object != null) {
            c.accept(object);
        }
    }

    private void ifCopyRequired(RequestCaseDatum rcd, Consumer<T> c) {
        if (rcd.needsCopyFromAnotherCase()) {
            suite.forExistingCase(rcd.getAsIn(), c);
        }
    }

    @Override
    public void visit(MasheryPackageKeyModel v) {
        ifCopyRequired(v, (tc) -> consumeNonNull(locatePackageKey(tc), v::deepCopyFrom));
    }

    @Override
    public void visit(MasheryEndpointModel v) {
        // Do nothing: the model is defined
    }

    @Override
    public void visit(MasheryAuthorizationContextModel v) {
        ifCopyRequired(v, (tc) -> consumeNonNull(locateAuthorizationContext(tc), v::deepCopyFrom));
    }

    @Override
    public void visit(MasheryApplicationModel v) {
        ifCopyRequired(v, (tc) -> consumeNonNull(locateApplicationModel(tc), v::deepCopyFrom));
    }

    @Override
    public void visit(APIClientRequestModel v) {
        ifCopyRequired(v, (tc) -> consumeNonNull(tc.getClientRequest(), v::deepCopyFrom));
    }

    @Override
    public void visit(APIOriginInteractionModel v) {
        ifCopyRequired(v, (tc) -> consumeNonNull(tc.getOriginInteraction(), v::deepCopyFrom));
    }

    @Override
    public void visit(APIOriginRequestModificationModel v) {
        ifCopyRequired(v, (tc) -> consumeNonNull(locateAPIOriginRequestModification(tc), v::deepCopyFrom));
    }

    @Override
    public void visit(APIOriginResponseModel v) {
        ifCopyRequired(v, (tc) -> consumeNonNull(locateAPIOriginResponse(tc), v::deepCopyFrom));
    }

    @Override
    public void visit(MasheryDebugContextInteractionModel v) {
        ifCopyRequired(v, (tc) -> consumeNonNull(tc.getDebugContextInteraction(), v::deepCopy));
    }

    @Override
    public void visit(MasheryCacheInteractionModel v) {
        ifCopyRequired(v, (tc) -> consumeNonNull(tc.getCacheInteraction(), v::deepCopyFrom));
    }

    @Override
    public void visit(APIClientResponseModel v) {
        ifCopyRequired(v, (tc) -> consumeNonNull(tc.getMasheryResponse(), v::deepCopyFrom));
    }

    @Override
    public void visit(T tc) {
        T other = suite.getFirst();
        allocOrGet(tc::getEndpoint, tc::setEndpoint, MasheryEndpointModel::new).deepCopyFrom(suite.getEndpointModel());

        if (tc.getInherited() != null) {
            tc.getInherited().forEach((i) -> {
                doInherit(tc, other, i);
            });
        }
    }

    protected void doInherit(T self, T other, String feature) {
        switch (feature) {
            case MasheryProcessorTestCaseFeature.CLIENT_REQUEST:
                assertNotNull(other.getClientRequest());
                allocOrGetAPIClientRequest(self)
                        .inheritFrom(other.getClientRequest());
            break;

            case MasheryProcessorTestCaseFeature.CLIENT_HTTP_REQUEST:
                assertNotNull(other.getClientRequest());
                allocOrGetAPIClientRequest(self)
                        .inheritClientHTTPRequestFrom(other.getClientRequest());
                break;

            case MasheryProcessorTestCaseFeature.DEBUG_CONTEXT:
                assertNotNull(other.getDebugContextInteraction());
                allocOrGetMasheryDebugContextInteraction(self)
                        .inheritFrom(other.getDebugContextInteraction());
            break;

            case MasheryProcessorTestCaseFeature.CACHE:
                assertNotNull(other.getCacheInteraction());
                allocOrGetCacheInteraction(self)
                        .inheritFrom(other.getCacheInteraction());
            break;

            case MasheryProcessorTestCaseFeature.MASHERY_RESPONSE:
                assertNotNull(other.getMasheryResponse());
                allocOrGetApiClientResponse(self)
                        .inheritFrom(other.getMasheryResponse());
            break;

            case MasheryProcessorTestCaseFeature.APPLICATION:
                MasheryApplicationModel mdl = locateApplicationModel(other);
                assertNotNull(mdl);
                allocOrGetApplication(self).inheritFrom(mdl);
            break;

            case MasheryProcessorTestCaseFeature.PACKAGE_KEY:
                MasheryPackageKeyModel pkMld = locatePackageKey(other);
                assertNotNull(pkMld);
                allocOrGetPackageKey(self).inheritFrom(pkMld);
            break;

            case MasheryProcessorTestCaseFeature.AUTHORIZATION_CTX:
                MasheryAuthorizationContextModel actxMld = locateAuthorizationContext(other);
                assertNotNull(actxMld);
                allocOrGetAuthorizationContext(self).inheritFrom(actxMld);
            break;

            case MasheryProcessorTestCaseFeature.ORIGIN_INTERACTION:
                assertNotNull(other.getOriginInteraction());
                allocOrGetApiOriginInteraction(self)
                        .inheritFrom(other.getOriginInteraction());
            break;

            case MasheryProcessorTestCaseFeature.ORIGIN_REQUEST_MOD:
                APIOriginRequestModificationModel origModifMdl = locateAPIOriginRequestModification(other);
                assertNotNull(origModifMdl);
                allocOrGetAPIOriginRequestModification(self).inheritFrom(origModifMdl);
            break;

            case MasheryProcessorTestCaseFeature.ORIGIN_RESPONSE:
                APIOriginResponseModel origRespMdl = locateAPIOriginResponse(other);
                assertNotNull(origRespMdl);
                allocOrGetAPIOriginResponse(self).inheritFrom(origRespMdl);
            break;

            default:
                fail(String.format("Don't know how to handle features %s", feature));
        }
    }

}
