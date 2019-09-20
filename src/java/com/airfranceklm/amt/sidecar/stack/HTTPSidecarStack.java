package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.*;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.airfranceklm.amt.sidecar.AFKLMSidecarProcessor.toSidecarOutput;

/**
 * HTTP Lambda stack allows using the simple HTTP post and put methods.
 */
public class HTTPSidecarStack implements AFKLMSidecarStack {

    private static String CFG_URI = "uri";
    private static String CFG_COMPRESS = "compression";
    private static final SidecarOutput emptyResponse = new SidecarOutputImpl();

    private CloseableHttpClient httpClient;

    public HTTPSidecarStack() {
        // TODO: Configure client for production usage as well.
        httpClient = HttpClients.createDefault();
    }

    @Override
    public SidecarOutput invoke(AFKLMSidecarStackConfiguration cfg, SidecarInput input) throws IOException {
        HTTPLambdaStackConfiguration htCfg = (HTTPLambdaStackConfiguration) cfg;

        HttpPost httpPost = new HttpPost(htCfg.getUri());
        htCfg.forEachHeader(httpPost::addHeader);
//        httpPost.addHeader("content-encoding", "gzip");

        final String str = AFKLMSidecarProcessor.toJSON(input);
        StringEntity se = new StringEntity(str, StandardCharsets.UTF_8);

        if (htCfg.supportCompression()) {
            httpPost.setHeader("Content-Encoding", "gzip");
            httpPost.setEntity(new GzipCompressingEntity(se));
        } else {
            httpPost.setEntity(se);
        }

        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Accept-Charset", "utf-8");
        httpPost.setHeader("Accept-Encoding", "gzip");
        httpPost.setHeader("Content-type", "application/json; charset=UTF-8");

        CloseableHttpResponse resp = httpClient.execute(httpPost);

        final int statusCode = resp.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new IOException(String.format("Server %s returned unexpected code %d", htCfg.getUri(), statusCode));
        } else if (statusCode == 202) {
            return emptyResponse;
        } else {
            return AFKLMSidecarProcessor.toSidecarOutput(resp.getEntity().getContent(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public AFKLMSidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return new HTTPLambdaStackConfiguration(cfg.getStackParams());
    }

    class HTTPLambdaStackConfiguration implements AFKLMSidecarStackConfiguration {
        private String uri;
        private boolean compress = true;
        private Map<String, String> headers;

        public HTTPLambdaStackConfiguration(Map<String, String> params) {
            headers = new HashMap<>();
            params.forEach((key, value) -> {
                if (CFG_URI.equalsIgnoreCase(key)) {
                    this.uri = value;
                } else if (CFG_COMPRESS.equalsIgnoreCase(key)) {
                    this.compress = Boolean.parseBoolean(value);
                } else{
                    headers.put(key, value);
                }
            });
        }

        @Override
        public boolean isValid() {
            return uri != null;
        }

        public String getUri() {
            return uri;
        }

        public boolean supportCompression() {
            return compress;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void forEachHeader(BiConsumer<String, String> consumer) {
            if (headers != null && headers.size() > 0) {
                headers.forEach(consumer);
            }
        }
    }
}
