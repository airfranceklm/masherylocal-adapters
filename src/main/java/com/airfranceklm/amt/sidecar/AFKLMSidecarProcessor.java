package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.config.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.config.SidecarSynchronicity;
import com.airfranceklm.amt.sidecar.filters.SidecarScopeFilterGroup;
import com.airfranceklm.amt.sidecar.filters.SidecarScopeFilteringResult;
import com.airfranceklm.amt.sidecar.input.EventInspector;
import com.airfranceklm.amt.sidecar.input.SidecarInputBuilder;
import com.airfranceklm.amt.sidecar.input.SidecarRuntimeCompiler;
import com.airfranceklm.amt.sidecar.stack.AFKLMSidecarStack;
import com.airfranceklm.amt.sidecar.stack.AFKLMSidecarStacks;
import com.airfranceklm.amt.sidecar.stack.InMemoryStack;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashery.http.MutableHTTPHeaders;
import com.mashery.http.io.ContentProducer;
import com.mashery.http.io.ContentSource;
import com.mashery.http.server.HTTPServerResponse;
import com.mashery.trafficmanager.cache.Cache;
import com.mashery.trafficmanager.cache.CacheException;
import com.mashery.trafficmanager.event.listener.TrafficEventListener;
import com.mashery.trafficmanager.event.model.TrafficEvent;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.model.core.TrafficManagerResponse;
import com.mashery.trafficmanager.processor.ProcessorBean;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

/**
 * AFKLM sidecar pre- and post-processor. See the <a href="./readme.md">readme file</a> file for the complete
 * description of the operation.
 */
@ProcessorBean(enabled = true,
        immediate = true,
        name = "com.airfranceklm.amt.lambda.AFKLMLambdaSidecarProcessor")
public class AFKLMSidecarProcessor implements TrafficEventListener {

    private static final TimeUnit IDEMPOTENT_STORAGE_UNIT = TimeUnit.MINUTES;
    private static final int IDEMPOTENT_STORAGE_ADVANCE = 5;
    private static final String RELAY_MESSAGE = "X_RELAY_MSG";
    private static final String RELAY_PARAMS = "X_RELAY_PARAMS";

    public static final DateFormat jsonDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(AFKLMSidecarProcessor.class);

    /**
     * Maximum number of threads we can have in the background. The initial value of 50 is set based on an educated
     * guess, not a measurements.
     */
    private static final int MAX_BACKGROUND_THREADS = 50;

    /**
     * Maximum number of objects that could be offloaded to the background execution. Attempting to send more
     * will result in skipping.
     */
    private static final int MAX_NONBLOCKING_QUEUE = 2000;

    private SidecarConfigurationStore configStore;

    /**
     * Invokers where {@link SidecarSynchronicity#NonBlockingEvent} synchronicity is offloaded.
     */
    static private ThreadPoolExecutor backgroundInvokers;
    static private AFKLMSidecarStacks sidecarStacks;

    static {
        backgroundInvokers = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_BACKGROUND_THREADS);
        sidecarStacks = new AFKLMSidecarStacks();
    }

    public static final String PRE_CONDITION_FAILURE_MSG = "Request pre-condition not met, code 0x000003BB";
    public static final String MSG_SERVICE_CANNOT_BE_PROVIDED = "Service cannot be provided, code 0x000003BB";

    public static final String MSG_PREPROCESSING_ERROR = "Internal server error before processing the call, code 0x000003BB";
    public static final String MSG_POSTPROCESSING_ERROR = "Internal server error before sending the response, code 0x000003BB";
    public static final String MSG_PREFLIGHT_ERROR = "Internal server error before processing the call, code 0x000003BB, reason 0x000003B1";
    public static final String MSG_GLOB_ERROR = "Internal server error before processing the call, code 0x000003BB, reason 0x000003B3";

    private IdempotentUpdateDebouncer idempotentUpdateDebounce = new IdempotentUpdateDebouncer(300, TimeUnit.SECONDS, 10);
    private IdempotentShallowCache idempotentShallowCache = new IdempotentShallowCache(5 * 1024);

    private LocalConfigDirectoryScanner scanner;


    public AFKLMSidecarProcessor() {
        this(new ProductionConfigurationStore());
        initIdempotentDependencies();

        scanner = new LocalConfigDirectoryScanner(this, new File("/etc/mashery/sidecar"));
        scanner.scanOnStartup();

        // TODO:
        // - Start the file system watcher to monitor the file system changes.
    }

    public void initIdempotentDependencies() {
        idempotentUpdateDebounce = new IdempotentUpdateDebouncer(300, TimeUnit.SECONDS, 10);
        idempotentShallowCache = new IdempotentShallowCache(5 * 1024);
    }

    public AFKLMSidecarProcessor(SidecarConfigurationStore store) {
        useStore(store);
    }

    void useStore(SidecarConfigurationStore store) {
        store.bindTo(this);
        this.configStore = store;
    }


    public static SidecarOutput toSidecarOutput(InputStream is, Charset c) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[10240];

        int k = 0;
        while ((k = is.read(buf)) > 0) {
            baos.write(buf, 0, k);
        }
        return toSidecarOutput(new String(baos.toByteArray(), c));
    }

    /**
     * Convert a raw string value into the Lambda sidecar output
     *
     * @param lambdaRawValue lambda sidecar output value
     * @return marshalled object
     * @throws IOException if the marshalling cannot be successfully completed.
     */
    public static SidecarOutput toSidecarOutput(String lambdaRawValue) throws IOException {
        try {
            return objectMapper.readValue(lambdaRawValue,
                    SidecarOutputImpl.class);
        } catch (JsonMappingException ex) {
            log.error(String.format("Function returned JSON that cannot be converted to the output: %s", lambdaRawValue));
            throw new IOException("Cannot unmarshal Lambda return");
        }
    }

    @Override
    public void handleEvent(TrafficEvent trafficEvent) {
        if (trafficEvent instanceof PreProcessEvent) {
            PreProcessEvent ppe = (PreProcessEvent) trafficEvent;

            SidecarConfiguration cfg = configStore.getConfiguration(ppe);

            // Apply static modification. This is needed mainly to override SaaS-configured API call routing with the
            // routing applicable for the Mashery machine is actually residing. In a typical scenario, this sidecar
            // synthetic output will also add a header indicating the origin or a machine identity.

            if (cfg.getStaticModification() != null) {
                try {
                    if (!applySidecarOutput(ppe, cfg.getStaticModification())) {
                        return;
                    }
                } catch (IOException ex) {
                    log.error(String.format("Failed to apply static configuration on endpoint %s: %s", ppe.getEndpoint(), ex.getMessage()));
                    sendError(ppe.getCallContext().getResponse(), SidecarInputPoint.StaticModification);
                }
            }

            try {
                if (cfg.isPreflightEnabled()) {
                    // The main purpose of the pre-flight check is to disable the traffic to the API when a specific
                    // condition is either met or violated. Secondary purpose is to add additional headers that "enrich" the identification
                    // of the service, endpoint, or a package.
                    if (!handlePreFlight(ppe, cfg)) {
                        return;
                    }
                }

                handlePreProcessor(ppe, cfg);
            } catch (IOException ex) {
                if (cfg.isFailsafe()) {
                    log.warn(String.format("Sidecar processing failed on endpoint %s.", ppe.getEndpoint().getExternalID()));
                } else {
                    sendError(ppe.getCallContext().getResponse(), SidecarInputPoint.PreProcessor);
                }
            }
        } else if (trafficEvent instanceof PostProcessEvent) {
            PostProcessEvent ppe = (PostProcessEvent) trafficEvent;
            SidecarConfiguration cfg = configStore.getConfiguration(ppe);

            try {
                handlePostProcessor(ppe, cfg);
            } catch (IOException ex) {
                if (cfg.isFailsafe()) {
                    // A fail-safe using invalid configuration. This needs to be reported.
                    // This code shows the minimum required. In production-grade implementation, the
                    // code would also count how many times an error has been reported. The objective is to
                    // stop reporting the same error after so-many erroneous calls.
                    log.warn(String.format("Sidecar configuration is not valid for endpoint %s.", ppe.getEndpoint().getExternalID()));
                } else {
                    sendError(ppe.getCallContext().getResponse(), SidecarInputPoint.PostProcessor);
                }
            }
        }
    }

    private void invokeInBackground(SidecarInvocationData cmd) {
        if (backgroundInvokers.getQueue().size() < MAX_NONBLOCKING_QUEUE) {
            backgroundInvokers.submit(new NonBlockingInvocationTask(cmd));
        }
        // else: the queue is too big to accept any further messages.
    }

    private boolean handlePreFlight(PreProcessEvent ppe, SidecarConfiguration cfg) throws IOException {
        SidecarInputBuilder<PreProcessEvent> preflightBuilder = configStore.getPreflightInputBuilder(cfg);
        SidecarInvocationData cmd = preflightBuilder.build(ppe);
        // TODO: The cmd MUST be idempotent-aware, otherwise the pre-flight check will not be
        // correctly cached.

        try {
            SidecarOutput sOut = invokeIdempotentAware(cmd);
            return applySidecarOutput(ppe, sOut);
        } catch (IOException ex) {
            if (cfg.isFailsafe()) {
                // TODO: better logging
                log.warn(String.format("Pre-flight check failed for endpoint %s.", ppe.getEndpoint().getExternalID()));
                return true;
            } else {
                sendError(ppe.getCallContext().getResponse(), SidecarInputPoint.Preflight);
                return false;
            }
        }
    }

    private void handlePreProcessor(PreProcessEvent ppe, SidecarConfiguration cfg) throws IOException {

        SidecarInputBuilder<PreProcessEvent> builder = configStore.getPreProcessorInputBuilder(cfg);

        SidecarScopeFilteringResult filterResult = null;
        // Filter the conditions to see if the function actually applies to this request.
        if (builder.requiresScopeFiltering()) {
            filterResult = new SidecarScopeFilteringResult();

            for (SidecarScopeFilterGroup<?> filter : builder.getScopeFilters()) {
                if (!filter.match(ppe, filterResult)) {
                    return;
                }
            }
        }

        // Inspect the pre-conditions.
        if (builder.requiresPreconditionInspection()) {
            for (EventInspector<PreProcessEvent> ppi : builder.getConditionAssertions()) {
                // The inspection can yield:
                // pre-condition is not met;
                // operation is not required;
                // pre-processing should continue
                switch (ppi.accept(ppe)) {
                    case Fail:
                        sendCurtailed(ppe.getCallContext().getResponse(), 500, PRE_CONDITION_FAILURE_MSG);
                        return;
                    case Reject:
                        sendCurtailed(ppe.getCallContext().getResponse(), 400, PRE_CONDITION_FAILURE_MSG);
                        return;
                    case Noop:
                        return;
                    case Pass:
                        break;
                }
            }
        }

        SidecarInvocationData cmd = builder.build(ppe, filterResult);
        SidecarOutput sidecarOutput = null;

        switch (cmd.getInput().getSynchronicity()) {
            case Event:
            case RequestResponse:
                sidecarOutput = invokeRelayAware(cmd);
                break;
            case NonBlockingEvent:
                invokeInBackground(cmd);
                break;

            default:
                // The code below is unreachable. Should the conditions above fail, then
                // we will send an internal error, because, well, it is an internal error.
                sendError(ppe.getCallContext().getResponse(), SidecarInputPoint.PreProcessor);
                break;
        }

        if (sidecarOutput != null) {
            applySidecarOutput(ppe, sidecarOutput);
        }


    }

    private SidecarOutput invokeRelayAware(SidecarInvocationData cmd) throws IOException {
        SidecarOutput retVal = invokeIdempotentAware(cmd);

        if (retVal != null && retVal.relaysMessageToPostprocessor()) {
            if (retVal.getRelayParameters() != null) {
                cmd.logEntry(RELAY_PARAMS, retVal.createSerializeableRelayParameters());
            }
        }

        return retVal;
    }

    private SidecarOutput invokeIdempotentAware(SidecarInvocationData cmd) throws IOException {
        SidecarOutput retVal = null;
        if (cmd.isIdempotentAware()) {
            try {
                final String cacheKey = cmd.getCacheKey();

                // We will try reading the idempotent data from the shallow cache. If it is absent,
                // the idempotent response will be placed in a shallow cache to speed things up even further.
                SidecarOutputCache soc = idempotentShallowCache.get(cacheKey);
                if (soc == null) {
                    soc = (SidecarOutputCache) cmd.getCache().get(getClass().getClassLoader(), cacheKey);
                    idempotentShallowCache.put(cacheKey, soc);
                }

                if (soc != null) {
                    retVal = soc.getValue();

                    // Refresh the storage of the idempotent object if it's still used.
                    if (soc.needsStorageRefresh() && !idempotentUpdateDebounce.debounce(cacheKey)) {
                        soc.extendStoreWith(IDEMPOTENT_STORAGE_UNIT, IDEMPOTENT_STORAGE_ADVANCE);
                        cacheSidecarOutput(cmd.getCache(), cacheKey, soc);
                    }

                    return retVal;
                }
            } catch (CacheException ex) {
                log.warn(String.format("Error is storing: %s", ex.getMessage()));
            }
        }

        retVal = invokeWithCircuitBreaker(cmd);
        if (cmd.isIdempotentAware() && retVal != null && retVal.getUnchangedUntil() != null) {
            SidecarOutputCache soc = new SidecarOutputCache(retVal,
                    IDEMPOTENT_STORAGE_UNIT,
                    IDEMPOTENT_STORAGE_ADVANCE);

            cacheSidecarOutput(cmd.getCache(), cmd.getCacheKey(), soc);
            // The cached value is NOT loaded in memory. There is no guarantee that an idempotent data will
            // be used. Loading fresh data in memory can displace objects in the shallow cache that are
            // actually used. Therefore the shallow cache will be filled only when it will be actually
            // used.
        }
        return retVal;
    }

    private void cacheSidecarOutput(Cache mashCache, String cacheKey, SidecarOutputCache soc) {
        try {
            mashCache.put(cacheKey, soc, soc.getStorageDuration());
        } catch (CacheException e) {
            log.warn(String.format("Error is storing: %s", e.getMessage()));
        }
    }

    private SidecarOutput invokeWithCircuitBreaker(SidecarInvocationData cmd) throws IOException {
        // TODO: build the support for the circuit breaker.
        return invokeStack(cmd);
    }

    /**
     * Performs the actual invocation of the network sidecar stack.
     *
     * @param cmd sidecar invocation data
     * @return instance of {@link SidecarOutput} that was returned from sidecar.
     * @throws IOException if the invocation cannot be invoked.
     */
    private SidecarOutput invokeStack(SidecarInvocationData cmd) throws IOException {
        return cmd.getStack().invoke(cmd.getStackConfiguration(), cmd.getInput());
    }

    /**
     * Applies the sidecar output on the pre-processor event
     *
     * @param ppe    a pre-processor event.
     * @param output an out
     * @return true if handling can continue, false otherwise.
     * @throws IOException if data could not be applied.
     */
    private boolean applySidecarOutput(PreProcessEvent ppe, SidecarOutput output) throws IOException {
        // Null object means do-nothing.
        if (output == null) {
            return true;
        }

        if (output.getCode() != null) {
            sendCurtailed(ppe.getCallContext().getResponse(), output);
            return false;
        } else {
            // Apply any modifications that were returned form the function.
            if (output.getDropHeaders() != null) {
                output.getDropHeaders().forEach(h -> {
                    ppe.getClientRequest().getHeaders().remove(h);
                });
            }

            if (output.getAddHeaders() != null) {
                output.getAddHeaders().forEach((key, value) -> {
                    ppe.getClientRequest().getHeaders().set(key, value);
                });
            }

            if (output.getChangeRoute() != null) {
                SidecarOutputRouting r = output.getChangeRoute();

                if (r.getUri() != null) {
                    ppe.getClientRequest().setURI(r.getUri());
                } else if (r.overridesPartially()) {
                    try {
                        URL current = new URL(ppe.getClientRequest().getURI());
                        String host = r.getHost() != null ? r.getHost() : current.getHost();
                        String file = r.getFile() != null ? r.getFile() : current.getFile();
                        int port = r.getPort() > 0 ? r.getPort() : current.getPort();

                        URL newURL = new URL(current.getProtocol(), host, port, file);
                        ppe.getClientRequest().setURI(newURL.toString());
                    } catch (MalformedURLException ex) {
                        throw new IOException(String.format("Could not apply target route: %s", ex.getMessage()));
                    }
                }
                if (r.getHttpVerb() != null) {
                    ppe.getClientRequest().setMethod(r.getHttpVerb());
                }
            }

            if (output.getPayload() != null) {
                ppe.getClientRequest().setBody(new ContentProducerImpl(output.getPayload()));
            } else if (output.getJson() != null) {
                if (!output.addsContentType()) {
                    ppe.getClientRequest().getHeaders().set("content-type", "application/json");
                }
                ppe.getClientRequest().getHeaders().set("content-encoding", "gzip");
                ppe.getClientRequest().setBody(new ContentProducerImpl(toJSON(output.getJson(), true)));
            }

            return true;
        }
    }

    private void applySidecarOutput(PostProcessEvent ppe, SidecarOutput output) throws IOException {
        if (output.getCode() != null) {
            sendCurtailed(ppe.getCallContext().getResponse(), output);
        } else {
            // Apply any modifications that were returned form the function.
            if (output.getDropHeaders() != null) {
                output.getDropHeaders().forEach(h -> ppe.getServerResponse().getHeaders().remove(h));
            }

            if (output.getAddHeaders() != null) {
                final MutableHTTPHeaders headers = ppe.getServerResponse().getHeaders();
                output.getAddHeaders().forEach(headers::set);
            }

            if (output.getPayload() != null) {
                ppe.getServerResponse().setBody(new ContentProducerImpl(output.getPayload()));
            }

            if (output.getJson() != null) {
                ppe.getServerResponse().setBody(new ContentProducerImpl(toJSON(output.getJson())));
            }
        }
    }

    private void handlePostProcessor(PostProcessEvent ppe, SidecarConfiguration cfg) throws IOException {
        SidecarInputBuilder<PostProcessEvent> builder = configStore.getPostProcessorInputBuilder(cfg);

        // TODO: This is duplication code: ->
        SidecarScopeFilteringResult filterResult = null;
        // Filter the conditions to see if the function actually applies to this request.
        if (builder.requiresScopeFiltering()) {
            filterResult = new SidecarScopeFilteringResult();

            for (SidecarScopeFilterGroup<?> filter : builder.getScopeFilters()) {
                if (!filter.match(ppe, filterResult)) {
                    return;
                }
            }
        }

        // Inspect the pre-conditions.
        if (builder.requiresPreconditionInspection()) {
            for (EventInspector<PostProcessEvent> ppi : builder.getConditionAssertions()) {
                // The inspection can yield:
                // pre-condition is not met;
                // operation is not required;
                // pre-processing should continue
                switch (ppi.accept(ppe)) {
                    case Fail:
                        sendCurtailed(ppe.getCallContext().getResponse(), 500, PRE_CONDITION_FAILURE_MSG);
                        return;
                    case Reject:
                        sendCurtailed(ppe.getCallContext().getResponse(), 400, PRE_CONDITION_FAILURE_MSG);
                        return;
                    case Noop:
                        return;
                    case Pass:
                        break;
                }
            }
        }

        // <- TODO end of duplicat ecode.

        SidecarInvocationData cmd = builder.build(ppe, filterResult);
        // TODO: Fetch the relayed data, if any.
        SidecarOutput sidecarOutput = null;

        switch (cmd.getInput().getSynchronicity()) {
            case Event:
            case RequestResponse:
                sidecarOutput = invokeIdempotentAware(cmd);
                break;
            case NonBlockingEvent:
                invokeInBackground(cmd);
                break;
            default:
                // The code below is unreachable. Should the conditions above fail, then
                // we will send an internal error, because, well, it is an internal error.
                sendError(ppe.getCallContext().getResponse(), SidecarInputPoint.PostProcessor);
                break;
        }

        if (sidecarOutput != null) {
            applySidecarOutput(ppe, sidecarOutput);
        }
    }


    public static String toJSON(Object obj) throws IOException {
        return toJSON(obj, false);
    }

    private static String toJSON(Object obj, boolean compress) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream sink = baos;
        if (compress) {
            sink = new GZIPOutputStream(baos);
        }
        objectMapper.writeValue(sink, obj);
        sink.close();

        return compress ? Base64.encode(baos.toByteArray()) : baos.toString();
    }

    /**
     * Sends an error to the tranffic manager response.
     *
     * @param response response object to write an opaque response message.
     */
    private void sendError(TrafficManagerResponse response, SidecarInputPoint point) {
        response.getHTTPResponse().setStatusCode(500);
        response.getHTTPResponse().getHeaders().set("content-type", "application/xml");

        String msg = "";
        switch (point) {
            case PreProcessor:
                msg = MSG_PREPROCESSING_ERROR;
                break;
            case PostProcessor:
                msg = MSG_POSTPROCESSING_ERROR;
                break;
            case Preflight:
                msg = MSG_PREFLIGHT_ERROR;
                break;
            case StaticModification:
                msg = MSG_GLOB_ERROR;
                break;
        }

        response.getHTTPResponse().setBody(new ContentProducerImpl(formatCurtailedMessage(msg)));
        response.setComplete();
    }

    /**
     * @param response response
     * @param code     code
     * @param message  message to format. If not specified, then {@link #MSG_SERVICE_CANNOT_BE_PROVIDED}
     *                 string will be used.
     */
    private void sendCurtailed(TrafficManagerResponse response, int code, String message) {
        final HTTPServerResponse http = response.getHTTPResponse();

        http.setStatusCode(code);

        http.getHeaders().set("content-type", "application/xml");

        String uMessage = message != null ? message : MSG_SERVICE_CANNOT_BE_PROVIDED;
        http.setBody(new ContentProducerImpl(formatCurtailedMessage(uMessage)));

        response.setComplete();
    }

    public static String formatCurtailedMessage(String uMessage) {
        return String.format("<h1><![CDATA[%s]]></h1>", uMessage);
    }

    /**
     * Terminates pre-processing of the request and sends the lambda-provided body to the response
     *
     * @param response response object
     * @param output   output of the lambda dunctoin
     */
    private void sendCurtailed(TrafficManagerResponse response, SidecarOutput output) {
        if (output.getPayload() != null || output.getJson() != null) {

            response.getHTTPResponse().setStatusCode(output.getCode());

            final MutableHTTPHeaders headers = response.getHTTPResponse().getHeaders();
            String content = null;

            if (output.getAddHeaders() != null) {
                output.getAddHeaders().forEach(headers::set);
            }

            if (output.getPayload() != null) {
                content = output.getPayload();
            } else if (output.getJson() != null) {
                if (!output.addsContentType()) {
                    headers.set("content-type", "application/json");
                }

                try {
                    content = toJSON(output.getJson(), true);
                    headers.set("content-encoding", "gzip");
                } catch (IOException ex) {
                    content = "{\"message\": \"Internal server error before processing the call, code 0x000003BB/A\"}";
                }
            }

            if (content != null) {
                response.getHTTPResponse().setBody(new ContentProducerImpl(content));
            }
            response.setComplete();
        } else {
            sendCurtailed(response, output.getCode(), output.getMessage());
        }
    }

    public static void addContentBody(ContentSource bodyContent, SidecarInputHTTPMessage to) throws IOException {
        if (bodyContent != null) {
            to.setPayloadLength(bodyContent.getContentLength());
            if (to.getPayloadLength() > 0) {
                to.setPayload(getContentOf(bodyContent));
            }
        }
    }

    private class NonBlockingInvocationTask implements Runnable {
        SidecarInvocationData cmd;

        NonBlockingInvocationTask(SidecarInvocationData cmd) {
            this.cmd = cmd;
        }

        @Override
        public void run() {
            try {
                invokeIdempotentAware(cmd);
            } catch (IOException ex) {
                log.error(String.format("Failed to invoke command for service %s and endpoint %s: %s",
                        cmd.getServiceId(),
                        cmd.getEndpointId(),
                        ex.getMessage()));
            }
        }
    }

    public static ContentProducer produceFromString(String str) {
        return new ContentProducerImpl(str);
    }

    public static String getContentOf(ContentSource cs) throws IOException {
        StringBuilder sb = new StringBuilder();
        Reader r = new InputStreamReader(cs.getInputStream());
        CharBuffer cb = CharBuffer.allocate(10240);

        while (r.read(cb) > 0) {
            if (cb.length() == 0) {
                cb.rewind();
                sb.append(cb);
                cb.clear();
            }
        }

        int uPos = cb.position();
        if (uPos > 0) {
            cb.rewind();
            sb.append(cb, 0, uPos);
        }

        r.close();

        return sb.toString();
    }

    /**
     * Resolves the stack referred to in this configuration.
     * @param cfg configuration
     * @return stack to use, or null if not supported or installed.
     */
    public AFKLMSidecarStack getStackFor(SidecarConfiguration cfg) {
        return sidecarStacks.getStackFor(cfg);
    }

    public InMemoryStack getInMemoryStack() {
        return sidecarStacks.getInMemoryStack();
    }

    public SidecarConfigurationStore getConfigStore() {
        return configStore;
    }
}
