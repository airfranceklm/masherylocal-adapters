package com.airfranceklm.amt.testsupport.mocks;

import com.mashery.http.MutableHTTPHeaders;
import com.mashery.http.io.ContentProducer;
import com.mashery.http.io.ContentSource;
import com.mashery.http.server.HTTPServerResponse;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HTTPServerResponseImpl extends HTTPServerResponse {
    @Getter @Setter
    int statusCode;

    @Getter @Setter
    String statusMessage;

    @Getter @Setter
    ContentProducer body;

    @Getter @Setter
    String version;

    @Getter @Setter
    MutableHTTPHeaders headers;
}
