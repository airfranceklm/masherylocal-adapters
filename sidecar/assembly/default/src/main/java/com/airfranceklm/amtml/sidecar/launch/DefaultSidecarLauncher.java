package com.airfranceklm.amtml.sidecar.launch;


import com.airfranceklm.amt.sidecar.SidecarProcessor;
import com.airfranceklm.amt.sidecar.SidecarProcessorDefaults;
import com.airfranceklm.amt.sidecar.SidecarProcessorLauncher;
import com.airfranceklm.amt.sidecar.stack.*;
import com.mashery.http.HTTPHeaders;
import com.mashery.http.MutableHTTPHeaders;
import com.mashery.http.io.ContentSource;
import com.mashery.trafficmanager.cache.CacheException;
import com.mashery.trafficmanager.event.listener.TrafficEventListener;
import com.mashery.trafficmanager.event.model.TrafficEvent;
import com.mashery.trafficmanager.event.processor.model.PostProcessEvent;
import com.mashery.trafficmanager.event.processor.model.PreProcessEvent;
import com.mashery.trafficmanager.processor.ProcessorBean;
import lombok.extern.slf4j.Slf4j;

/**
 * A default sidecar launcher, suitable for most production settings.
 * <p/>
 * The launcher will automatically determine the configuration level the requirement will be capable to support:
 * <ul>
 *     <li><code>Essential</code>: reasonable default, configured via Mashery control center</li>
 *     <li><code>Advanced</code>: support for local configuration</li>
 * </ul>
 */
@ProcessorBean(name = "com.airfranceklm.Sidecar", enabled = true, immediate = true)
@Slf4j
public class DefaultSidecarLauncher extends SidecarProcessorLauncher implements TrafficEventListener {

    public SidecarProcessor create() {
        SidecarProcessorDefaults.LaunchLevel level = SidecarProcessorDefaults.determineLaunchLevel();
        log.warn(String.format("AF/KLM Default Sidecar Launcher will launch with %s level.", level.name()));
        final SidecarStacks stacks = initializeSupportedStacks(level);

        return SidecarProcessorDefaults.getDefaultsFor(level).apply(stacks).build();
    }

    @Override
    public void handleEvent(TrafficEvent trafficEvent) {
        getDelegate().handleEvent(trafficEvent);
    }

    private SidecarStacks initializeSupportedStacks(SidecarProcessorDefaults.LaunchLevel level) {
        switch (level) {
            case Essential:
                return SidecarStacks.stacksFor(FileStack.class, HTTPSidecarStack.class, AWSLambdaStack.class);
            case Advanced:
            case BasicALCP:
            case AdvancedALCP:
                return SidecarStacks.stacksFor(LocalOverrideStack.class, HTTPSidecarStack.class, AWSLambdaStack.class, AWSLambdaOverHttpStack.class);
            default:
                throw new IllegalArgumentException("Unsupported run level");
        }
    }

    /**
     * Required copy
     */
    protected void forceCorrectImport(TrafficEvent tel) throws CacheException {

        if (tel instanceof PreProcessEvent) {
            PreProcessEvent ppe = (PreProcessEvent) tel;

            HTTPHeaders reqH = ppe.getServerRequest().getHeaders();
            ContentSource cs = ppe.getServerRequest().getBody();

            ppe.getClientRequest();
            MutableHTTPHeaders clReq = ppe.getClientRequest().getHeaders();

            ppe.getCache();
            ppe.getCache().get(getClass().getClassLoader(), "Fake");
            ppe.getDebugContext();
            ppe.getCallContext();
        } else if (tel instanceof PostProcessEvent) {
            PostProcessEvent ppe = (PostProcessEvent) tel;

            ppe.getServerResponse();
            ppe.getCache();
            ppe.getDebugContext();
            ppe.getCallContext();
        }
    }
}
