package com.airfranceklm.amt.testsupport;

public interface TestModelVisitor<T extends MasheryProcessorTestCase> {

     void visit(MasheryPackageKeyModel v);
     void visit(MasheryEndpointModel v);
     void visit(MasheryAuthorizationContextModel v);
     void visit(MasheryApplicationModel v);
     void visit(APIClientRequestModel v);
     void visit(APIOriginInteractionModel v);
     void visit(APIOriginRequestModificationModel v);
     void visit(APIOriginResponseModel v);

    void visit(MasheryDebugContextInteractionModel masheryDebugContextInteractionModel);

     void visit(MasheryCacheInteractionModel masheryCacheInteractionModel);

    void visit(T masheryProcessorTestCase);

    void visit(APIClientResponseModel apiClientResponseModel);
}
