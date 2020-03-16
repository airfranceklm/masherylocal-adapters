package com.airfranceklm.amt.testsupport.mocks;

import com.mashery.http.server.HTTPServerResponse;
import com.mashery.trafficmanager.model.core.TrafficManagerResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseFeature.MASHERY_MESSAGE_ID;
import static com.airfranceklm.amt.testsupport.MasheryProcessorTestCaseFeature.MASHERY_MESSAGE_ID_HEADER;

@Builder
@AllArgsConstructor
public class TrafficManagerResponseImpl implements TrafficManagerResponse {

    @Getter
    @Builder.Default
    private boolean complete = false;
    @Getter
    private boolean failed;
    @Getter
    private String message;

    @Getter
    @Setter
    private HTTPServerResponse HTTPResponse;

    public TrafficManagerResponseImpl() {
        MutableHTTPHeadersImpl header = MutableHTTPHeadersImpl
                .builder()
                .entry(MASHERY_MESSAGE_ID_HEADER, MASHERY_MESSAGE_ID)
                .build();


        this.HTTPResponse = HTTPServerResponseImpl.builder()
                .headers(header)
                .build();
    }


    @Override
    public void setComplete() {
        this.complete = true;
    }

    @Override
    public void setComplete(String s) {
        this.complete = true;
        this.message = s;

    }

    @Override
    public void setFailed(String s) {
        this.complete = true;
        this.failed = true;
        this.message = s;
    }

    @Override
    public void setComplete(boolean b) {
        this.complete = b;
    }
}
