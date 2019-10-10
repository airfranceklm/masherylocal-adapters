package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.config.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.config.SidecarSynchronicity;
import com.airfranceklm.amt.sidecar.filters.SidecarScopeFilterGroup;
import com.airfranceklm.amt.sidecar.filters.SidecarScopeFilteringResult;
import com.airfranceklm.amt.sidecar.impl.model.SidecarPostProcessorOutputImpl;
import com.airfranceklm.amt.sidecar.impl.model.SidecarPreProcessorOutputImpl;
import com.airfranceklm.amt.sidecar.input.EventInspector;
import com.airfranceklm.amt.sidecar.input.SidecarInputBuilder;
import com.airfranceklm.amt.sidecar.model.*;
import com.airfranceklm.amt.sidecar.stack.AFKLMSidecarStack;
import com.airfranceklm.amt.sidecar.stack.AFKLMSidecarStacks;
import com.airfranceklm.amt.sidecar.stack.InMemoryStack;
import com.airfranceklm.amt.sidecar.stack.ProcessorServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashery.http.HTTPHeaders;
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
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * AFKLM sidecar pre- and post-processor. See the <a href="./readme.md">readme file</a> file for the complete
 * description of the operation.
 */
public class AFKLMSidecarProcessor implements TrafficEventListener, ProcessorServices {

    private static final SidecarPreProcessorOutput doNothingAtPreprocessor = new SidecarPreProcessorOutputImpl();
    private static final SidecarPostProcessorOutput doNothingAtPostProcessor = new SidecarPostProcessorOutputImpl();

    private static final TimeUnit IDEMPOTENT_STORAGE_UNIT = TimeUnit.MINUTES;
    private static final int IDEMPOTENT_STORAGE_ADVANCE = 5;
    private static final String RELAY_MESSAGE = "X_RELAY_MSG";
    private static final String RELAY_PARAMS = "X_RELAY_PARAMS";

    private static DateTimeFormatter jsonFormat = ISODateTimeFormat.dateTimeParser();

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

    private static Pattern mimePattern = Pattern.compile("([a-zA-Z/\\-+.]+)(\\s*;\\s*charset\\s*=\\s*([a-zA-Z0-9\\-]{1,}))?");


    public AFKLMSidecarProcessor() {
        this(new ProductionConfigurationStore());
        initIdempotentDependencies();

        log.info("AFKLM Sidecar Processor has been loaded.");
    }

    void initIdempotentDependencies() {
        idempotentUpdateDebounce = new IdempotentUpdateDebouncer(300, TimeUnit.SECONDS, 10);
        idempotentShallowCache = new IdempotentShallowCache(5 * 1024);
    }

    void watchLocalConfiguration(File root) {
        try {
            scanner = new LocalConfigDirectoryScanner(this, root);
            scanner.scanOnStartup();

            scanner.watch();
        } catch (Throwable ex) {
            log.error(String.format("Could not initialize local directory watch: %s", ex.getMessage()), ex);
        }
    }

    public AFKLMSidecarProcessor(SidecarConfigurationStore store) {
        useStore(store);
    }

    void useStore(SidecarConfigurationStore store) {
        store.bindTo(this);
        this.configStore = store;
    }

    /**
     * Parses the JSON date
     *
     * @param date date to parse
     * @return instance of the parsed date.
     */
    public static Date parseJSONDate(String date) {
        return jsonFormat.parseDateTime(date).toDate();
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
                if (cfg.demandsPreflightHandling()) {
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
            SidecarPreProcessorOutput sOut = invokeIdempotentAware(cmd);
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
        SidecarPreProcessorOutput sidecarOutput = null;

        switch (cmd.getInput().getSynchronicity()) {
            case Event:
            case RequestResponse:
                sidecarOutput = invokePreProcessorRelayAware(cmd);
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

    private SidecarPreProcessorOutput invokePreProcessorRelayAware(SidecarInvocationData cmd) throws IOException {
        SidecarOutput retVal = invokeIdempotentAware(cmd);

        if (retVal != null) {
            if (retVal instanceof SidecarPreProcessorOutput) {
                SidecarPreProcessorOutput preOutput = (SidecarPreProcessorOutput) retVal;

                if (preOutput.relaysMessageToPostprocessor()) {
                    if (preOutput.getRelayParams() != null) {
                        cmd.logEntry(RELAY_PARAMS, preOutput.createSerializableRelayParameters());
                    }
                }

                return preOutput;
            } else {
                throw new IllegalStateException(String.format("Wrong class returned: %s", retVal.getClass().getName()));
            }
        } else {
            return null;
        }
    }

    private SidecarPreProcessorOutput invokeIdempotentAware(SidecarInvocationData cmd) throws IOException {
        SidecarPreProcessorOutput retVal = null;
        if (cmd.isIdempotentAware()) {
            try {
                final String cacheKey = cmd.getCacheKey();

                // We will try reading the idempotent data from the shallow cache. If it is absent,
                // the idempotent response will be placed in a shallow cache to speed things up even further.
                SidecarOutputCache soc = idempotentShallowCache.get(cacheKey);
                if (soc == null) {
                    soc = (SidecarOutputCache) cmd.getCache().get(cmd.getStack().getClass().getClassLoader(), cacheKey);
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

        retVal = invokePreProcessorWithCircuitBreaker(cmd);
        if (cmd.isIdempotentAware() && retVal != null && retVal.getUnchangedUntil() != null) {
            SidecarOutputCache soc = new SidecarOutputCache(retVal.toSerializableForm(),
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

    private SidecarPreProcessorOutput invokePreProcessorWithCircuitBreaker(SidecarInvocationData cmd) throws
            IOException {
        // TODO: Check if the circuit breaker is open.

        if (cmd.getStack() != null) {
            return cmd.getStack().invokeAtPreProcessor(cmd.getStackConfiguration(), cmd, this);
        } else {
            throw new IOException("Null stack is not allowed");
        }
    }

    private SidecarPostProcessorOutput invokePostProcessorWithCircuitBreaker(SidecarInvocationData cmd) throws
            IOException {
        // TODO: Check if the circuit breaker is open.

        if (cmd.getStack() != null) {
            return cmd.getStack().invokeAtPostProcessor(cmd.getStackConfiguration(), cmd, this);
        } else {
            throw new IOException("Null stack is not allowed");
        }
    }

    /**
     * Applies the sidecar output on the pre-processor event
     *
     * @param ppe    a pre-processor event.
     * @param output an out
     * @return true if handling can continue, false otherwise.
     * @throws IOException if data could not be applied.
     */
    private boolean applySidecarOutput(PreProcessEvent ppe, SidecarPreProcessorOutput output) throws IOException {
        // Null object means do-nothing.
        if (output == null) {
            return true;
        }

        if (output.getTerminate() != null) {
            sendCurtailed(ppe.getCallContext().getResponse(), output.getTerminate());
            return false;
        } else if (output.getModify() != null) {
            RequestModificationCommand cmd = output.getModify();

            // Apply any modifications that were returned form the function.
            if (cmd.getDropHeaders() != null) {
                cmd.getDropHeaders().forEach(h -> {
                    ppe.getClientRequest().getHeaders().remove(h);
                });
            }

            if (cmd.getAddHeaders() != null) {
                cmd.getAddHeaders().forEach((key, value) -> {
                    ppe.getClientRequest().getHeaders().set(key, value);
                });
            }

            if (cmd.getChangeRoute() != null) {
                RequestRoutingChangeBean r = cmd.getChangeRoute();

                if (r.getUri() != null) {
                    ppe.getClientRequest().setURI(r.getUri());
                } else if (r.outboundURINeedsChanging()) {
                    try {
                        URL current = new URL(ppe.getClientRequest().getURI());
                        String host = r.getHost() != null ? r.getHost() : current.getHost();
                        String file = r.getFile() != null ? r.getFile() : current.getFile();
                        int port = r.getPort() != null ? r.getPort() : current.getPort();

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

            if (cmd.getPayload() != null) {
                ppe.getClientRequest().setBody(new ContentProducerImpl(cmd.getPayload()));
            } else if (cmd.getJSONPayload() != null) {
                if (!cmd.addsContentType()) {
                    ppe.getClientRequest().getHeaders().set("content-type", "application/json");
                }
                ppe.getClientRequest().getHeaders().set("content-encoding", "gzip");
                ppe.getClientRequest().setBody(new ContentProducerImpl(JsonHelper.toJSON(cmd.getJSONPayload(), true)));
            }
        }

        return true;
    }

    private void applySidecarOutput(PostProcessEvent ppe, SidecarPostProcessorOutput output) throws IOException {
        if (output.getTerminate() != null) {
            sendCurtailed(ppe.getCallContext().getResponse(), output.getTerminate());
        } else if (output.getModify() != null) {
            ResponseModificationCommand cmd = output.getModify();

            // Apply any modifications that were returned form the function.
            if (cmd.getDropHeaders() != null) {
                cmd.getDropHeaders().forEach(h -> ppe.getServerResponse().getHeaders().remove(h));
            }

            if (cmd.getAddHeaders() != null) {
                final MutableHTTPHeaders headers = ppe.getServerResponse().getHeaders();
                cmd.getAddHeaders().forEach(headers::set);
            }

            if (cmd.getPayload() != null) {
                ppe.getServerResponse().setBody(new ContentProducerImpl(cmd.getPayload()));
            }

            if (cmd.getJSONPayload() != null) {
                ppe.getServerResponse().setBody(new ContentProducerImpl(JsonHelper.toJSON(cmd.getJSONPayload())));
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
        SidecarPostProcessorOutput sidecarOutput = null;

        switch (cmd.getInput().getSynchronicity()) {
            case Event:
            case RequestResponse:
                sidecarOutput = invokePostProcessorWithCircuitBreaker(cmd);
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

        return compress ? Base64.getEncoder().encodeToString(baos.toByteArray()) : baos.toString();
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
     * @param cmd  termination command.
     */
    private void sendCurtailed(TrafficManagerResponse response, SidecarOutputTerminationCommand cmd) {
        if (cmd.getPayload() != null || cmd.getJSONPayload() != null) {

            response.getHTTPResponse().setStatusCode(cmd.getCode());

            final MutableHTTPHeaders headers = response.getHTTPResponse().getHeaders();
            String content = null;

            if (cmd.getHeaders() != null) {
                cmd.getHeaders().forEach(headers::set);
            }

            if (cmd.getPayload() != null) {
                content = cmd.getPayload();
            } else if (cmd.getJSONPayload() != null) {
                if (!cmd.specifiesContentType()) {
                    headers.set("content-type", "application/json");
                }

                content = JsonHelper.toJSON(cmd.getJSONPayload(), true);
                headers.set("content-encoding", "gzip");
            }

            if (content != null) {
                response.getHTTPResponse().setBody(new ContentProducerImpl(content));
            }
            response.setComplete();
        } else {
            sendCurtailed(response, cmd.getCode(), cmd.getMessage());
        }
    }

    static boolean isTextMimeType(String pType) {
        if (pType == null) return false;

        String type = pType.toLowerCase();

        if (type.startsWith("text/")) {
            return true;
        } else if (type.startsWith("application/json")
                || type.startsWith("application/javascript")
                || type.equals("application/ld+json")
                || type.equals("application/vnd.api+json")
        ) {
            return true;
        } else if (type.startsWith("application/yaml") || type.startsWith("application/x-yaml")) {
            return true;
        } else if (type.equals("application/x-www-form-urlencoded")) {
            return true;
        } else if (type.startsWith("application/xml") || type.startsWith("application/xhtml")) {
            return true;
        } else if (type.startsWith("application/graphql")) {
            return true;
        }

        return false;
    }

    static String inferTextEncoding(String mimeValue) {
        Matcher m = mimePattern.matcher(mimeValue);
        if (m.matches()) {
            String type = m.group(1);
            if (isTextMimeType(type)) {
                String specifiedEnc = m.group(3);
                if (specifiedEnc == null) {
                    return "utf-8";
                } else {
                    return specifiedEnc;
                }
            }
        }
        return null;
    }

    public static void addContentBody(HTTPHeaders headers, ContentSource bodyContent, SidecarInputHTTPMessage to) throws IOException {
        if (bodyContent != null) {
            boolean sendAsBase64 = true;
            Charset strCharset = StandardCharsets.UTF_8;

            final boolean hasContentEncoding = headers.contains("content-encoding")
                    || headers.contains("content-transfer-encoding")
                    || headers.contains("transfer-encoding");

            if (!hasContentEncoding && headers.contains("content-type")) {
                final String contentType = headers.get("content-type");
                if (isTextMimeType(contentType)) {
                    String textEnc = inferTextEncoding(contentType);
                    if (textEnc != null) {
                        String normalizedEnc = textEnc.toLowerCase();
                        switch (normalizedEnc) {
                            case "utf-8":
                            case "utf8":
                                sendAsBase64 = false;
                            default:
                                try {
                                    strCharset = Charset.forName(textEnc);
                                    sendAsBase64 = false;
                                } catch (UnsupportedCharsetException ex) {
                                    // Charset is not known, the content will be sent as Base64
                                }
                                break;
                        }
                    }
                }
            }

            to.setPayloadLength(bodyContent.getContentLength());
            if (sendAsBase64) {
                to.setPayloadBase64Encoded(true);
                to.setPayload(getBase64ContentOf(bodyContent));
            } else {
                to.setPayloadBase64Encoded(false);
                to.setPayload(getContentOf(bodyContent, strCharset));
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

    /**
     * Gets the content of the stream using the specified charset
     *
     * @param cs      content source
     * @param charSet charset to use
     * @return read values of the content source
     * @throws IOException if an I/O error is thrown.
     */
    public static String getContentOf(ContentSource cs, Charset charSet) throws IOException {
        StringBuilder sb = new StringBuilder();
        Reader r = new InputStreamReader(cs.getInputStream(), charSet);
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
     * Returns the content of the stream as a Base-64 encoded array
     *
     * @param cs a non-null content source
     * @return Base64 representation of this array.
     * @throws IOException if an i/o error reading the data will occur.
     */
    public static String getBase64ContentOf(ContentSource cs) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        InputStream is = cs.getInputStream();
        byte[] buf = new byte[10240];

        int k = 0;
        while ((k = is.read(buf)) > 0) {
            baos.write(buf, 0, k);
        }

        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * Resolves the stack referred to in this configuration.
     *
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

    //------------------------------------------------------------------
    // Processor services


    @Override
    public SidecarPreProcessorOutput asPreProcessor(String rawJSON) throws IOException {
        return JsonHelper.toSidecarPreProcessorOutput(rawJSON);
    }

    @Override
    public SidecarPostProcessorOutput asPostProcessor(String rawJSON) throws IOException {
        return JsonHelper.toSidecarPostProcessorOutput(rawJSON);
    }

    @Override
    public SidecarPreProcessorOutput doNothingForPreProcessing() {
        return doNothingAtPreprocessor;
    }

    @Override
    public SidecarPostProcessorOutput doNothingForPostProcessing() {
        return doNothingAtPostProcessor;
    }
}
