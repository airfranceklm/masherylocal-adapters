package com.airfranceklm.amt.sidecar.model;

import com.airfranceklm.amt.sidecar.elements.ElementSpec;
import com.airfranceklm.amt.sidecar.elements.StringFilterAlgorithms;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.Consumer;

import static com.airfranceklm.amt.sidecar.elements.DataElementFilterIntent.MatchRequired;
import static com.airfranceklm.amt.sidecar.model.ElementVisibility.Filters;

/**
 * Configuration of the sidecar required for the particular service Id and endpoint.
 * <p>
 * The sidecar configuration abstracts the configuration data that can be presented in the variety of sources. For
 * example, it hides the difference whether the configuration comes from Mashery key-value settings or it has
 * loaded from a local YAML configuration.
 * </p>
 * This configuration isn't immediately executable. A builder needs to be built.
 * <p>
 * The configuration also centralized dealing with case insensitivity of the headers.
 */
@AllArgsConstructor
public abstract class SidecarConfiguration {

    private static final long DEFAULT_SIDECAR_TIMEOUT = 3000L;

    @Getter @Setter private String serviceId;
    @Getter @Setter private String endpointId;

    @Getter @Setter private MaxPayloadSizeSetting maximumRequestPayloadSize;

    /**
     * Defines the synchronicity of the invocation.
     */
    @Getter @Setter private SidecarSynchronicity synchronicity;

    /**
     * Whether this function is fail-safe. Fail-safe sidecars are not essential for producing the return value
     * to the back-end and to the client. If a function is not a fail-safe -- i.e., an invocation must be successful --
     * then 500 should be returned if such function would fail.
     */
    @Getter @Setter private boolean failsafe;

    @Getter @Setter private StackDemand stack;

    @Getter @Setter private Map<String, Object> sidecarParams;

    @Getter @Setter private long timeout;

    private int errors = 0;

    @Getter @Setter private List<ElementDemand> elements;

    /**
     * Messages from the configuration parsing.
     */
    @Getter @Setter private List<String> messages;

    @Getter @Setter private ALCPConfiguration alcpConfiguration;

    public SidecarConfiguration() {
        timeout = DEFAULT_SIDECAR_TIMEOUT;
    }

    public abstract SidecarInputPoint getPoint();

    public void maximumRequestPayloadSizeFrom(Long size, MaxPayloadSizeExcessAction action) {
        maximumRequestPayloadSize = new MaxPayloadSizeSetting(size, action);
    }

    private <T> void filterOutNulls(T[] list, Consumer<T> c) {
        for (T v : list) {
            if (v != null) {
                c.accept(v);
            }
        }
    }

    public void addSidecarParameter(String name, Object value) {
        if (sidecarParams == null) {
            sidecarParams = new HashMap<>();
        }
        sidecarParams.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getSidecarParameter(String pName) {
        if (sidecarParams == null) {
            return null;
        } else {
            return (T) sidecarParams.get(pName);
        }
    }

    public boolean hasSidecarParams() {
        return sidecarParams != null && sidecarParams.size() > 0;
    }


    /**
     * Increment an error in this configuration.
     */
    public void incrementError() {
        errors++;
    }

    /**
     * Increment an error in this configuration.
     */
    public void incrementError(int count) {
        errors += count;
    }

    public boolean hasErrors() {
        return errors > 0;
    }

    public void addElement(ElementDemand elems) {
        if (this.elements == null) {
            this.elements = new ArrayList<>();
        }
        this.elements.add(elems);
    }

    public MasheryProcessorPointReference getPointReference() {
        return new MasheryProcessorPointReference(this.getServiceId(), this.getEndpointId(), this.getPoint());
    }

    public StackDemand allocOrGetStackDemand() {
        if (stack == null) {
            stack = new StackDemand();
        }
        return stack;
    }

    public ElementDemand demandElement(String val) {
        final ElementDemand retVal = new ElementDemand(val);
        allocOrGetElements().add(retVal);
        return retVal;
    }

    public ElementDemand demandElement(String val, String param) {
        final ElementDemand retVal = new ElementDemand(val, param);
        allocOrGetElements().add(retVal);
        return retVal;
    }

    public ALCPConfiguration allocOrGetALCPConfiguration() {
        if (alcpConfiguration == null) {
            alcpConfiguration = new ALCPConfiguration();
        }
        return alcpConfiguration;
    }

    private List<ElementDemand> allocOrGetElements() {
        if (elements == null) {
            elements = new ArrayList<>();
        }
        return elements;
    }

    /**
     * Retrieves and removes the element from the configuration list.
     * @param elementName name of the lement
     * @param param parameter
     * @return entity if it exists, otherwise null.
     */
    public ElementDemand popElement(String elementName, String param) {
        if (elements != null) {
            for (ElementDemand d: elements) {
                if (d.demands(elementName, param)) {
                    elements.remove(d);
                    return d;
                }
            }
        }
        return null;
    }

    /**
     * Creates the element that will only apply at filtering stage, and should not be propagated
     * to the sidecar input
     * @param name name of the element
     * @param param parameter
     * @return
     */
    public ElementDemand allocOrGetFilteredElement(String name, String param) {
        for (ElementDemand d: allocOrGetElements()) {
            if (d.demands(name, param)) {
                return d;
            }
        }

        ElementDemand retVal = new ElementDemand(name, param);
        retVal.setVisibility(Filters);
        this.elements.add(retVal);
        return retVal;
    }

    public void requireAll(String elementName, String... params) {
        for (String h : params) {
            ElementDemand ed = allocOrGetElementDemand(elementName, h);
            ed.addFilter(StringFilterAlgorithms.NonEmpty, MatchRequired);

            addElement(ed);
        }
    }

    /**
     * Demand inclusion of the specified element
     * @param elemName element
     * @param params a standard value-list expression
     */
    public void demandAll(String elemName, String... params) {
        for (String h : params) {
            allocOrGetElementDemand(elemName, h);
        }
    }

    public ElementDemand allocOrGetElementDemand(String name, String param) {
        for (ElementDemand d: allocOrGetElements()) {
            if (d.demands(name, param)) {
                return d;
            }
        }

        ElementDemand retVal = new ElementDemand(name, param);
        this.elements.add(retVal);
        return retVal;
    }

    public boolean demandsElement(ElementSpec ne) {
        return demandsElement(Objects.requireNonNull(ne).getElementName());
    }

    /**
     * Checks if the element is currently demanded.
     * @param element name of the element to be demanded.
     * @return
     */
    public boolean demandsElement(String element) {
        if (elements != null) {
            for (ElementDemand ed: elements) {
                if (element.equals(ed.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addMessage(String str) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        this.messages.add(str);
    }
}
