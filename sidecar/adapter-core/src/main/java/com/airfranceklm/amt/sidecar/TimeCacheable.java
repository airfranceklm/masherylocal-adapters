package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.MasheryProcessorPointReference;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A time-cached entity extracted from the Mashery configuration. It is meant.
 */
public class TimeCacheable<EType extends ProcessorEvent, T> {

    private String declaredIn;
    private long checkTime = -1;
    private Map<String, String> sourceConfiguration;
    private long timeToChangeCheck;
    private T cached;

    private Function<EType, T> converter;
    private Function<EType, Map<String, String>> cfgLocator;

    private MasheryProcessorPointReference point;

    public TimeCacheable(long timeToChangeCheck, Function<EType, Map<String, String>> cfgLocator, Function<EType, T> conv) {
        initConfigSourceMemory();

        this.timeToChangeCheck = timeToChangeCheck;

        locateConfigurationUsing(cfgLocator);
        extractCachedValueUsing(conv);
    }

    void initConfigSourceMemory() {
        this.sourceConfiguration = new HashMap<>();
    }

    public void timeBetweenConfigurationChangeChecks(TimeUnit ts, long value) {
        this.timeToChangeCheck = ts.toMillis(value);
    }

    public void locateConfigurationUsing(Function<EType, Map<String, String>> conv) {
        this.cfgLocator = conv;
    }

    public void extractCachedValueUsing(Function<EType, T> conv) {
        this.converter = conv;
    }

    public MasheryProcessorPointReference getPoint() {
        return point;
    }

    public void setPoint(MasheryProcessorPointReference point) {
        this.point = point;
    }

    protected long now() {
        return System.currentTimeMillis();
    }

    public void overrideFrom(String f, T value) {
        this.declaredIn = f;
        this.cached = value;
    }

    public void resetOverride() {
        this.cached = null;
        this.declaredIn = null;
    }

    public String getDeclaredIn() {
        return declaredIn;
    }

    public T get(EType event) {
        if (declaredIn != null) {
            return cached;
        }

        // Reload from Mashery configuration.
        final long now = now();

        if (this.cached != null && now - checkTime < timeToChangeCheck) {
            return cached;
        } else {
            return checkConfigurationChanged(event, now);
        }
    }

    protected T checkConfigurationChanged(EType event, long now) {
        this.checkTime = now;

        Map<String, String> cfg = cfgLocator.apply(event);

        if (eventConfigDiffers(cfg)) {
            create(event, cfg, now);
        }

        return cached;
    }

    private boolean eventConfigDiffers(Map<String, String> cfg) {
        return !this.sourceConfiguration.equals(cfg);
    }

    protected synchronized void create(EType event, Map<String,String> config, long timestamp) {
        // Second guard for the race condition
        if (eventConfigDiffers(config)) {
            cached = converter.apply(event);
            this.checkTime = timestamp;

            this.sourceConfiguration.clear();
            if (config != null) {
                this.sourceConfiguration.putAll(config);
            }
        }
    }
}
