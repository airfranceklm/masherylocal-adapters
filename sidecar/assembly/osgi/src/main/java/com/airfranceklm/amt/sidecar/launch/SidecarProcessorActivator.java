package com.airfranceklm.amt.sidecar.launch;

import com.airfranceklm.amt.sidecar.SidecarProcessor;
import com.airfranceklm.amt.sidecar.stack.LocalOverrideStack;
import com.airfranceklm.amt.sidecar.stack.LogStack;
import com.mashery.trafficmanager.event.listener.TrafficEventListener;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

import static com.airfranceklm.amt.sidecar.SidecarProcessorDefaults.determineLaunchLevel;
import static com.airfranceklm.amt.sidecar.SidecarProcessorDefaults.getDefaultsFor;

@Slf4j
public class SidecarProcessorActivator implements BundleActivator {

    private static final String MASHERY_PROCESSOR_NAME ="com.airfranceklm.Sidecar";

    private SidecarProcessor processor;

    @Override
    public void start(BundleContext bundleContext) throws Exception {

        processor.start();

        Dictionary<String,String> dict = new Hashtable<>();
        dict.put("processor-name", MASHERY_PROCESSOR_NAME);
        dict.put("lifecycle-callback-state", "runtime");

        bundleContext.registerService(TrafficEventListener.class.getName(),
                createProcessor(bundleContext),
                dict);

        log.warn("Air France/KLM Sidecar processor with OSGi support has been activated.");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if (processor != null) {
            processor.stop();
        }
    }

    private SidecarProcessor createProcessor(BundleContext ctx) {

        final OSGIStacks sidecarStacks = new OSGIStacks(processor, ctx);
        sidecarStacks.addStack(LogStack.STACK_NAME, new LogStack());
        sidecarStacks.addStack(LocalOverrideStack.STACK_NAME, new LocalOverrideStack());

        processor = getDefaultsFor(determineLaunchLevel())
                .apply(sidecarStacks)
                .build();

        processor.setup();
        return processor;
    }
}
