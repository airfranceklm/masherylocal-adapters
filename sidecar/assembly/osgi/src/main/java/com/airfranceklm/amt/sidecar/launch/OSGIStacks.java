package com.airfranceklm.amt.sidecar.launch;

import com.airfranceklm.amt.sidecar.SidecarProcessor;
import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;
import com.airfranceklm.amt.sidecar.stack.SidecarStack;
import com.airfranceklm.amt.sidecar.stack.SidecarStacks;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class OSGIStacks extends SidecarStacks {

    private BundleContext ctx;

    public OSGIStacks(SidecarProcessor p, BundleContext ctx) {
        super();
        this.ctx = ctx;
    }

    @Override
    public SidecarStack getStackFor(SidecarConfiguration cfg) {
        SidecarStack preLoader =  super.getStackFor(cfg);
        if (preLoader != null) {
            return preLoader;
        }
        return lookupStackComponent(cfg.getStack().getName());
    }

    private synchronized SidecarStack lookupStackComponent(String stackName) {
        if (hasStack(stackName)) {
            return super.getStackByName(stackName);
        }
        // We have to lookup the service.
        try {
            ServiceReference[] sr = ctx.getServiceReferences(SidecarStack.class.getName(), String.format("(stack=%s)", stackName));
            if (sr != null && sr.length == 1) {
                Object service = ctx.getService(sr[0]);
                if (service instanceof SidecarStack) {
                    SidecarStack stack = (SidecarStack)service;
                    addStack(stackName, stack);

                    return stack;
                } else {
                    log.error(String.format("Object %s doesn't implement requires service stack interface.", service));
                }
            } else {
                log.warn(String.format("Could not find service for stack %s", stackName));
            }
        } catch (InvalidSyntaxException e) {
            log.error(String.format("Cannot lookup stack %s: %s", stackName, e.getMessage()), e);
        }

        return null;
    }


}
