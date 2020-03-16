package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarSynchronicity;
import com.airfranceklm.amt.sidecar.model.alcp.EncryptedMessage;
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
public class HTTPSidecarStack extends ALCPEnabledStack {

    public static final String STACK_NAME = "http";

    public static String CFG_URI = "uri";
    public static String CFG_COMPRESS = "compression";

    private CloseableHttpClient httpClient;

    public HTTPSidecarStack() {
        httpClient = HttpClients.createDefault();
    }

    @Override
    public String getStackName() {
        return STACK_NAME;
    }

    @Override
    protected <TProtectedIn, TProtectedOutput> TProtectedOutput doInvoke(SidecarStackConfiguration cfg
            , EncryptedMessage<TProtectedIn> m
            , Class<TProtectedOutput> protectedResponseCls) throws IOException {

        final String payload = getProcessorServices().stringify(m.getPayload());
        String retVal = doHttpPostWith(cfg, payload, m.getContext());
        return getProcessorServices().readJson(retVal, protectedResponseCls);
    }

    @Override
    protected <TType> TType doInvoke(SidecarStackConfiguration cfg
            , SidecarInput si
            , Class<TType> respCls) throws IOException {

        String retVal = doHttpPostWith(cfg, getProcessorServices().stringify(si), null);
        return getProcessorServices().readJson(retVal, respCls);
    }


    private String doHttpPostWith(SidecarStackConfiguration cfg, String payload, Map<String, String> customHeaders) throws IOException {
        HTTPStackConfiguration htCfg = (HTTPStackConfiguration) cfg;

        HttpPost httpPost = new HttpPost(htCfg.getUri());
        htCfg.forEachHeader(httpPost::addHeader);
//        httpPost.addHeader("content-encoding", "gzip");
        if (customHeaders != null) {
            customHeaders.forEach((key, value) -> {
                httpPost.addHeader(String.format("X-SidecarContext-%s", key), value);
            });
        }

        StringEntity se = new StringEntity(payload, StandardCharsets.UTF_8);

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

            while ((k = is.read(buf)) > 0) {
                baos.write(buf, 0, k);
            }

            // TODO: Factor in content-type and encoding.
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public SidecarStackConfiguration configureFrom(SidecarConfiguration cfg) {
        return new HTTPStackConfiguration(cfg.getStack().getParams());
    }

    static class HTTPStackConfiguration implements SidecarStackConfiguration {
        private String uri;
        private boolean compress = true;
        private Map<String, String> headers;

        HTTPStackConfiguration(Map<String, String> params) {
            headers = new HashMap<>();
            params.forEach((key, value) -> {
                if (CFG_URI.equalsIgnoreCase(key)) {
                    this.uri = value;
                } else if (CFG_COMPRESS.equalsIgnoreCase(key)) {
                    this.compress = Boolean.parseBoolean(value);
                } else {
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
