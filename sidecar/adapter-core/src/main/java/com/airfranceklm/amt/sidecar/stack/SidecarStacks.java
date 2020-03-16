package com.airfranceklm.amt.sidecar.stack;

import com.airfranceklm.amt.sidecar.SidecarProcessor;
import com.airfranceklm.amt.sidecar.model.SidecarConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic implementation of stacks.
 */
@Slf4j
public class SidecarStacks {

    private Map<String, SidecarStack> stacks;
    private SidecarProcessor ownerProcessor;

    public SidecarStacks() {
        stacks = new HashMap<>();
    }

    /**
     * Perform setup on the stacks.
     * @param sp processor to setup.
     */
    public void setup(SidecarProcessor sp) {
        this.ownerProcessor = sp;

        stacks.forEach((k, stack) -> {
            stack.useProcessorServices(sp.getProcessorServices());
            stack.useAlcpIdentities(sp.getAlcpIdentities());
        });
    }

    public SidecarStack getStackFor(SidecarConfiguration cfg) {
        if (cfg == null) {
            return null;
        } else if (cfg.getStack() == null) {
            return null;
        }
        return stacks.get(cfg.getStack().getName());
    }

    public void addStack(SidecarStack stack) {
        addStack(stack.getStackName(), stack);
    }

    public void addStack(String stackMnemonic, SidecarStack stack) {
        if (ownerProcessor != null) {
            stack.useProcessorServices(this.ownerProcessor.getProcessorServices());
        }
        stacks.put(stackMnemonic, stack);
    }

    protected boolean hasStack(String name) {
        return stacks.containsKey(name);
    }

    public SidecarStack getStackByName(String name) {
        return getStackByName(name, SidecarStack.class);
    }

    @SuppressWarnings("unchecked")
    public <T extends SidecarStack> T getStackByName(String name, Class<T> cls) {
        final SidecarStack sidecarStack = stacks.get(name);
        if (sidecarStack != null && cls.isAssignableFrom(sidecarStack.getClass())) {
            return (T) sidecarStack;
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static SidecarStacks stacksFor(Class... stacks) {
        SidecarStacks retVal = new SidecarStacks();
        for (Class cls : stacks) {
            try {
                if (SidecarStack.class.isAssignableFrom(cls)) {
                    final Object o = cls.newInstance();
                    retVal.addStack((SidecarStack) o);
                }
            } catch (IllegalAccessException | InstantiationException e) {
                log.error(String.format("Could not create stack %s: %s", cls.getName(), e.getMessage()), e);
            }
        }

        return retVal;
    }

    public String describe() {
       StringBuilder sb = new StringBuilder();
       sb.append(String.format("%d stacks", stacks.size()));
       if (stacks.size() > 0) {
           sb.append("(");
           boolean first = true;

           for (String st: stacks.keySet()) {
               if (first) {
                   sb.append(", ");
                   first = false;
               }
               sb.append(st);
           }

           sb.append(")");
       }

       return sb.toString();
    }
}
