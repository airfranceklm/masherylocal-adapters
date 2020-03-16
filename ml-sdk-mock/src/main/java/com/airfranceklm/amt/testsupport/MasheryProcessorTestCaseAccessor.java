package com.airfranceklm.amt.testsupport;

import lombok.NonNull;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.airfranceklm.amt.testsupport.Mocks.allocOrGet;

// NOTE: Do not static-import Lombok-generated methods


public class MasheryProcessorTestCaseAccessor {

    private static final Function<MasheryProcessorTestCase, APIClientRequestModel> locateClientRequest =
            (l) -> l != null ? l.getClientRequest() : null;

    private static final Function<MasheryProcessorTestCase, APIOriginInteractionModel> locateOriginInteraction
            = (l) -> l != null ? l.getOriginInteraction() : null;

    private static final Function<MasheryProcessorTestCase, APIClientRequestModel> allocOrGetClientRequest =
            (l) -> allocOrGet(l::getClientRequest, l::setClientRequest, APIClientRequestModel::new);

    private static final Function<MasheryProcessorTestCase, APIOriginInteractionModel> allocOrGetOriginInteraction
            = (l) -> allocOrGet(l::getOriginInteraction, l::setOriginInteraction, APIOriginInteractionModel::new);

    public static MasheryPackageKeyModel locatePackageKey(MasheryProcessorTestCase loc) {

        return locateClientRequest.andThen((am) -> am != null ? am.getApplication() : null)
                .andThen((app) -> app != null ? app.getPackageKeyModel() : null)
                .apply(loc);
    }

    public static MasheryPackageKeyModel allocOrGetPackageKey(MasheryProcessorTestCase loc) {
        return allocOrGetClientRequest
                .andThen((appReq) -> allocOrGet(appReq::getApplication, appReq::setApplication, MasheryApplicationModel::new))
                .andThen((app) -> allocOrGet(app::getPackageKeyModel, app::setPackageKeyModel, MasheryPackageKeyModel::new))
                .apply(loc);
    }

    public static MasheryApplicationModel locateApplicationModel(MasheryProcessorTestCase loc) {
        return locateClientRequest.andThen((am) -> am != null ? am.getApplication() : null)
                .apply(loc);
    }

    public static MasheryApplicationModel allocOrGetApplication(MasheryProcessorTestCase loc) {
        return allocOrGetClientRequest
                .andThen((am) -> allocOrGet(am::getApplication, am::setApplication, MasheryApplicationModel::new))
                .apply(loc);
    }

    public static MasheryAuthorizationContextModel locateAuthorizationContext(MasheryProcessorTestCase loc) {
        return locateClientRequest.andThen((am) -> am != null ? am.getAuthorizationContext() : null)
                .apply(loc);
    }

    public static MasheryAuthorizationContextModel allocOrGetAuthorizationContext(MasheryProcessorTestCase loc) {
        return allocOrGetClientRequest
                .andThen((am) -> allocOrGet(am::getAuthorizationContext, am::setAuthorizationContext, MasheryAuthorizationContextModel::new))
                .apply(loc);
    }

    public static APIOriginRequestModificationModel locateAPIOriginRequestModification(MasheryProcessorTestCase loc) {
        return locateOriginInteraction
                .andThen((inter) -> inter != null ? inter.getRequestModification() : null)
                .apply(loc);
    }

    public static APIOriginRequestModificationModel allocOrGetAPIOriginRequestModification(MasheryProcessorTestCase loc) {
        return allocOrGetOriginInteraction
                .andThen((inter) -> allocOrGet(inter::getRequestModification, inter::setRequestModification, APIOriginRequestModificationModel::new))
                .apply(loc);
    }

    public static APIOriginResponseModel locateAPIOriginResponse(MasheryProcessorTestCase loc) {
        return locateOriginInteraction
                .andThen((inter) -> inter != null ? inter.getResponse() : null)
                .apply(loc);
    }

    public static APIOriginResponseModel allocOrGetAPIOriginResponse(MasheryProcessorTestCase loc) {
        return allocOrGetOriginInteraction
                .andThen((inter) -> allocOrGet(inter::getResponse, inter::setResponse, APIOriginResponseModel::new))
                .apply(loc);
    }

    public static APIOriginInteractionModel allocOrGetApiOriginInteraction(MasheryProcessorTestCase tc) {
        return allocOrGet(tc::getOriginInteraction, tc::setOriginInteraction, APIOriginInteractionModel::new);
    }

    public static APIClientRequestModel allocOrGetAPIClientRequest(MasheryProcessorTestCase tc) {
        return allocOrGet(tc::getClientRequest, tc::setClientRequest, APIClientRequestModel::new);
    }

    public static MasheryDebugContextInteractionModel allocOrGetMasheryDebugContextInteraction(MasheryProcessorTestCase tc) {
        return allocOrGet(tc::getDebugContextInteraction, tc::setDebugContextInteraction, MasheryDebugContextInteractionModel::new);
    }

    public static MasheryCacheInteractionModel allocOrGetCacheInteraction(MasheryProcessorTestCase tc) {
        return allocOrGet(tc::getCacheInteraction, tc::setCacheInteraction, MasheryCacheInteractionModel::new);
    }

    public static APIClientResponseModel allocOrGetApiClientResponse(MasheryProcessorTestCase tc) {
        return allocOrGet(tc::getMasheryResponse, tc::setMasheryResponse, APIClientResponseModel::new);
    }

    // ------------------------------------------------------------------------
    // Builder-stile methods.

    public static <T extends MasheryProcessorTestCase> void buildEndpoint(@NonNull T base
            , @NonNull Consumer<MasheryEndpointModel.MasheryEndpointModelBuilder> cfg) {
        MasheryEndpointModel.MasheryEndpointModelBuilder b =
                base.getEndpoint() != null ? base.getEndpoint().toBuilder()
                        : MasheryEndpointModel.masheryEndpointModel();

        cfg.accept(b);
        base.setEndpoint(b.build());
    }


    public static <T extends MasheryProcessorTestCase> void buildClientRequest(@NonNull T base
            , @NonNull Consumer<APIClientRequestModel.APIClientRequestModelBuilder> cfg) {
        APIClientRequestModel.APIClientRequestModelBuilder b = base.getClientRequest() != null
                ? base.getClientRequest().toBuilder()
                : APIClientRequestModel.apiClientRequest();
        cfg.accept(b);
        base.setClientRequest(b.build());
    }

    public static <T extends MasheryProcessorTestCase> void buildAuthorizationContext(@NonNull T base
            , @NonNull Consumer<MasheryAuthorizationContextModel.MasheryAuthorizationContextModelBuilder> cfg) {
        APIClientRequestModel acrm = allocOrGetAPIClientRequest(base);
        MasheryAuthorizationContextModel.MasheryAuthorizationContextModelBuilder b = acrm.getAuthorizationContext() != null ?
                acrm.getAuthorizationContext().toBuilder()
                : MasheryAuthorizationContextModel.authorizationContext();

        cfg.accept(b);
        acrm.setAuthorizationContext(b.build());
    }

    public static <T extends MasheryProcessorTestCase> void buildApplication(@NonNull T base
            , @NonNull Consumer<MasheryApplicationModel.MasheryApplicationModelBuilder> cfg) {
        APIClientRequestModel acrm = allocOrGetAPIClientRequest(base);
        MasheryApplicationModel.MasheryApplicationModelBuilder b = acrm.getApplication() != null ?
                acrm.getApplication().toBuilder()
                : MasheryApplicationModel.masheryApplication();

        cfg.accept(b);
        acrm.setApplication(b.build());
    }

    public static <T extends MasheryProcessorTestCase> void buildPackageKey(@NonNull T base
            , @NonNull Consumer<MasheryPackageKeyModel.MasheryPackageKeyModelBuilder> c) {
        MasheryApplicationModel mam = allocOrGetApplication(base);

        MasheryPackageKeyModel.MasheryPackageKeyModelBuilder builder = mam.getPackageKeyModel() != null
                ? mam.getPackageKeyModel().toBuilder()
                : MasheryPackageKeyModel.packageKey();
        c.accept(builder);
        mam.setPackageKeyModel(builder.build());
    }

    public static <T extends MasheryProcessorTestCase> void buildAPIOriginInteraction(@NonNull T base
            , @NonNull Consumer<APIOriginInteractionModel.APIOriginInteractionModelBuilder> cfg) {

        APIOriginInteractionModel.APIOriginInteractionModelBuilder b =
                base.getOriginInteraction() != null ? base.getOriginInteraction().toBuilder()
                        : APIOriginInteractionModel.apiOriginInteraction();

        cfg.accept(b);
        base.setOriginInteraction(b.build());
    }

    public static <T extends MasheryProcessorTestCase> void buildAPIOriginRequestModification(@NonNull T base
            , @NonNull Consumer<APIOriginRequestModificationModel.APIOriginRequestModificationModelBuilder> cfg) {

        APIOriginInteractionModel aim = allocOrGetApiOriginInteraction(base);

        APIOriginRequestModificationModel.APIOriginRequestModificationModelBuilder b =
                aim.getRequestModification() != null ? aim.getRequestModification().toBuilder()
                        : APIOriginRequestModificationModel.originRequestModification();

        cfg.accept(b);
        aim.setRequestModification(b.build());
    }

    public static <T extends MasheryProcessorTestCase> void buildAPIOriginResponse(@NonNull T base
            , @NonNull Consumer<APIOriginResponseModel.APIOriginResponseModelBuilder> cfg) {

        APIOriginInteractionModel aim = allocOrGetApiOriginInteraction(base);

        APIOriginResponseModel.APIOriginResponseModelBuilder b =
                aim.getResponse() != null ? aim.getResponse().toBuilder()
                        : APIOriginResponseModel.apiOriginResponse();

        cfg.accept(b);
        aim.setResponse(b.build());
    }

    public static <T extends MasheryProcessorTestCase> void buildMasheryResponse(@NonNull T base
    , @NonNull Consumer<APIClientResponseModel.APIClientResponseModelBuilder> cfg) {
        APIClientResponseModel.APIClientResponseModelBuilder b =
                base.getMasheryResponse() != null ?
                        base.getMasheryResponse().toBuilder() :
                        APIClientResponseModel.masheryResponse();
        cfg.accept(b);
        base.setMasheryResponse(b.build());
    }

    public static <T extends MasheryProcessorTestCase> void buildDebugContext(@NonNull T base
    , @NonNull Consumer<MasheryDebugContextInteractionModel.MasheryDebugContextInteractionModelBuilder> cfg) {

        MasheryDebugContextInteractionModel.MasheryDebugContextInteractionModelBuilder b =
                base.getDebugContextInteraction() != null ?
                        base.getDebugContextInteraction().toBuilder() :
                        MasheryDebugContextInteractionModel.debugContext();

        cfg.accept(b);
        base.setDebugContextInteraction(b.build());

    }

    public static <T extends MasheryProcessorTestCase> void buildCache(@NonNull T base
    , @NonNull Consumer<MasheryCacheInteractionModel.MasheryCacheInteractionModelBuilder> cfg) {
        MasheryCacheInteractionModel.MasheryCacheInteractionModelBuilder b =
                base.getCacheInteraction() != null ?
                        base.getCacheInteraction().toBuilder() :
                        MasheryCacheInteractionModel.masheryCache();

        cfg.accept(b);
        base.setCacheInteraction(b.build());
    }

    public static <T extends MasheryProcessorTestCase> void synchPayloadLengths(@NonNull T base) {
        if (base.getClientRequest() != null) {
            base.getClientRequest().syncPayloadLength();
        }

        APIOriginResponseModel resp = locateAPIOriginResponse(base);
        if (resp != null) {
            resp.syncPayloadLength();
        }
    }
}
