package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.config.MasheryConfigSidecarConfigurationBuilder;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.config.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.input.SidecarInputBuilder;
import com.airfranceklm.amt.sidecar.input.SidecarRuntimeCompiler;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ProductionConfigurationStore implements SidecarConfigurationStore {

    public static Logger log = LoggerFactory.getLogger(ProductionConfigurationStore.class);

    private Map<MasheryPreprocessorPointReference, SidecarConfigurationHolder> configurationStore = new HashMap<>();
    private MasheryConfigSidecarConfigurationBuilder mashBuilder = new MasheryConfigSidecarConfigurationBuilder();

    private long checkFrequency = TimeUnit.MINUTES.toMillis(1);

    private SidecarRuntimeCompiler compiler;
    private final Object syncMon = new Object();

    ProductionConfigurationStore() {
    }

    @Override
    public void bindTo(AFKLMSidecarProcessor processor) {
        this.compiler = new SidecarRuntimeCompiler(processor);
    }

    @Override
    public SidecarConfiguration getConfiguration(ProcessorEvent event) {
        SidecarInputPoint point;
        if (event instanceof PreProcessEvent) {
            point = SidecarInputPoint.PreProcessor;
        } else if (event instanceof PostProcessEvent) {
            point = SidecarInputPoint.PostProcessor;
        } else {
            throw new IllegalArgumentException(String.format("Unsupported event class: %s", event.getClass().getName()));
        }

        MasheryPreprocessorPointReference key = new MasheryPreprocessorPointReference(event, point);
        SidecarConfigurationHolder holder = configurationStore.get(key);
        if (holder != null) {
            // If the configuration is not reloadable by Mashery, don't check anything.
            if (!holder.masheryConfigReloadable) {
                return holder.sidecarConfig;
            }

            if (!holder.upToDate()) {
                synchronized (syncMon) {
                    checkSidecarConfigurationUpToDate(holder, event);
                }
            }
            return holder.sidecarConfig;
        }

        synchronized (syncMon) {
            if (!configurationStore.containsKey(key)) {
                holder = new SidecarConfigurationHolder();
                holder.setConfiguration(mashBuilder.buildFrom(event));
                holder.originalConfiguration = getMasheryConfigurationFor(event, holder.sidecarConfig.getPoint());

                configurationStore.put(key, holder);
            } else {
                holder = configurationStore.get(key);
            }

            return holder.sidecarConfig;
        }

    }

    private void checkSidecarConfigurationUpToDate(SidecarConfigurationHolder holder, ProcessorEvent ppe) {
        //
        Map<String, String> params = getMasheryConfigurationFor(ppe, holder.sidecarConfig.getPoint());

        if (holder.originalConfiguration.equals(params)) {
            holder.checkTime.set(System.currentTimeMillis());
            return;
        }

        // Okay, we have a situation wherein the configuration has been changed from what was cached.
        holder.setConfiguration(mashBuilder.buildFrom(ppe));
        holder.checkTime.set(System.currentTimeMillis());

        holder.refreshBuilders();
    }

    private Map<String, String> getMasheryConfigurationFor(ProcessorEvent ppe, SidecarInputPoint point) {
        Map<String, String> params = null;
        if (point == SidecarInputPoint.PreProcessor) {
            params = ppe.getEndpoint().getProcessor().getPreProcessorParameters();
        } else {
            params = ppe.getEndpoint().getProcessor().getPostProcessorParameters();
        }
        return params;
    }

    @Override
    public SidecarConfiguration getConfiguration(SidecarInputPoint point, String serviceId, String endpointId, Map<String, String> mashConfig) {
        return null; // TODO
    }

    @Override
    public SidecarInputBuilder<PreProcessEvent> getPreProcessorInputBuilder(SidecarConfiguration cfg) {
        SidecarConfigurationHolder h = configurationStore.get(new MasheryPreprocessorPointReference(cfg));
        if (h != null) {
            SidecarInputBuilder<PreProcessEvent> retVal = h.preProcessorBuilder;
            if (retVal == null) {
                synchronized (syncMon) {
                    if (h.preProcessorBuilder == null) {
                        h.preProcessorBuilder = compiler.compilePreProcessor(cfg);
                    }

                    retVal = h.preProcessorBuilder;
                }
            }

            return retVal;
        } else {
            throw new IllegalStateException("Missing configuration in store; check upstream sequence of call.");
        }
    }

    @Override
    public SidecarInputBuilder<PostProcessEvent> getPostProcessorInputBuilder(SidecarConfiguration cfg) {
        SidecarConfigurationHolder h = configurationStore.get(new MasheryPreprocessorPointReference(cfg));
        if (h != null) {
            SidecarInputBuilder<PostProcessEvent> retVal = h.postProcessorBuilder;
            if (retVal == null) {
                synchronized (syncMon) {
                    if (h.postProcessorBuilder == null) {
                        h.postProcessorBuilder = compiler.compilePostProcessor(cfg);
                    }

                    retVal = h.postProcessorBuilder;
                }
            }

            return retVal;
        } else {
            throw new IllegalStateException("Missing configuration in store; check upstream sequence of call.");
        }
    }

    @Override
    public SidecarInputBuilder<PreProcessEvent> getPreflightInputBuilder(SidecarConfiguration cfg) {
        SidecarConfigurationHolder h = configurationStore.get(new MasheryPreprocessorPointReference(cfg));
        if (h != null) {
            SidecarInputBuilder<PreProcessEvent> retVal = h.preflightBuilder;
            if (retVal == null) {
                synchronized (syncMon) {
                    if (h.preflightBuilder == null) {
                        h.preflightBuilder = compiler.compilePreFlight(cfg);
                    }

                    retVal = h.preflightBuilder;
                }
            }

            return retVal;
        } else {
            throw new IllegalStateException("Missing configuration in store; check upstream sequence of call.");
        }
    }

    /**
     * Returns the set of references that are declared in this file.
     * @param file path to the file
     * @return set of references, or empty set if no references are found.
     */
    @Override
    public Set<MasheryPreprocessorPointReference> getDeclaredIn(String file) {
        HashSet<MasheryPreprocessorPointReference> retVal = new HashSet<>();
        configurationStore.forEach((key, storedConfig) -> {
            if (storedConfig.isDeclaredIn(file)) {
                retVal.add(key);
            }
        });

        return retVal;
    }

    @Override
    public void acceptConfigurationChange(MasheryPreprocessorPointReference ref, String declaredInFile, SidecarConfiguration cfg) {
        SidecarConfigurationHolder h = configurationStore.get(ref);
        if (h != null) {
            h.setConfiguration(cfg);
            h.masheryConfigReloadable = false;
            h.declaredInFile = declaredInFile;
        } else {
            synchronized (syncMon) {
                h = new SidecarConfigurationHolder();
                h.setConfiguration(cfg);
                h.masheryConfigReloadable = false;
                h.declaredInFile = declaredInFile;
                configurationStore.put(ref, h);
            }
        }
    }

    @Override
    public void forget(MasheryPreprocessorPointReference ref) {
        configurationStore.remove(ref);
    }

    class SidecarConfigurationHolder {

        String declaredInFile;
        Map<String, String> originalConfiguration;
        boolean masheryConfigReloadable = true;
        SidecarConfiguration sidecarConfig;

        SidecarInputBuilder<PreProcessEvent> preProcessorBuilder;
        SidecarInputBuilder<PostProcessEvent> postProcessorBuilder;
        SidecarInputBuilder<PreProcessEvent> preflightBuilder;

        AtomicLong checkTime = new AtomicLong(System.currentTimeMillis());

        boolean upToDate() {
            return System.currentTimeMillis() - checkTime.get() <= checkFrequency;
        }

        void setConfiguration(SidecarConfiguration cfg) {
            this.sidecarConfig = cfg;

            preflightBuilder = null;
            preProcessorBuilder = null;
            postProcessorBuilder = null;

            checkTime.set(System.currentTimeMillis());
        }

        /**
         * Checks if this configuration is declared in the local file
         * @param file path to the file
         * @return true only if this entry (a) was declared from the file, and that the argument matches
         * the value stored in the object.
         */
        boolean isDeclaredIn(String file) {
            if (declaredInFile == null) return false;
            return Objects.equals(declaredInFile, file);
        }

        void refreshBuilders() {
            if (preflightBuilder != null) {
                preflightBuilder = getPreflightInputBuilder(sidecarConfig);
            }

            if (preProcessorBuilder != null) {
                preProcessorBuilder = getPreProcessorInputBuilder(sidecarConfig);
            }

            if (postProcessorBuilder != null) {
                postProcessorBuilder = getPostProcessorInputBuilder(sidecarConfig);
            }
        }
    }
}
