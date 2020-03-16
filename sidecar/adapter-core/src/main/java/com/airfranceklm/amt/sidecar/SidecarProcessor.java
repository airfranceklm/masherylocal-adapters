package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.builders.PostProcessSidecarInputBuilder;
import com.airfranceklm.amt.sidecar.builders.PreFlightSidecarInputBuilder;
import com.airfranceklm.amt.sidecar.builders.PreProcessSidecarInputBuilder;
import com.airfranceklm.amt.sidecar.elements.ElementsFactory;
import com.airfranceklm.amt.sidecar.identity.CounterpartKeySet;
import com.airfranceklm.amt.sidecar.identity.ProcessorKeySet;
import com.airfranceklm.amt.sidecar.model.*;
import com.airfranceklm.amt.sidecar.model.alcp.ALCPAlgorithmFactory;
import com.airfranceklm.amt.sidecar.stack.SidecarStack;
import com.airfranceklm.amt.sidecar.stack.SidecarStacks;
import com.airfranceklm.amtml.payload.PayloadOperations;
import com.airfranceklm.amtml.payload.PipedContentProducer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashery.http.HTTPHeaders;
import com.mashery.http.MutableHTTPHeaders;
import com.mashery.http.client.HTTPClientException;
import com.mashery.http.client.HTTPClientRequest;
import com.mashery.http.client.HTTPClientResponse;
import com.mashery.http.server.HTTPServerRequest;
import com.mashery.http.server.HTTPServerResponse;
import com.mashery.trafficmanager.cache.Cache;
import com.mashery.trafficmanager.cache.CacheException;
import com.mashery.trafficmanager.debug.DebugContext;
import com.mashery.trafficmanager.event.listener.TrafficEventListener;
import com.mashery.trafficmanager.event.model.TrafficEvent;
import com.mashery.trafficmanager.event.model.TrafficEventType;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import com.mashery.trafficmanager.model.auth.AuthorizationContext;
import com.mashery.trafficmanager.model.core.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.airfranceklm.amt.sidecar.JsonHelper.*;
import static com.airfranceklm.amt.sidecar.SidecarInvocationData.RAW_RESPONSE_PAYLOAD_KEY;
import static com.airfranceklm.amt.sidecar.model.SidecarInputPoint.*;
import static com.airfranceklm.amtml.payload.PayloadOperations.*;

/**
 * Air France/KLM sidecar pre- and post-processor. See the <a href="./readme.md">readme file</a> file for the complete
 * description of the operation.
 */
@Slf4j
public class SidecarProcessor implements TrafficEventListener {

    private static final Base64.Decoder base64Decoder = Base64.getDecoder();

    private static final TimeUnit IDEMPOTENT_STORAGE_UNIT = TimeUnit.MINUTES;
    private static final int IDEMPOTENT_STORAGE_ADVANCE = 5;

    public static final String RELAY_PARAMS = "X_AFKLM_SCAR_RELAY_PARAMS";

    /**
     * Maximum number of objects that could be offloaded to the background execution. Attempting to send more
     * will result in skipping.
     */
    private static final int MAX_NONBLOCKING_QUEUE = 2000;

    public static final String PRE_CONDITION_FAILURE_MSG = "Request pre-condition not met, code 0x000003BB";
    public static final String POST_CONDITION_FAILURE_MSG = "Request post-condition not met, code 0x000003BB";

    public static final String MSG_SERVICE_CANNOT_BE_PROVIDED = "Service cannot be provided, code 0x000003BB";

    public static final String MSG_PRECONDITION_ERROR = "Internal server error in service configuration, code 0x000003BB";
    public static final String MSG_PREPROCESSING_ERROR = "Internal server error before processing the call, code 0x000003BB";
    public static final String MSG_POSTPROCESSING_ERROR = "Internal server error before sending the response, code 0x000003BB";
    public static final String MSG_PREFLIGHT_ERROR = "Internal server error before processing the call, code 0x000003BB, reason 0x000003B1";
    public static final String MSG_GLOB_ERROR = "Internal server error before processing the call, code 0x000003BB, reason 0x000003B3";

    private static Pattern mimePattern = Pattern.compile("([a-zA-Z/\\-+.]+)(\\s*;\\s*charset\\s*=\\s*([a-zA-Z0-9\\-]{1,}))?");

    @Getter
    @Setter
    private int sidecarDefaultErrorCode;

    @Getter
    @Setter
    private int sidecarGeneratedErrorCode;

    // ---------------------------------------------------------------------------------------------------
    // Underpinning components which the bootstrapping code has to initialize.
    /**
     * Invokers where {@link SidecarSynchronicity#NonBlockingEvent} synchronicity is offloaded.
     */
    @Getter
    @Setter
    private ThreadPoolExecutor asyncExecutors;
    @Getter
    @Setter
    private MasheryEndpointConfigurationDialect configurationDialect;
    @Getter
    @Setter
    private Map<String, SidecarOutputCache> idempotentShallowCache;
    @Getter
    @Setter
    private IdempotentUpdateDebouncer idempotentUpdateDebounce;
    @Getter
    @Setter
    private SidecarConfigurationStore configStore;
    @Getter
    @Setter
    private SidecarStacks sidecarStacks;
    @Getter
    @Setter
    private EndpointConfigurationProvider localConfigurationProvider;
    @Getter
    @Setter
    private ElementsFactory supportedElements;
    @Getter
    @Setter
    private ProcessorServices processorServices;
    @Getter
    @Setter
    private CircuitBreaker circuitBreaker;
    @Getter
    @Setter
    private ProcessorKeySet alcpIdentities;
    @Getter
    @Setter
    private CounterpartKeySet alcpCounterpartKeys;
    @Getter
    @Setter
    private ALCPAlgorithmFactory alcpFactory;

    public SidecarProcessor() {
    }

    // --------------------------------------------------------------------
    // Getters and setters for individual underpinning components.

    /**
     * Injects the objects used in the idempotent handling.
     * objects
     *
     * @param bouncer      bouncer of repeated cache attempts
     * @param shallowCache shallow cache
     */
    void useIdempotentDependencies(IdempotentUpdateDebouncer bouncer, Map<String, SidecarOutputCache> shallowCache) {
        this.idempotentUpdateDebounce = bouncer;
        this.idempotentShallowCache = shallowCache;
    }

    @Override
    public void handleEvent(TrafficEvent trafficEvent) {
        if (trafficEvent instanceof PreProcessEvent) {
            PreProcessEvent ppe = (PreProcessEvent) trafficEvent;

            PreProcessorSidecarRuntime cfg = configStore.getPreProcessor(ppe);
            if (cfg == null) {
                sendError(ppe.getCallContext().getResponse(), PreCondition);
                return;
            }

            SidecarResponseDecision decision = SidecarResponseDecision.Continue;

            // The pre-processing part can consist of three different points:
            // - static modification
            // - pre-flight handling
            // - sidecar handling
            // It can happen that, for some cases, only static modification will be necessary
            // to achieve the purpose of the particular integration.

            // Apply static modification. This is needed mainly to override SaaS-configured API call routing with the
            // routing applicable for the Mashery machine is actually residing. In a typical scenario, this sidecar
            // synthetic output will also add a header indicating the origin or a machine identity.

            if (cfg.getStaticModification() != null) {
                try {

                    decision = applySidecarOutput(ppe, cfg.getStaticModification());
                    if (decision.stops()) {
                        return;
                    }
                } catch (IOException ex) {
                    log.error(String.format("Failed to apply static configuration on endpoint %s: %s", ppe.getEndpoint(), ex.getMessage()));
                    sendError(ppe.getCallContext().getResponse(), SidecarInputPoint.StaticModification);
                }
            }


            // The main purpose of the pre-flight check is to disable the traffic to the API when a specific
            // condition is either met or violated. Secondary purpose is to add additional headers that "enrich" the identification
            // of the service, endpoint, or a package.

            if (decision.continues() && cfg.demandsPreflightHandling()) {
                decision = decision.after(handlePreFlight(ppe, cfg.getPreflightBuilder()));
                if (decision.stops()) {
                    return;
                }
            }

            // The sidecar handling, in the absence of the better term, is the invocation of the sidecar processor.
            // Where the configuration demands only static modification, then neither pre-flight nor sidecar
            // processing will be invoked.
            if (decision.continues() && cfg.demandsSidecarHandling()) {
                try {
                    decision = decision.after(handlePreProcessor(ppe, cfg.getPreProcessBuilder()));
                    if (decision.stops()) {
                        return;
                    }
                } catch (IOException ex) {
                    if (cfg.getPreProcessBuilder().isFailsafe()) {
                        log.warn(String.format("Sidecar processing failed on endpoint %s.", ppe.getEndpoint().getExternalID()));
                    } else {
                        log.error(String.format("I/O exception during processing of a call on endpoint %s: %s. This endpoint is sure-fire, error will be returned to the API clinet",
                                ppe.getEndpoint().getExternalID(),
                                ex.getMessage())
                                , ex);
                        sendError(ppe.getCallContext().getResponse(), PreProcessor);
                        return;
                    }
                }
            }

            // In the end, if the request has to be executed immediately, do it.
            if (decision.continuesWithChangeRoute()) {
                executeRequestDirectly(ppe, cfg.isExecutePostProcessing());
            }
        } else if (trafficEvent instanceof PostProcessEvent) {
            PostProcessEvent ppe = (PostProcessEvent) trafficEvent;

            PostProcessSidecarInputBuilder cfg = configStore.getPostProcessor(ppe);
            if (cfg == null) {
                sendError(ppe.getCallContext().getResponse(), PostProcessor);
                return;
            }

            try {
                handlePostProcessor(ppe, cfg);
            } catch (IOException ex) {
                if (cfg.isFailsafe()) {
                    // In case the sidecar is fail-safe, only the error message will be logged.
                    log.warn(String.format("Sidecar post-processing failed for endpoint %s.", ppe.getEndpoint().getExternalID()));
                } else {
                    sendError(ppe.getCallContext().getResponse(), SidecarInputPoint.PostProcessor);
                }
            }
        }
    }

    /**
     * Execute the pre-processor request
     *
     * @param ppe                   pre-processor request
     * @param executePostProcessing if post-processing needs to be executed
     */
    private void executeRequestDirectly(PreProcessEvent ppe, boolean executePostProcessing) {
        final TrafficManagerResponse tmr = ppe.getCallContext().getResponse();

        try {
            final HTTPClientResponse res = ppe.getClientRequest().send();

            final HTTPServerResponse mashResponse = tmr.getHTTPResponse();

            mashResponse.setStatusCode(res.getStatusCode());
            mashResponse.setStatusMessage(res.getStatusMessage());

            for (String h : res.getHeaders()) {
                mashResponse.getHeaders().set(h, res.getHeaders().get(h));
            }

            mashResponse.setBody(new PipedContentProducer(res.getBody()));

            tmr.setComplete();

            if (executePostProcessing) {
                handleEvent(new DelegatingPostProcessEvent(ppe, res));
            }
        } catch (HTTPClientException e) {
            sendCurtailed(tmr, 552, "Back-end execution failed");
        }
    }

    void invokeInBackground(SidecarInvocationData cmd) throws IOException {
        if (asyncExecutors == null) {
            throw new IOException("Attempt to invoke in background without initializing background invokers");
        } else {
            Runnable asyncTask = new NonBlockingInvocationTask(cmd);
            if (asyncExecutors.getQueue().size() < MAX_NONBLOCKING_QUEUE) {
                asyncExecutors.submit(asyncTask);
            } else {
                asyncExecutors.execute(asyncTask);
            }
        }
        // else: the queue is too big to accept any further messages.
    }

    /**
     * Performs pre-flight handling
     *
     * @param ppe              pre-flight processor event.
     * @param preflightBuilder builder of the input to the preflight sidecar function
     * @return {@link SidecarResponseDecision} for what should happen next.
     */
    private SidecarResponseDecision handlePreFlight(PreProcessEvent ppe, PreFlightSidecarInputBuilder preflightBuilder) {
        SidecarInvocationData cmd = handleScope(ppe, preflightBuilder.build(ppe), Preflight);
        if (cmd == null) {
            return SidecarResponseDecision.Continue;
        }

        try {
            SidecarPreProcessorOutput sOut = invokeIdempotentAware(cmd);
            return applySidecarOutput(ppe, sOut);
        } catch (IOException ex) {
            if (preflightBuilder.isFailsafe()) {
                // TODO: better logging
                log.warn(String.format("Pre-flight check failed for endpoint %s.", ppe.getEndpoint().getExternalID()));
                return SidecarResponseDecision.Continue;
            } else {
                sendError(ppe.getCallContext().getResponse(), SidecarInputPoint.Preflight);
                return SidecarResponseDecision.Stop;
            }
        }
    }

    private SidecarInvocationData handleScope(ProcessorEvent ppe, SidecarInvocationData cmd, SidecarInputPoint point) {
        if (cmd == null) {
            return null;
        }

        switch (cmd.getRelevance()) {
            case ServiceNotReady:
                sendCurtailed(ppe.getCallContext().getResponse(), 598, MSG_SERVICE_CANNOT_BE_PROVIDED);
                return null;
            case ClientError:
                sendCurtailed(ppe.getCallContext().getResponse(), 400, PRE_CONDITION_FAILURE_MSG);
                return null;
            case InternalFault:
                sendError(ppe.getCallContext().getResponse(), point);
                return null;
            case Invoke:
                return cmd;
            case Noop:
            case Ignored:
            default:
                return null;
        }
    }

    private SidecarResponseDecision handlePreProcessor(PreProcessEvent ppe, PreProcessSidecarInputBuilder builder) throws IOException {

        SidecarInvocationData cmd = handleScope(ppe, builder.build(ppe), PreProcessor);
        // If no further action is required, then return.
        if (cmd == null) {
            return SidecarResponseDecision.Continue;
        }

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
                sendError(ppe.getCallContext().getResponse(), PreProcessor);
                break;
        }

        if (sidecarOutput != null) {
            return applySidecarOutput(ppe, sidecarOutput);
        } else {
            return SidecarResponseDecision.Continue;
        }
    }

    private SidecarPreProcessorOutput invokePreProcessorRelayAware(SidecarInvocationData cmd) throws IOException {
        SidecarPreProcessorOutput retVal = invokeIdempotentAware(cmd);

        if (retVal != null) {
            if (retVal.relaysMessageToPostprocessor()) {
                if (retVal.getRelayParams() != null) {
                    cmd.logEntry(RELAY_PARAMS, retVal.createSerializableRelayParameters());
                }
            }
            return retVal;
        } else {
            return null;
        }
    }

    long now() {
        return System.currentTimeMillis();
    }

    SidecarPreProcessorOutput invokeIdempotentAware(SidecarInvocationData cmd) throws IOException {
        SidecarPreProcessorOutput retVal;
        if (cmd.isIdempotentAware()) {
            long callStartTs = now();

            try {
                final String cacheKey = cmd.getCacheKey();

                // We will try reading the idempotent data from the shallow cache. If it is absent,
                // the idempotent response will be placed in a shallow cache to speed things up even further.
                SidecarOutputCache soc = idempotentShallowCache.get(cacheKey);

                // The data returned from the cache should not have expired against the current
                // timestamp. If it did, forget it in the shallow cache.
                if (soc != null && !soc.isValid(callStartTs)) {
                    idempotentShallowCache.remove(cacheKey);
                    soc = null;
                }

                if (soc == null) {
                    soc = (SidecarOutputCache) cmd.getCache().get(cmd.getStackClassLoader(), cacheKey);

                    // Only non-null cached will be used. Additionally, the objects must be considered valid "storable", i.e.
                    // they should not be at the very end of their storage life cycle, which is needed to avoid displacing
                    // active objects in the shallow memory hash.
                    if (soc != null) {
                        if (soc.isValid(callStartTs)) {
                            if (soc.isStorable(callStartTs)) {
                                idempotentShallowCache.put(cacheKey, soc);
                            }
                        } else {
                            soc = null;
                        }
                    }
                }

                if (soc != null) {
                    // Refresh the storage of the idempotent object if it's still used.
                    if (soc.needsStorageRefresh(callStartTs) && !idempotentUpdateDebounce.shouldBounce(cacheKey)) {
                        soc.extendStoreWith(IDEMPOTENT_STORAGE_UNIT, IDEMPOTENT_STORAGE_ADVANCE);
                        cacheSidecarOutput(cmd.getCache(), callStartTs, cacheKey, soc);
                    }

                    return soc.getValue();
                }
            } catch (CacheException ex) {
                log.warn(String.format("Error is storing: %s", ex.getMessage()));
            }
        }

        retVal = invokePreProcessorWithCircuitBreaker(cmd);
        if (cmd.isIdempotentAware() && retVal != null && retVal.idempotentAware()) {
            long answerReceivedTs = now();
            SidecarOutputCache soc = new SidecarOutputCache(retVal.toSerializableForm(),
                    answerReceivedTs,
                    IDEMPOTENT_STORAGE_UNIT,
                    IDEMPOTENT_STORAGE_ADVANCE);

            cacheSidecarOutput(cmd.getCache(), answerReceivedTs, cmd.getCacheKey(), soc);
            // The cached value is NOT loaded in memory. There is no guarantee that an idempotent data will
            // be used. Loading fresh data in memory can displace objects in the shallow cache that are
            // actually used. Therefore the shallow cache will be filled only when it will be actually
            // used.
        }
        return retVal;
    }

    private void cacheSidecarOutput(Cache mashCache, long queryTs, String cacheKey, SidecarOutputCache soc) {
        if (soc.getStorageDuration(queryTs) > 0) {
            try {
                mashCache.put(cacheKey, soc, soc.getStorageDuration(queryTs));
            } catch (CacheException e) {
                log.warn(String.format("Error is storing: %s", e.getMessage()));
            }
        }
    }

    SidecarPreProcessorOutput invokePreProcessorWithCircuitBreaker(SidecarInvocationData cmd) throws
            IOException {
        if (this.circuitBreaker.isOpen(cmd.getEndpointId())) {
            throw new IOException("Sidecar circuit breaker is open on this endpoint");
        }

        if (cmd.getStack() != null) {
            try {
                final SidecarPreProcessorOutput retVal = cmd.getStack().invoke(cmd.getStackConfiguration(), cmd, SidecarPreProcessorOutput.class);
                this.circuitBreaker.trackSuccess(cmd.getEndpointId());
                return retVal;
            } catch (IOException ex) {
                this.circuitBreaker.trackSuccess(cmd.getEndpointId());
                throw ex;
            }
        } else {
            throw new IOException("Null stack is not allowed");
        }
    }

    SidecarPostProcessorOutput invokePostProcessorWithCircuitBreaker(SidecarInvocationData cmd) throws
            IOException {
        if (this.circuitBreaker.isOpen(cmd.getEndpointId())) {
            throw new IOException("Sidecar circuit breaker is open on this endpoint");
        }

        if (cmd.getStack() != null) {
            try {
                final SidecarPostProcessorOutput retVal = cmd.getStack().invoke(cmd.getStackConfiguration(), cmd, SidecarPostProcessorOutput.class);
                this.circuitBreaker.trackSuccess(cmd.getEndpointId());
                return retVal;
            } catch (IOException ex) {
                this.circuitBreaker.trackError(cmd.getEndpointId());
                throw ex;
            }
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
    SidecarResponseDecision applySidecarOutput(PreProcessEvent ppe, SidecarPreProcessorOutput output) throws IOException {
        // Null object means do-nothing.
        if (output != null) {
            if (output.getTerminate() != null) {
                sendCurtailed(ppe.getCallContext().getResponse(), output.getTerminate());
                return SidecarResponseDecision.Stop;
            } else if (output.getModify() != null) {
                SidecarResponseDecision continueDecision = SidecarResponseDecision.Continue;

                RequestModificationCommand cmd = output.getModify();

                // Apply any modifications that were returned form the function.
                final HTTPClientRequest clientRequest = ppe.getClientRequest();
                if (cmd.getDropHeaders() != null) {
                    cmd.getDropHeaders().forEach(h -> clientRequest.getHeaders().remove(h));
                }

                if (cmd.getPassHeaders() != null) {
                    cmd.getPassHeaders().forEach((key, value) -> clientRequest.getHeaders().set(key, value));
                }

                if (cmd.getChangeRoute() != null) {
                    RequestRoutingChangeBean r = cmd.getChangeRoute();

                    if (r.getUri() != null) {
                        clientRequest.setURI(r.getUri());
                    } else if (r.outboundURINeedsChanging()) {
                        try {
                            URL current = new URL(clientRequest.getURI());
                            String protocol = r.getProtocol() != null ? r.getProtocol() : current.getProtocol();
                            String host = r.getHost() != null ? r.getHost() : current.getHost();
                            String file = getRoutingTargetFile(ppe, r, current);
                            int port = r.getPort() != null ? r.getPort() : current.getPort();

                            URL newURL = new URL(protocol, host, port, file);

                            // Calling newURL.toURI is necessary to extract the correct port
                            clientRequest.setURI(newURL.toString());

                            if (r.outboundHostChanged()) {
                                continueDecision = SidecarResponseDecision.ContinueWithChangedRoute;
                            }
                        } catch (MalformedURLException ex) {
                            throw new IOException(String.format("Could not apply target route: %s", ex.getMessage()));
                        }
                    }
                    if (r.getHttpVerb() != null) {
                        clientRequest.setMethod(r.getHttpVerb());
                    }
                }

                // Applying modification of the payload. The logic is that:
                // - if the sidecar  has supplied modifications of the fragments, these would be applied, otherwise
                // - the payload will be replaced with whatever sidecar has supplied.
                if (cmd.modifiesFragments()) {
                    final HTTPServerRequest serverReq = ppe.getServerRequest();

                    if (bearsJson(serverReq.getHeaders(), serverReq.getBody())) {

                        byte[] origContent = getContentsOf(serverReq.getBody());

                        try {
                            applyJsonPayloadModification(origContent
                                    , cmd.getDropFragments()
                                    , cmd.getPassFragments()
                                    , clientRequest.getHeaders()
                                    , (modifiedJson) -> clientRequest.setBody(produceFromBinary(modifiedJson)));

                        } catch (IOException ex) {
                            // Client didn't send a well-formed JSON.
                            sendCurtailed(ppe.getCallContext().getResponse(), 400, "Malformed JSON");
                            return SidecarResponseDecision.Stop;
                        }
                    }
                } else {
                    if (cmd.getPayload() != null) {

                        if (cmd.getBase64Encoded() != null && cmd.getBase64Encoded()) {
                            clientRequest.setBody(produceFromBase64String(cmd.getPayload()));
                        } else {
                            clientRequest.setBody(produceFromString(cmd.getPayload()));
                        }
                    } else if (cmd.getJsonPayload() != null) {
                        if (!cmd.addsContentType()) {
                            clientRequest.getHeaders().set("Content-Type", "application/json");
                        }

                        // The JSON payload will be sent depending on it's size. Payloads exceeding 256 bytes
                        // will be gzipped for efficiency purposes
                        byte[] transportOptimized = JsonHelper.toTransportOptimizedJSON(cmd.getJsonPayload());
                        if (JsonHelper.isGzipped(transportOptimized)) {
                            clientRequest.getHeaders().set("Content-Encoding", "gzip");
                        }
                        clientRequest.setBody(produceFromBinary(transportOptimized));
                    }
                }

                return continueDecision;
            } else if (output.getReply() != null) {
                ReplyCommand reply = output.getReply();

                final TrafficManagerResponse response = ppe.getCallContext().getResponse();
                int code = reply.getStatusCode() != null ? reply.getStatusCode() : 200;

                response.getHTTPResponse().setStatusCode(code);

                if (reply.getPassHeaders() != null) {
                    reply.getPassHeaders().forEach((k, v) -> response.getHTTPResponse().getHeaders().add(k, v));
                }

                if (reply.getPayload() != null) {
                    if (reply.getBase64Encoded() != null && reply.getBase64Encoded()) {
                        response.getHTTPResponse().setBody(produceFromBase64String(reply.getPayload()));
                    } else {
                        response.getHTTPResponse().setBody(produceFromString(reply.getPayload()));
                    }
                } else if (reply.getJsonPayload() != null) {
                    if (!reply.addsContentType()) {
                        response.getHTTPResponse().getHeaders().set("content-type", "application/json");
                    }

                    // The JSON payload will be sent depending on it's size. Payloads exceeding 256 bytes
                    // will be gzipped for efficiency purposes
                    byte[] transportOptimized = JsonHelper.toTransportOptimizedJSON(reply.getJsonPayload());
                    if (JsonHelper.isGzipped(transportOptimized)) {
                        response.getHTTPResponse().getHeaders().set("content-encoding", "gzip");
                    }
                    response.getHTTPResponse().setBody(produceFromBinary(transportOptimized));
                }

                response.setComplete();
                return SidecarResponseDecision.Stop;
            }
        }
        return SidecarResponseDecision.Continue;
    }

    private void applyJsonPayloadModification(byte[] origContent
            , List<String> dropFragments
            , Map<String, Object> passFragments
            , MutableHTTPHeaders headers
            , Consumer<byte[]> valueWriter) throws IOException {

        ObjectNode on = JsonHelper.readTransportOptimizedJSONForModification(origContent);
        if (on != null) {

            if (dropFragments != null) {
                dropFragments.forEach((f) -> JsonHelper.remove(on, f));
            }

            if (passFragments != null) {
                passFragments.forEach((path, v) -> JsonHelper.replacePath(on, path, v));
            }

            byte[] transportOptimized = JsonHelper.toTransportOptimizedJSON(on);
            if (JsonHelper.isGzipped(transportOptimized)) {
                headers.set("Content-Encoding", "gzip");
            }
            valueWriter.accept(transportOptimized);
        }
    }


    /**
     * The file part of the target URL is computed as follows:
     * <ol>
     *     <li>If the {@link RequestRoutingChangeBean#getFile()} specifies this directly, then this one is used; otherwise</li>
     *     <li>If the {@link RequestRoutingChangeBean#getFileBase()} is specified, the remained of the operation will be appended</li>
     *     <li>Otherwise, the original will be used.</li>
     * </ol>
     *
     * @param ppe     Pre-process or event
     * @param r       routing bean
     * @param current current routing URL
     * @return String value to be used for file part of the URL.
     */
    String getRoutingTargetFile(PreProcessEvent ppe, RequestRoutingChangeBean r, URL current) {
        if (r.getFile() != null) {
            return r.getFile();
        } else if (r.getFileBase() != null) {
            StringBuilder sb = new StringBuilder(r.getFileBase());
            final String remainderPath = ppe.getCallContext().getRequest().getPathRemainder();

            boolean slashes = r.getFileBase().endsWith("/") || remainderPath.startsWith("/");
            if (!slashes) {
                sb.append("/");
            }
            sb.append(remainderPath);
            // TODO: Need to check how query string is getting translated here
            return sb.toString();
        } else {
            return current.getFile();
        }
    }

    SidecarResponseDecision applySidecarOutput(PostProcessEvent ppe, SidecarInvocationData sid, SidecarPostProcessorOutput output) {

        if (output.getTerminate() != null) {
            sendCurtailed(ppe.getCallContext().getResponse(), output.getTerminate());
            return SidecarResponseDecision.Stop;
        }

        boolean bodyUpdated = false;
        if (output.getModify() != null) {
            ResponseModificationCommand cmd = output.getModify();

            // Apply any modifications that were returned form the function.
            final HTTPServerResponse serverResponse = ppe.getServerResponse();

            if (cmd.getDropHeaders() != null) {
                cmd.getDropHeaders().forEach(h -> serverResponse.getHeaders().remove(h));
            }

            if (cmd.getPassHeaders() != null) {
                final MutableHTTPHeaders headers = serverResponse.getHeaders();
                cmd.getPassHeaders().forEach(headers::set);
            }

            if (cmd.modifiesFragments()) {
                final HTTPClientResponse clientRes = ppe.getClientResponse();

                if (bearsJson(clientRes.getHeaders(), clientRes.getBody())) {

                    try {
                        byte[] origContent = getContentsOf(clientRes.getBody());
                        applyJsonPayloadModification(origContent
                                , cmd.getDropFragments()
                                , cmd.getPassFragments()
                                , serverResponse.getHeaders()
                                , (modifiedJson) -> serverResponse.setBody(produceFromBinary(modifiedJson)));

                        bodyUpdated = true;

                    } catch (IOException ex) {
                        // Client didn't send a well-formed JSON.
                        sendCurtailed(ppe.getCallContext().getResponse(), 553, "Malformed Response JSON");
                        return SidecarResponseDecision.Stop;
                    }
                }
            }
            if (cmd.getJsonPayload() != null) {
                if (!cmd.addsContentType()) {
                    serverResponse.getHeaders().set("content-type", "application/json");
                }
                serverResponse.getHeaders().set("content-encoding", "gzip");
                serverResponse.setBody(produceFromBinary(toGzippedJson(cmd.getJsonPayload())));
                bodyUpdated = true;
            } else if (cmd.getPayload() != null) {
                if (cmd.getBase64Encoded() != null && cmd.getBase64Encoded()) {
                    serverResponse.setBody(produceFromBase64String(cmd.getPayload()));
                    bodyUpdated = true;
                } else {
                    serverResponse.setBody(produceFromString(cmd.getPayload()));
                    bodyUpdated = true;
                }
            }
        }

        // If the body of the response was not updated, and if the body of the API origin response was extracted
        // into the sidecar input, it is necessary to send the original API origin response body.

        if (!bodyUpdated) {
            flushRawIntermediateBody(ppe, sid);
        }

        return SidecarResponseDecision.Continue;
    }

    void flushRawIntermediateBody(PostProcessEvent ppe, SidecarInvocationData sid) {
        if (sid.hasIntermediary(RAW_RESPONSE_PAYLOAD_KEY)) {
            ppe.getServerResponse().setBody(produceFromBinary(sid.getIntermediary(RAW_RESPONSE_PAYLOAD_KEY)));
        }
    }

    void handlePostProcessor(PostProcessEvent ppe, PostProcessSidecarInputBuilder builder) throws IOException {

        // Post-processor building would fetch relayed data.
        SidecarInvocationData cmd = handleScope(ppe, builder.build(ppe), PostProcessor);

        // Null value returned from the scope handling means that the further processing of this API call
        // is not necessary at the post-processor point. Returning from this function.
        if (cmd == null) {
            return;
        }


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
            applySidecarOutput(ppe, cmd, sidecarOutput);
        } else {
            flushRawIntermediateBody(ppe, cmd);
        }
    }


    /**
     * Sends an error to the traffic manager response.
     *
     * @param response response object to write an opaque response message.
     */
    void sendError(TrafficManagerResponse response, SidecarInputPoint point) {
        response.getHTTPResponse().setStatusCode(sidecarGeneratedErrorCode);
        response.getHTTPResponse().getHeaders().set("Content-Type", "application/xml");

        String msg = "";
        switch (point) {
            case PreCondition:
                msg = MSG_PRECONDITION_ERROR;
                break;
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

        response.getHTTPResponse().setBody(produceFromString(formatCurtailedMessage(msg)));
        response.setComplete();
    }

    /**
     * @param response response
     * @param code     code
     * @param message  message to format. If not specified, then {@link #MSG_SERVICE_CANNOT_BE_PROVIDED}
     *                 string will be used.
     */
    void sendCurtailed(TrafficManagerResponse response, Integer code, String message) {
        final HTTPServerResponse http = response.getHTTPResponse();

        http.setStatusCode(code != null ? code : sidecarDefaultErrorCode);

        http.getHeaders().set("Content-Type", "application/xml");

        String uMessage = message != null ? message : MSG_SERVICE_CANNOT_BE_PROVIDED;
        http.setBody(produceFromString(formatCurtailedMessage(uMessage)));

        response.setComplete();
    }

    public static String formatCurtailedMessage(String uMessage) {
        return String.format("<h1><![CDATA[%s]]></h1>", uMessage);
    }

    /**
     * Terminates pre-processing of the request and sends the lambda-provided body to the response
     *
     * @param response response object
     * @param cmd      termination command.
     */
    void sendCurtailed(TrafficManagerResponse response, TerminateCommand cmd) {
        if (cmd.getPayload() != null || cmd.getJsonPayload() != null) {

            final HTTPServerResponse clResp = response.getHTTPResponse();

            if (cmd.getStatusCode() != null) {
                clResp.setStatusCode(cmd.getStatusCode());
            } else {
                clResp.setStatusCode(sidecarDefaultErrorCode);
            }

            final MutableHTTPHeaders headers = clResp.getHeaders();

            if (cmd.getPassHeaders() != null) {
                cmd.getPassHeaders().forEach(headers::set);
            }

            if (cmd.getJsonPayload() != null) {
                if (!cmd.addsContentType()) {
                    headers.set("Content-Type", "application/json");
                }

                try {
                    byte[] buf = toTransportOptimizedJSON(cmd.getJsonPayload());
                    if (isGzipped(buf)) {
                        headers.set("Content-Encoding", "gzip");
                    }

                    clResp.setBody(produceFromBinary(buf));
                } catch (IOException ex) {
                    //
                }
            } else if (cmd.getPayload() != null) {
                if (cmd.getBase64Encoded() != null && cmd.getBase64Encoded()) {
                    clResp.setBody(produceFromBinary(base64Decoder.decode(cmd.getPayload())));
                } else {
                    clResp.setBody(produceFromString(cmd.getPayload()));
                }
            }

            response.setComplete();
        } else {
            sendCurtailed(response, cmd.getStatusCode(), cmd.getMessage());
        }
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

    public static void addContentBody(HTTPHeaders headers, byte[] bodyContent, SidecarInputHTTPMessage to) {
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

            // The length parameter will be sent only if the body is Base-64 encoded.
            // If it's string, then the string length is the length of the body.
            if (sendAsBase64) {
                to.setPayloadLength((long) bodyContent.length);
                to.setPayloadBase64Encoded(true);
                to.setPayload(PayloadOperations.getBase64Of(bodyContent));
            } else {
                to.setPayload(PayloadOperations.getContentOf(bodyContent, strCharset));
            }
        }
    }


    /**
     * Resolves the stack referred to in this configuration.
     *
     * @param cfg configuration
     * @return stack to use, or null if not supported or installed.
     */
    public SidecarStack getStackFor(SidecarConfiguration cfg) {
        return sidecarStacks.getStackFor(cfg);
    }

//    public InMemoryStack getInMemoryStack() {
//        return sidecarStacks.getInMemoryStack();
//    }

    // ---------------------------------------------------------------------------
    // Setup, start and stop sequence.

    public void setup() {
        if (configStore != null) {
            configStore.bindTo(this);
        }

        if (sidecarStacks != null) {
            sidecarStacks.setup(this);
        }

        if (localConfigurationProvider != null) {
            localConfigurationProvider.setup(this);
        }
    }

    public void start() {


        if (localConfigurationProvider != null) {
            localConfigurationProvider.start();
        }
    }

    public void stop() {
        if (asyncExecutors != null) {
            asyncExecutors.shutdown();
            try {
                asyncExecutors.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                log.error(String.format("Wait for the termination of background invokers terminated: %s", ex.getMessage()),
                        ex);
            }
        }

        if (localConfigurationProvider != null) {
            localConfigurationProvider.shutdown();
        }
    }

    public void describeStateOnStart() {
        log.warn(" -> Sidecar processor state <- ");
        log.warn(String.format(" - Async executors enabled: %b", asyncExecutors != null));
        log.warn(String.format(" - Configuration dialect: %s", configurationDialect != null ? configurationDialect.getName() : "--UNDEFINED--"));
        log.warn(String.format(" - Idempotent shallow cache: %s", idempotentShallowCache != null ? "defined" : "--NOT DEFINED--"));
        log.warn(String.format(" - Idempotent update debouncer: %s", idempotentUpdateDebounce != null ? "defined" : "--NOT DEFINED--"));
        log.warn(String.format(" - Configuration store: %s", configStore != null ? configStore.getName() : "--NOT DEFINED--"));
        log.warn(String.format(" - Sidecar stacks: %s", sidecarStacks != null ? sidecarStacks.describe() : "--NOT DEFINED--"));
        log.warn(String.format(" - Local config store: %s", localConfigurationProvider != null ? localConfigurationProvider.describe() : "--NOT DEFINED--"));
        log.warn(String.format(" - Supported elements: %s", supportedElements != null ? "defined" : "--NOT DEFINED--"));
        log.warn(String.format(" - Processor services: %s", processorServices != null ? "defined" : "--NOT DEFINED--"));
        log.warn(String.format(" - Circuit breaker: %s", circuitBreaker != null ? "defined" : "--NOT DEFINED--"));
        log.warn(String.format(" - ALCP self-identity key set: %s", alcpIdentities != null ? "defined" : "--NOT DEFINED--"));
        log.warn(String.format(" - ALCP counterpart key set: %s", alcpCounterpartKeys != null ? "defined" : "--NOT DEFINED--"));
        log.warn(String.format(" - ALCP factory: %s", alcpFactory != null ? "defined" : "--NOT DEFINED--"));
    }


// -----------------------------------------------------------------------------------------------
// Private classes

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

@AllArgsConstructor
static class DelegatingPostProcessEvent implements PostProcessEvent {

    PreProcessEvent delegate;
    HTTPClientResponse response;

    @Override
    public HTTPServerResponse getServerResponse() {
        return delegate.getCallContext().getResponse().getHTTPResponse();
    }

    @Override
    public HTTPClientResponse getClientResponse() {
        return response;
    }

    @Override
    public ResponseFilter getContentFilter() {
        return null;
    }

    @Override
    public APICall getCallContext() {
        return delegate.getCallContext();
    }

    @Override
    public Endpoint getEndpoint() {
        return delegate.getEndpoint();
    }

    @Override
    public DebugContext getDebugContext() {
        return delegate.getDebugContext();
    }

    @Override
    public AuthorizationContext getAuthorizationContext() {
        return delegate.getAuthorizationContext();
    }

    @Override
    public Cache getCache() {
        return delegate.getCache();
    }

    @Override
    public Key getKey() {
        return delegate.getKey();
    }

    @Override
    public Method getMethod() {
        return delegate.getMethod();
    }

    @Override
    public TrafficEventType getType() {
        return PostProcessEvent.EVENT_TYPE;
    }
}

enum SidecarResponseDecision {
    Stop, Continue, ContinueWithChangedRoute;

    boolean stops() {
        return this == Stop;
    }

    boolean continuesWithChangeRoute() {
        return this == ContinueWithChangedRoute;
    }

    SidecarResponseDecision after(SidecarResponseDecision nextDesc) {
        switch (this) {
            case Stop:
                return Stop;
            case Continue:
                return nextDesc;
            case ContinueWithChangedRoute:
                if (nextDesc == Stop) {
                    return Stop;
                } else {
                    return this;
                }
            default:
                return nextDesc;
        }
    }

    public boolean continues() {
        return this == Continue;
    }
}

}























