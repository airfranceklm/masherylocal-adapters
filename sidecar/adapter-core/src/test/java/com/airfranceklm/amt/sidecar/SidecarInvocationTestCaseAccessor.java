package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarInputHTTPMessage;
import com.airfranceklm.amt.sidecar.model.SidecarInputHTTPResponseMessage;
import com.airfranceklm.amt.sidecar.model.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.model.json.JsonSidecarPreProcessorOutput;

import java.util.function.Consumer;

public class SidecarInvocationTestCaseAccessor {

    public static SidecarInputHTTPMessage sidecarHTTPInput(Consumer<SidecarInputHTTPMessage.SidecarInputHTTPMessageBuilder> c) {
        final SidecarInputHTTPMessage.SidecarInputHTTPMessageBuilder b = SidecarInputHTTPMessage.buildHTTPInputMessage();
        c.accept(b);
        return b.build();
    }

    public static SidecarInputHTTPResponseMessage sidecarHTTPOutput(Consumer<SidecarInputHTTPResponseMessage.SidecarInputHTTPResponseMessageBuilder> c) {
        final SidecarInputHTTPResponseMessage.SidecarInputHTTPResponseMessageBuilder b = SidecarInputHTTPResponseMessage.buildHTTPResponseMessage();
        c.accept(b);
        return b.build();
    }

    public static void buildPreflightInput(SidecarInvocationTestCase tc, Consumer<SidecarInput.SidecarInputBuilder> b) {
        SidecarInput.SidecarInputBuilder bld = tc.allocOrGetPreflightInput(true).toBuilder();

        bld.point(SidecarInputPoint.Preflight);
        b.accept(bld);
        tc.getPreflightInteraction().setInput(bld.build());
    }

    public static void buildPreProcessorInput(SidecarInvocationTestCase tc, Consumer<SidecarInput.SidecarInputBuilder> b) {
        SidecarInput.SidecarInputBuilder bld = tc.allocOrGetPreProcessorInput(true)
                .toBuilder();

        bld.point(SidecarInputPoint.PreProcessor);
        b.accept(bld);
        tc.getPreProcessorInteraction().setInput(bld.build());
    }

    public static void buildPostProcessorInput(SidecarInvocationTestCase tc, Consumer<SidecarInput.SidecarInputBuilder> b) {
        SidecarInput.SidecarInputBuilder bld = tc.allocOrGetPostProcessorInput(true)
                .toBuilder();

        bld.point(SidecarInputPoint.PostProcessor);
        b.accept(bld);
        tc.getPostProcessorInteraction().setInput(bld.build());
    }

    public static void buildPreflightOutput(SidecarInvocationTestCase tc, Consumer<JsonSidecarPreProcessorOutput> c) {
        c.accept(tc.allocOrGetPreflightOutput());
    }
}
