package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.builders.PostProcessSidecarInputBuilder;
import com.airfranceklm.amt.sidecar.model.MasheryProcessorPointReference;
import com.airfranceklm.amt.sidecar.model.SidecarInputPoint;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ProductionConfigurationStore implements SidecarConfigurationStore {

    public static Logger log = LoggerFactory.getLogger(ProductionConfigurationStore.class);

    private Map<String, TimeCacheable<PreProcessEvent, PreProcessorSidecarRuntime>> preProcessors = new HashMap<>();
    private Map<String, TimeCacheable<PostProcessEvent, PostProcessSidecarInputBuilder>> postProcessors = new HashMap<>();

    private long checkFrequency = TimeUnit.MINUTES.toMillis(1);

    private final Object syncMon = new Object();

    private SidecarProcessor processor;

    public ProductionConfigurationStore() {
    }

    public TimeCacheable<PreProcessEvent, PreProcessorSidecarRuntime> getStoredPreProcessor(String endpointId) {
        return preProcessors.get(endpointId);
    }

    public TimeCacheable<PostProcessEvent, PostProcessSidecarInputBuilder> getStoredPostProcessors(String endpointId) {
        return postProcessors.get(endpointId);
    }

    @Override
    public String getName() {
        return "Production-grade; default";
    }

    @Override
    public PreProcessorSidecarRuntime getPreProcessor(PreProcessEvent ppe) {
        final String endpId = ppe.getEndpoint().getExternalID();

        TimeCacheable<PreProcessEvent, PreProcessorSidecarRuntime> tcRuntime = preProcessors.get(endpId);
        if (tcRuntime != null) {
            return tcRuntime.get(ppe);
        } else {
            synchronized (syncMon) {
                // Second guard against race conditions.
                final String serviceId = ppe.getEndpoint().getAPI().getExternalID();
                return ensurePreProcessorCache(serviceId, endpId).get(ppe);
            }
        }
    }

    private TimeCacheable<PreProcessEvent, PreProcessorSidecarRuntime> ensurePreProcessorCache(String serviceId, String endpId) {
        TimeCacheable<PreProcessEvent, PreProcessorSidecarRuntime> tcRuntime;
        if (!preProcessors.containsKey(endpId)) {
            tcRuntime = new TimeCacheable<>(checkFrequency,
                    (event) -> event.getEndpoint().getProcessor().getPreProcessorParameters(),
                    (pEvent) ->  ConfigurationStoreHelper.createPreProcessorRuntime(pEvent, processor, processor.getSupportedElements())
            );
            tcRuntime.setPoint(SidecarInputPoint.PreProcessor.at(serviceId, endpId));
            preProcessors.put(endpId, tcRuntime);
        } else {
            tcRuntime = preProcessors.get(endpId);
        }
        return tcRuntime;
    }

    private TimeCacheable<PostProcessEvent, PostProcessSidecarInputBuilder> ensurePostProcessorCache(String serviceId, String endpId) {
        TimeCacheable<PostProcessEvent, PostProcessSidecarInputBuilder> tcRuntime;
        if (!postProcessors.containsKey(endpId)) {
            tcRuntime = new TimeCacheable<>(checkFrequency,
                    (event) -> event.getEndpoint().getProcessor().getPreProcessorParameters(),
                    (pEvent) ->  ConfigurationStoreHelper.getPostProcessSidecarInputBuilder(pEvent, processor, processor.getSupportedElements())
            );
            tcRuntime.setPoint(SidecarInputPoint.PostProcessor.at(serviceId, endpId));

            postProcessors.put(endpId, tcRuntime);
        } else {
            tcRuntime = postProcessors.get(endpId);
        }

        return tcRuntime;
    }

    @Override
    public PostProcessSidecarInputBuilder getPostProcessor(PostProcessEvent ppe) {
        final String endpId = ppe.getEndpoint().getExternalID();

        TimeCacheable<PostProcessEvent, PostProcessSidecarInputBuilder> tcRuntime = postProcessors.get(endpId);
        if (tcRuntime != null) {
            return tcRuntime.get(ppe);
        } else {
            synchronized (syncMon) {
                // Second guard against race conditions.
                final String serviceId = ppe.getEndpoint().getAPI().getExternalID();
                return ensurePostProcessorCache(serviceId, endpId).get(ppe);
            }
        }
    }

    @Override
    public void acceptConfigurationChange(MasheryProcessorPointReference endpRef, String declaredInFile, PreProcessorSidecarRuntime cfg) {
        synchronized (syncMon) {
            ensurePreProcessorCache(endpRef.getServiceId(), endpRef.getEndpointId()).overrideFrom(declaredInFile, cfg);
            log.warn(String.format("Pre-processing of %s/%s endpoint is overridden from configuration supplied in file %s",
                    endpRef.getServiceId(),
                    endpRef.getEndpointId(),
                    declaredInFile));
        }
    }

    @Override
    public void acceptConfigurationChange(MasheryProcessorPointReference endpRef, String declaredInFile, PostProcessSidecarInputBuilder cfg) {
        synchronized (syncMon) {
            ensurePostProcessorCache(endpRef.getServiceId(), endpRef.getEndpointId()).overrideFrom(declaredInFile, cfg);

            log.warn(String.format("Post processing of %s/%s endpoint is overridden from configuration supplied in file %s",
                    endpRef.getServiceId(),
                    endpRef.getEndpointId(),
                    declaredInFile));
        }
    }

    @Override
    public void bindTo(SidecarProcessor processor) {
        this.processor = processor;
    }

    /**
     * Returns the set of references that are declared in this file.
     * @param file path to the file
     * @return set of references, or empty set if no references are found.
     */
    @Override
    public Set<MasheryProcessorPointReference> getDeclaredIn(String file) {
        HashSet<MasheryProcessorPointReference> retVal = new HashSet<>();

        synchronized (syncMon) {
            preProcessors.values().forEach((tc) -> {
                if (file.equals(tc.getDeclaredIn())) {
                    retVal.add(tc.getPoint());
                }
            });
        }

        return retVal;
    }

    @Override
    public void forget(MasheryProcessorPointReference ref) {
        synchronized (syncMon) {
            if (preProcessors.containsKey(ref.getEndpointId())) {
                preProcessors.get(ref.getEndpointId()).resetOverride();
            }

            if (postProcessors.containsKey(ref.getEndpointId())) {
                postProcessors.get(ref.getEndpointId()).resetOverride();
            }
        }
    }
}
