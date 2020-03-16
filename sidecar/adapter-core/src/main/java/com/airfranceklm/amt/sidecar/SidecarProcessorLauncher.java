package com.airfranceklm.amt.sidecar;

import com.mashery.trafficmanager.cache.CacheException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class SidecarProcessorLauncher {

    @Getter
    private SidecarProcessor delegate;

    protected SidecarProcessorLauncher() {
        try {
            this.delegate = create();

            log.warn("-- Setting up the sidecar processor");
            this.delegate.setup();

            log.warn("-- Starting up the sidecar processor");
            this.delegate.start();

            log.warn("AF/KLM sidecar adapter has been initialized");
            this.delegate.describeStateOnStart();
        } catch (Throwable ex) {
            log.error(String.format("Could not initialize AF/KLM sidecar: %s", ex.getMessage()), ex);
        }
    }

    protected abstract SidecarProcessor create();

    /**
     * <b>COPY THIS METHOD TO THE RUNNABLE SUBCLASS</b>
     * This method is, technically, unused. However, it causes Mashery 4 packager tool to generate the correct import
     * statements for the OSGi bundle containing your Sidecar customized processor.
     * <p/>
     * Should you remove this method, then the deployment of AF/KLM Sidecar processor will suddenly fail with a lot
     * of the class-not-found exceptions.
     *
     * @param tel traffic element
     * @throws CacheException
     */
    /*
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

     */

}
