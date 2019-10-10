package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.*;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * HTTP Lambda stack allows using the simple HTTP post and put methods.
 */
public class HTTPSidecarStack implements AFKLMSidecarStack {

    private static String CFG_URI = "uri";
    private static String CFG_COMPRESS = "compression";

    private CloseableHttpClient httpClient;

    public HTTPSidecarStack() {
        // TODO: Configure client for production usage as well.
        httpClient = HttpClients.createDefault();
    }

    @Override
    public SidecarPreProcessorOutput invokeAtPreProcessor(AFKLMSidecarStackConfiguration cfg, SidecarInvocationData cmd, ProcessorServices services) throws IOException {
        String retVal = invoke(cfg, cmd.getInput());
        if (retVal != null) {
            return services.asPreProcessor(retVal);
        } else {
            return null;
        }
    }

    @Override
    public SidecarPostProcessorOutput invokeAtPostProcessor(AFKLMSidecarStackConfiguration cfg, SidecarInvocationData cmd, ProcessorServices services) throws IOException {
        String retVal = invoke(cfg, cmd.getInput());
        if (retVal != null) {
            return services.asPostProcessor(retVal);
        } else {
            return null;
        }
    }

    public String invoke(AFKLMSidecarStackConfiguration cfg, SidecarInput input) throws IOException {
        HTTPLambdaStackConfiguration htCfg = (HTTPLambdaStackConfiguration) cfg;

        HttpPost httpPost = new HttpPost(htCfg.getUri());
        htCfg.forEachHeader(httpPost::addHeader);
//        httpPost.addHeader("content-encoding", "gzip");

        final String str = JsonHelper.toJSON(input);
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
        if (statusCode == 200) {
            return readFully(resp.getEntity());
        } else if (statusCode == 202) {
            return null;
        } else {
            throw new IOException(String.format("Server %s returned unexpected code %d", htCfg.getUri(), statusCode));
        }
    }

    private String readFully(HttpEntity entity) throws IOException {
        if (entity == null) {
            return null;
        } else if (entity.getContentLength() == 0) {
            return null;
        }

        try (InputStream is = entity.getContent()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[10240];
            int k;

            while((k=is.read(buf)) > 0) {
                baos.write(buf, 0, k);
            }

            // TODO: Factor in content-type and encoding.
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public AFKLMSidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return new HTTPLambdaStackConfiguration(cfg.getStackParams());
    }

    static class HTTPLambdaStackConfiguration implements AFKLMSidecarStackConfiguration {
        private String uri;
        private boolean compress = true;
        private Map<String, String> headers;

        HTTPLambdaStackConfiguration(Map<String, String> params) {
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

        boolean supportCompression() {
            return compress;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        void forEachHeader(BiConsumer<String, String> consumer) {
            if (headers != null && headers.size() > 0) {
                headers.forEach(consumer);
            }
        }
    }
}
