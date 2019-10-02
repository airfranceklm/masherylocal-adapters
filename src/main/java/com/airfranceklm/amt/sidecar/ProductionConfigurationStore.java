package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.config.MasheryConfigSidecarConfigurationBuilder;
import com.airfranceklm.amt.sidecar.config.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.config.SidecarInputPoint;
import com.airfranceklm.amt.sidecar.input.SidecarInputBuilder;
import com.airfranceklm.amt.sidecar.input.SidecarRuntimeCompiler;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ProductionConfigurationStore implements SidecarConfigurationStore {

    private Map<ConfigKey, SidecarConfigurationHolder> configurationStore = new HashMap<>();
    private MasheryConfigSidecarConfigurationBuilder mashBuilder = new MasheryConfigSidecarConfigurationBuilder();

    private long checkFrequency = TimeUnit.MINUTES.toMillis(1);

    private SidecarRuntimeCompiler compiler;

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

        ConfigKey key = new ConfigKey(event.getEndpoint().getAPI().getExternalID(), event.getEndpoint().getExternalID(), point);
        SidecarConfigurationHolder holder = configurationStore.get(key);
        if (holder != null) {
            // If the configuration is not reloadable by Mashery, don't check anything.
            if (!holder.masheryConfigReloadable) {
                return holder.sidecarConfig;
            }

            if (holder.upToDate()) {
                return holder.sidecarConfig;
            } else {
                synchronized (this) {
                    return checkSidecarConfigurationUpToDate(holder, event);
                }
            }
        }

        synchronized (this) {
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

    private SidecarConfiguration checkSidecarConfigurationUpToDate(SidecarConfigurationHolder holder, ProcessorEvent ppe) {
        // If the update was already done in a different thread, return.
        if (holder.upToDate()) {
            return holder.sidecarConfig;
        }

        Map<String, String> params = getMasheryConfigurationFor(ppe, holder.sidecarConfig.getPoint());

        if (holder.originalConfiguration.equals(params)) {
            holder.checkTime.set(System.currentTimeMillis());
            return holder.sidecarConfig;
        }

        // Okay, we have a situation wherein the configuration has been changed from what was cached.
        holder.setConfiguration(mashBuilder.buildFrom(ppe));
        holder.checkTime.set(System.currentTimeMillis());

        return holder.sidecarConfig;
    }

    private Map<String, String> getMasheryConfigurationFor(ProcessorEvent ppe, SidecarInputPoint point) {
        Map<String,String> params = null;
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
        SidecarConfigurationHolder h = configurationStore.get(new ConfigKey(cfg));
        if (h != null) {
            SidecarInputBuilder<PreProcessEvent> retVal = h.preProcessorBuilder;
            if (retVal == null) {
                synchronized (this) {
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
        SidecarConfigurationHolder h = configurationStore.get(new ConfigKey(cfg));
        if (h != null) {
            SidecarInputBuilder<PostProcessEvent> retVal = h.postProcessorBuilder;
            if (retVal == null) {
                synchronized (this) {
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
        SidecarConfigurationHolder h = configurationStore.get(new ConfigKey(cfg));
        if (h != null) {
            SidecarInputBuilder<PreProcessEvent> retVal = h.preflightBuilder;
            if (retVal == null) {
                synchronized (this) {
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

    @Override
    public void acceptConfigurationChange(String serviceId, String endpointId, SidecarConfiguration cfg) {
        ConfigKey key = new ConfigKey(serviceId, endpointId, cfg.getPoint());
        SidecarConfigurationHolder h = configurationStore.get(key);
        if (h != null) {
            h.setConfiguration(cfg);
            h.masheryConfigReloadable = false;
        } else {
            synchronized (this) {
                h = new SidecarConfigurationHolder();
                h.setConfiguration(cfg);
                h.masheryConfigReloadable = false;
                configurationStore.put(key, h);
            }
        }
    }

    @Override
    public void forget(String serviceId, String endpointId, SidecarInputPoint point) {
        configurationStore.remove(new ConfigKey(serviceId, endpointId, point));
    }

    class ConfigKey {
        String serviceId;
        String endpointId;
        SidecarInputPoint point;

        ConfigKey(String serviceId, String endpointId, SidecarInputPoint point) {
            this.serviceId = serviceId;
            this.endpointId = endpointId;
            this.point = point;
        }

        ConfigKey(SidecarConfiguration cfg) {
            this.serviceId = cfg.getServiceId();
            this.endpointId = cfg.getEndpointId();
            this.point = cfg.getPoint();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConfigKey configKey = (ConfigKey) o;
            return Objects.equals(serviceId, configKey.serviceId) &&
                    Objects.equals(endpointId, configKey.endpointId) &&
                    point == configKey.point;
        }

        @Override
        public int hashCode() {
            return Objects.hash(serviceId, endpointId, point);
        }
    }

    class SidecarConfigurationHolder {

        Map<String,String> originalConfiguration;
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

    }
}
