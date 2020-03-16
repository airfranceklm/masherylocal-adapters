package com.airfranceklm.amt.testsupport;

import com.airfranceklm.amt.testsupport.mocks.CacheImpl;
import com.airfranceklm.amt.testsupport.mocks.DebugContextImpl;
import com.airfranceklm.amt.testsupport.mocks.HTTPServerResponseImpl;
import com.airfranceklm.amt.testsupport.mocks.TrafficManagerResponseImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mashery.http.client.HTTPClientRequest;
import com.mashery.http.server.HTTPServerRequest;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import com.mashery.trafficmanager.model.core.APICall;
import com.mashery.trafficmanager.model.core.ApplicationRequest;
import com.mashery.trafficmanager.model.core.TrafficManagerResponse;
import lombok.NonNull;
import org.easymock.EasyMockSupport;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.locateAPIOriginResponse;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseAccessor.synchPayloadLengths;
import static org.easymock.EasyMock.expect;

public class MasheryProcessorTestSupport<T extends MasheryProcessorTestCase> extends EasyMockSupport {

    // -----------------------------------------------------
    // Methods to be overridden

    protected PreProcessEvent createPreProcessMock(@NonNull T caseData) {
        synchPayloadLengths(caseData);

        TestContext<T> ctx = new TestContext<>(this, caseData);

        PreProcessEvent retVal = createMock(PreProcessEvent.class);
        expect(retVal.getType()).andReturn(PreProcessEvent.EVENT_TYPE).anyTimes();

        mockCommonEventMethods(ctx, retVal);

        if (caseData.getClientRequest() != null) {
            final HTTPServerRequest serverReq = ctx.allocOrGetServerRequest(() -> caseData.getClientRequest().mock(ctx));
            final HTTPClientRequest clReq = ctx.allocOrGetAPIOriginRequest(() -> caseData.getClientRequest().mockOriginRequest(ctx));

            expect(retVal.getServerRequest()).andReturn(serverReq).anyTimes();
            expect(retVal.getClientRequest()).andReturn(clReq).anyTimes();
        }


        return retVal;
    }

    protected PostProcessEvent createPostProcessMock(@NonNull T caseData) {
        synchPayloadLengths(caseData);

        TestContext<T> ctx = new TestContext<>(this, caseData);

        PostProcessEvent retVal = createMock(PostProcessEvent.class);
        expect(retVal.getType()).andReturn(PostProcessEvent.EVENT_TYPE).anyTimes();

        mockCommonEventMethods(ctx, retVal);

        expect(retVal.getServerResponse()).andReturn(ctx.getHttpServerResponse()).anyTimes();

        APIOriginResponseModel resp = locateAPIOriginResponse(caseData);
        if (resp != null) {
            expect(retVal.getClientResponse()).andReturn(resp.mock(ctx)).anyTimes();
        }

        return retVal;
    }

    protected APICall mockAPICallContext(TestContext<T> ctx) {
        APICall callMock = createMock(APICall.class);

        if (ctx.getTestCase().getClientRequest() != null) {
            final ApplicationRequest appReq = ctx.getTestCase().getClientRequest().mockApplicationRequest(ctx);
            expect(callMock.getRequest()).andReturn(appReq).anyTimes();
        }

        TrafficManagerResponse tmr;
        if (ctx.getTestCase().getMasheryResponse() != null) {
            tmr = ctx.getTestCase().getMasheryResponse().mockMasheryResponse(ctx);
        } else {
            tmr = new TrafficManagerResponseImpl();
            ctx.setHttpServerResponse(tmr.getHTTPResponse());
        }

        expect(callMock.getResponse()).andReturn(tmr).anyTimes();

        return callMock;
    }


    protected void mockCommonEventMethods(TestContext<T> ctx, ProcessorEvent mock) {
        expect(mock.getCallContext()).andReturn(mockAPICallContext(ctx)).anyTimes();

        final MasheryEndpointModel endp = ctx.getTestCase().getEndpoint();
        final APIClientRequestModel clReq = ctx.getTestCase().getClientRequest();

        if (endp != null) {
            expect(mock.getEndpoint()).andReturn(endp.mock(ctx.getOwner())).anyTimes();
        }

        if (clReq != null) {
            if (clReq.getApplication() != null) {
                expect(mock.getKey())
                        .andReturn(clReq.getApplication().mockPackageKey(ctx.getOwner()))
                        .anyTimes();
            } else {
                expect(mock.getKey()).andReturn(null).anyTimes();
            }

            if (clReq.getAuthorizationContext() != null) {
                expect(mock.getAuthorizationContext())
                        .andReturn(clReq.getAuthorizationContext().mock(ctx.getOwner()))
                        .anyTimes();
            } else {
                expect(mock.getAuthorizationContext()).andReturn(null).anyTimes();
            }
        } else {
            // If a client request was not provided, we'll mock the methods anyway, but these
            // would return null.
            expect(mock.getAuthorizationContext())
                    .andReturn(null)
                    .anyTimes();

            expect(mock.getKey())
                    .andReturn(null)
                    .anyTimes();
        }

        // If cache interactions are not defined, then the code should
        // not be making any use of the caching.
        if (ctx.getTestCase().getCacheInteraction() != null) {
            expect(mock.getCache())
                    .andReturn(ctx.getTestCase().getCacheInteraction().mock(ctx.getOwner()))
                    .anyTimes();
        } else {
            expect(mock.getCache()).andReturn(new CacheImpl(null)).anyTimes();
        }

        // If the debug context interactions are not defined, the code should not be making
        // any use of the debug context.
        if (ctx.getTestCase().getDebugContextInteraction() != null) {
            expect(mock.getDebugContext())
                    .andReturn(ctx.getTestCase().getDebugContextInteraction().mock(ctx.getOwner()))
                    .anyTimes();
        } else {
            expect(mock.getDebugContext()).andReturn(new DebugContextImpl(null)).anyTimes();
        }
    }



}
