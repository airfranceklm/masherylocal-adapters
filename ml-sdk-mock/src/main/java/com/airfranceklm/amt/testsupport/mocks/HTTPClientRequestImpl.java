package com.airfranceklm.amt.testsupport.mocks;

import com.mashery.http.MutableHTTPHeaders;
import com.mashery.http.client.HTTPClientRequest;
import com.mashery.http.client.HTTPClientResponse;
import com.mashery.http.io.ContentProducer;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.net.Proxy;

import static org.junit.Assert.fail;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class HTTPClientRequestImpl extends HTTPClientRequest {

    private String URI;
    private String method;
    private ContentProducer body;

    private Integer connectTimeout;
    private Integer readTimeout;
    private Integer maxRedirects;
    private Proxy proxy;
    private HostnameVerifier hostnameVerifier;

    private javax.net.ssl.SSLSocketFactory SSLSocketFactory;
    private Metrics metrics;

    private MutableHTTPHeaders headers;
    private String version;

    @Override
    public HTTPClientResponse send() {
        fail("send() on the API Origin Request must be definde via a mock");
        throw new IllegalStateException("Missing mock definition for send()");
    }
}
